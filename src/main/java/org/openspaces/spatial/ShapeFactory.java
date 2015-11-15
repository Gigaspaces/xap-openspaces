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

import com.gigaspaces.spatial.shapes.*;
import com.gigaspaces.spatial.shapes.Circle;
import com.gigaspaces.spatial.shapes.Point;
import com.gigaspaces.spatial.shapes.Rectangle;
import com.gigaspaces.spatial.shapes.Shape;
import com.gigaspaces.spatial.shapes.internal.CircleImpl;
import com.gigaspaces.spatial.shapes.internal.PointImpl;
import com.gigaspaces.spatial.shapes.internal.PolygonImpl;
import com.gigaspaces.spatial.shapes.internal.RectangleImpl;
import com.spatial4j.core.context.SpatialContext;

import java.io.IOException;
import java.text.ParseException;

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
        return new CircleImpl(center, radius);
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
     * Creates a Polygon instance
     * @param first The first point
     * @param second The second point
     * @param third The third point
     * @param morePoints The rest of the points
     * @return A new Polygon instance
     */
    public static Polygon polygon(Point first, Point second, Point third, Point... morePoints) {
        return new PolygonImpl(first, second, third, morePoints);
    }

    /**
     * Under construction
     */
    private static Shape fromWkt(String wkt) throws ParseException, IOException {
        com.spatial4j.core.shape.Shape shape = SpatialContext.GEO.getFormats().getWktReader().read(wkt);
        return fromSpatial4JShape(shape);
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
        throw new IllegalArgumentException("Unsupported shape type: " + shape.getClass().getName());
    }
}
