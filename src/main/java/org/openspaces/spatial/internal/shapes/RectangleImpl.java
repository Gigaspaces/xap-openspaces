/*******************************************************************************
 * Copyright (c) 2015 GigaSpaces Technologies Ltd. All rights reserved
 *
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * The license agreement granted to you by GigaSpaces.
 *******************************************************************************/
package org.openspaces.spatial.internal.shapes;

import com.gigaspaces.spatial.shapes.Rectangle;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;
import org.openspaces.spatial.spatial4j.Spatial4jShapeProvider;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author Yohana Khoury
 * @since 11.0
 */
public class RectangleImpl implements Rectangle, Spatial4jShapeProvider, Externalizable {

    private static final long serialVersionUID = 1L;

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    public RectangleImpl() {
    }

    public RectangleImpl(double minX, double maxX, double minY, double maxY) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public double getMinX() {
        return minX;
    }

    @Override
    public double getMinY() {
        return minY;
    }

    @Override
    public double getMaxX() {
        return maxX;
    }

    @Override
    public double getMaxY() {
        return maxY;
    }

    @Override
    public Shape getSpatial4jShape(SpatialContext spatialContext) {
        return spatialContext.makeRectangle(minX, maxX, minY, maxY);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rectangle other = (Rectangle) o;
        if (Double.compare(this.minX, other.getMinX()) != 0) return false;
        if (Double.compare(this.maxX, other.getMaxX()) != 0) return false;
        if (Double.compare(this.minY, other.getMinY()) != 0) return false;
        if (Double.compare(this.maxY, other.getMaxY()) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(minX);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxX);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(minY);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxY);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeDouble(minX);
        out.writeDouble(maxX);
        out.writeDouble(minY);
        out.writeDouble(maxY);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        minX = in.readDouble();
        maxX = in.readDouble();
        minY = in.readDouble();
        maxY = in.readDouble();
    }
}
