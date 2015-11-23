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
 * A Point with X and Y coordinates.
 *
 * @author Barak Bar Orion
 * @since 11.0
 */
public interface Point extends Shape {

    /** The X coordinate, or Longitude in geospatial context. */
    double getX();

    /** The Y coordinate, or Latitude in geospatial context. */
    double getY();
}
