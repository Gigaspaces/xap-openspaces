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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Yohana Khoury
 * @since 11.0
 */
public class PolygonImpl implements Polygon, Externalizable {

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
