/*******************************************************************************
 * Copyright (c) 2015 GigaSpaces Technologies Ltd. All rights reserved
 *
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * The license agreement granted to you by GigaSpaces.
 *******************************************************************************/
//JAVA-DOC-STAMP

package org.openspaces.spatial.shapes;

/**
 * A rectangle, denoted by 2 points.
 *
 * @author Yohana Khoury
 * @since 11.0
 */
public interface Rectangle extends Shape {

    /** The left edge of the X coordinate. */
    double getMinX();

    /** The right edge of the X coordinate. */
    double getMaxX();

    /** The bottom edge of the Y coordinate. */
    double getMinY();

    /** The top edge of the Y coordinate. */
    double getMaxY();
}
