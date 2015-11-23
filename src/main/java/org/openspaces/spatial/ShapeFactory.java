/*******************************************************************************
 *
 * Copyright (c) 2015 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.openspaces.spatial;

import com.gigaspaces.internal.utils.Assert;
import com.gigaspaces.spatial.shapes.*;
import org.openspaces.spatial.shapes.Circle;
import org.openspaces.spatial.shapes.Point;
import org.openspaces.spatial.shapes.Polygon;
import org.openspaces.spatial.shapes.Rectangle;
import com.gigaspaces.spatial.shapes.Shape;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import org.openspaces.spatial.internal.shapes.CircleImpl;
import org.openspaces.spatial.internal.shapes.PointImpl;
import org.openspaces.spatial.internal.shapes.PolygonImpl;
import org.openspaces.spatial.internal.shapes.RectangleImpl;
import com.spatial4j.core.context.SpatialContext;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;

/**
 * Factory class for creating spatial shapes.
 *
 * @author Niv Ingberg
 * @since 11.0
 */
public class ShapeFactory {
    /**
     * Private ctor to prevent instantiating this factory class.
     */
    private ShapeFactory() {
    }

    /**
     * Creates a Point instance.
     * @param x The X coordinate, or Longitude in geospatial contexts
     * @param y The Y coordinate, or Latitude in geospatial contexts
     * @return A new Point instance
     */
    public static Point point(double x, double y) {
        return new PointImpl(x, y);
    }

    /**
     * Creates a Circle instance
     * @param center The center of the circle
     * @param radius The radius of the circle
     * @return A new Circle instance
     */
    public static Circle circle(Point center, double radius) {
        return new CircleImpl(center.getX(), center.getY(), radius);
    }

    /**
     * Creates a Rectangle instance
     * @param minX The left edge of the X coordinate
     * @param maxX The right edge of the X coordinate
     * @param minY The bottom edge of the Y coordinate
     * @param maxY The top edge of the Y coordinate
     * @return A new Rectangle instance
     */
    public static Rectangle rectangle(double minX, double maxX, double minY, double maxY) {
        return new RectangleImpl(minX, maxX, minY, maxY);
    }

    /**
     * Creates a Polygon instance from the specified points
     * @param first The first point
     * @param second The second point
     * @param third The third point
     * @param morePoints The rest of the points
     * @return A new Polygon instance
     */
    public static Polygon polygon(Point first, Point second, Point third, Point... morePoints) {
        Point[] points = new Point[3 + morePoints.length];
        points[0] = Assert.argumentNotNull(first, "first");
        points[1] = Assert.argumentNotNull(second, "second");
        points[2] = Assert.argumentNotNull(third, "third");
        for (int i=0 ; i < morePoints.length ; i++)
            points[i+3] = morePoints[i];

        return polygon(points);
    }

    /**
     * Creates a Polygon instance from the specified points
     * @param points The polygon points
     * @return A new Polygon instance
     */
    public static Polygon polygon(Collection<Point> points) {
        return polygon(points.toArray(new Point[points.size()]));
    }

    /**
     * Parses the specified string using the specified shape format
     * @param s String to parse
     * @param shapeFormat Shape format to use for parsing
     * @return The created shape instance
     */
    public static Shape parse(String s, ShapeFormat shapeFormat) {
        switch (shapeFormat) {
            case WKT: return fromSpatial4JShape(fromWkt(s));
            case GEOJSON: return fromSpatial4JShape(fromGeoJson(s));
            default: throw new IllegalArgumentException("Unsupported shape format - " + shapeFormat);
        }
    }

    private static com.spatial4j.core.shape.Shape fromWkt(String wkt) {
        try {
            return getDefaultSpatialContext().getFormats().getWktReader().read(wkt);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse WKT shape", e);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Failed to parse WKT shape", e);
        }
    }

    private static com.spatial4j.core.shape.Shape fromGeoJson(String geoJson) {
        try {
            return getDefaultSpatialContext().getFormats().getGeoJsonReader().read(geoJson);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse GeoJson shape", e);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Failed to parse GeoJson shape", e);
        }
    }

    private static Polygon polygon(Point[] points) {
        return new PolygonImpl(points);
    }

    private static SpatialContext getDefaultSpatialContext() {
        return JtsSpatialContext.GEO;
    }

    private static Shape fromSpatial4JShape(com.spatial4j.core.shape.Shape shape) {
        if (shape instanceof com.spatial4j.core.shape.Point) {
            com.spatial4j.core.shape.Point point = (com.spatial4j.core.shape.Point) shape;
            return point(point.getX(), point.getY());
        }
        if (shape instanceof com.spatial4j.core.shape.Circle) {
            com.spatial4j.core.shape.Circle circle = (com.spatial4j.core.shape.Circle) shape;
            return circle(point(circle.getCenter().getX(), circle.getCenter().getY()), circle.getRadius());
        }
        if (shape instanceof com.spatial4j.core.shape.Rectangle) {
            com.spatial4j.core.shape.Rectangle rectangle = (com.spatial4j.core.shape.Rectangle) shape;
            return rectangle(rectangle.getMinX(), rectangle.getMaxX(), rectangle.getMinY(), rectangle.getMaxY());
        }
        if (shape instanceof com.spatial4j.core.shape.jts.JtsGeometry) {
            com.vividsolutions.jts.geom.Coordinate[] coordinates = ((com.spatial4j.core.shape.jts.JtsGeometry) shape).getGeom().getCoordinates();
            Point[] points = new Point[coordinates.length];
            for (int i=0 ; i < coordinates.length ; i++)
                points[i] = point(coordinates[i].getOrdinate(0), coordinates[i].getOrdinate(1));
            return polygon(points);

        }
        throw new IllegalArgumentException("Unsupported shape type: " + shape.getClass().getName());
    }
}
