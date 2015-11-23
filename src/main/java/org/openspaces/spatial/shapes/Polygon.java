/*******************************************************************************
 * Copyright (c) 2015 GigaSpaces Technologies Ltd. All rights reserved
 *
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * The license agreement granted to you by GigaSpaces.
 *******************************************************************************/
//JAVA-DOC-STAMP

package org.openspaces.spatial.shapes;

import com.gigaspaces.spatial.shapes.Shape;

/**
 * A polygon, denoted by 3 or more points
 *
 * @author Yohana Khoury
 * @since 11.0
 */
public interface Polygon extends Shape {

    /**
     * Returns the number of points within the polygon
     * @return The number of points within the polygon
     */
    int getNumOfPoints();

    /**
     * Gets the X coordinate of the point in the specified index.
     */
    double getX(int index);

    /**
     * Gets the Y coordinate of the point in the specified index.
     */
    double getY(int index);
}
