/*******************************************************************************
 * Copyright (c) 2015 GigaSpaces Technologies Ltd. All rights reserved
 *
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * The license agreement granted to you by GigaSpaces.
 *******************************************************************************/
package org.openspaces.spatial.internal.shapes;

import com.gigaspaces.spatial.shapes.Point;
import com.gigaspaces.spatial.shapes.ShapeFormat;
import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;
import org.openspaces.spatial.spatial4j.Spatial4jShapeProvider;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author Barak Bar Orion
 * @since 11.0
 */
public class PointImpl implements Point, Spatial4jShapeProvider, Externalizable {
    private static final long serialVersionUID = 1L;

    private double x;
    private double y;
    private volatile transient com.spatial4j.core.shape.Shape spatial4jShape;

    public PointImpl() {
    }

    public PointImpl(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public String toString(ShapeFormat shapeFormat) {
        return append(new StringBuilder(), shapeFormat).toString();
    }

    @Override
    public StringBuilder append(StringBuilder stringBuilder, ShapeFormat shapeFormat) {
        switch (shapeFormat) {
            case WKT:       return appendWkt(stringBuilder);
            case GEOJSON:   return appendGeoJson(stringBuilder);
            default:        throw new IllegalArgumentException("Unsupported shape type: " + shapeFormat);
        }
    }

    private StringBuilder appendWkt(StringBuilder stringBuilder) {
        stringBuilder.append("POINT (");
        stringBuilder.append(x);
        stringBuilder.append(' ');
        stringBuilder.append(y);
        stringBuilder.append(')');
        return stringBuilder;
    }

    private StringBuilder appendGeoJson(StringBuilder stringBuilder) {
        stringBuilder.append("{\"type\":\"Point\",\"coordinates\":[");
        stringBuilder.append(x);
        stringBuilder.append(',');
        stringBuilder.append(y);
        stringBuilder.append("]}");
        return stringBuilder;
    }

    @Override
    public Shape getSpatial4jShape(SpatialContext spatialContext) {
        com.spatial4j.core.shape.Shape result = this.spatial4jShape;
        if (result == null) {
            result = spatialContext.makePoint(x, y);
            this.spatial4jShape = result;
        }
        return result;
    }

    @Override
    public String toString() {
        return toString(ShapeFormat.WKT);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point other = (Point) o;
        if (Double.compare(this.x, other.getX()) != 0) return false;
        if (Double.compare(this.y, other.getY()) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeDouble(x);
        out.writeDouble(y);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        x = in.readDouble();
        y = in.readDouble();
    }
}
