/*******************************************************************************
 * Copyright (c) 2015 GigaSpaces Technologies Ltd. All rights reserved
 *
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * The license agreement granted to you by GigaSpaces.
 *******************************************************************************/
package org.openspaces.spatial.internal.shapes;

import com.gigaspaces.internal.io.IOUtils;
import com.gigaspaces.internal.utils.Assert;
import com.gigaspaces.spatial.shapes.Point;
import com.gigaspaces.spatial.shapes.Polygon;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;
import org.openspaces.spatial.spatial4j.Spatial4jShapeProvider;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Yohana Khoury
 * @since 11.0
 */
public class PolygonImpl implements Polygon, Spatial4jShapeProvider, Externalizable {

    private static final long serialVersionUID = 1L;

    private Point[] points;
    private transient int hashcode;

    public PolygonImpl() {
    }

    public PolygonImpl(Point first, Point second, Point third, Point... morePoints) {
        this.points = new Point[3 + morePoints.length];
        this.points[0] = Assert.argumentNotNull(first, "first");
        this.points[1] = Assert.argumentNotNull(second, "second");
        this.points[2] = Assert.argumentNotNull(third, "third");
        for (int i=0 ; i < morePoints.length ; i++)
            this.points[i+3] = morePoints[i];
        initialize();
    }

    public PolygonImpl(Point[] points) {
        if (points.length < 3)
            throw new IllegalArgumentException("Polygon requires at least three points");
        this.points = new Point[points.length];
        for (int i=0 ; i < points.length ; i++)
            this.points[i] = points[i];
        initialize();
    }

    public PolygonImpl(Collection<Point> points) {
        if (points.size() < 3)
            throw new IllegalArgumentException("Polygon requires at least three points");
        this.points = points.toArray(new Point[points.size()]);
        initialize();
    }

    private void initialize() {
        if (points.length == 3) {
            Assert.isTrue((!points[0].equals(points[2])), "Polygon requires at least three distinct points " + Arrays.asList(points));
        }
        this.hashcode = Arrays.hashCode(points);
    }

    @Override
    public Point getPoint(int index) {
        return points[index];
    }

    @Override
    public int getNumOfPoints() {
        return points.length;
    }

    @Override
    public Shape getSpatial4jShape(SpatialContext spatialContext) {
        try {
            return spatialContext.readShapeFromWkt(toWkt());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private String toWkt() {
        String coordinates = "";
        for (int i = 0; i < points.length; i++)
            coordinates += (i == 0 ? "" : ",") + points[i].getX() + " " + points[i].getY();
        if (!points[points.length-1].equals(points[0]))
            coordinates += "," + points[0].getX() + " " + points[0].getY();
        return "POLYGON ((" + coordinates + "))";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PolygonImpl other = (PolygonImpl) o;
        return Arrays.equals(this.points, other.points);
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(points.length);
        for (int i=0 ; i < points.length ; i++)
            IOUtils.writeObject(out, points[i]);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int length = in.readInt();
        points = new Point[length];
        for (int i=0 ; i < length ; i++)
            points[i] = IOUtils.readObject(in);
        initialize();
    }
}
