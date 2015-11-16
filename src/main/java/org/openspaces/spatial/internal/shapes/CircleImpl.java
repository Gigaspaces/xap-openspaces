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
import com.gigaspaces.spatial.shapes.Circle;
import com.gigaspaces.spatial.shapes.Point;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author Barak Bar Orion
 * @since 11.0
 */
public class CircleImpl implements Circle, Externalizable {

    private static final long serialVersionUID = 1L;

    private Point center;
    private double radius;

    public CircleImpl() {
    }

    public CircleImpl(Point center, double radius) {
        this.center = Assert.argumentNotNull(center, "center");
        this.radius = radius;
    }

    @Override
    public Point getCenter() {
        return center;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Circle other = (Circle) o;
        if (!this.center.equals(other.getCenter())) return false;
        if (Double.compare(other.getRadius(), this.radius) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = center != null ? center.hashCode() : 0;
        temp = Double.doubleToLongBits(radius);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        IOUtils.writeObject(out, center);
        out.writeDouble(radius);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        center = IOUtils.readObject(in);
        radius = in.readDouble();
    }
}
