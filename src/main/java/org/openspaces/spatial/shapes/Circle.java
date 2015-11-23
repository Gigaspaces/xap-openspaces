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
 * A circle, denoted by a center point and radius.
 *
 * @author Barak Bar Orion
 * @since 11.0
 */
public interface Circle extends Shape {

    /**
     * Gets the X coordinate of the center of this circle.
     */
    double getCenterX();

    /**
     * Gets the Y coordinate of the center of this circle.
     */
    double getCenterY();

    /**
     * Gets the radius of this circle.
     */
    double getRadius();
}
