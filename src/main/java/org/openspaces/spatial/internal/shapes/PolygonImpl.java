/*******************************************************************************
 * Copyright (c) 2015 GigaSpaces Technologies Ltd. All rights reserved
 *
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * The license agreement granted to you by GigaSpaces.
 *******************************************************************************/
package org.openspaces.spatial.internal.shapes;

import com.gigaspaces.internal.io.IOUtils;
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

    public PolygonImpl(Point[] points) {
        if (points.length < 3)
            throw new IllegalArgumentException("Polygon requires at least three points");
        if (points.length == 3 && points[0].equals(points[2]))
            throw new IllegalArgumentException("Polygon requires at least three distinct points " + Arrays.asList(points));
        this.points = points;
        initialize();
    }

    private void initialize() {
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
            return spatialContext.getFormats().getWktReader().read(toWkt());
        } catch (ParseException e) {
            throw new IllegalStateException("Failed to convert polygon to Spatial4J", e);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to convert polygon to Spatial4J", e);
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
