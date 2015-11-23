/*******************************************************************************
 * Copyright (c) 2015 GigaSpaces Technologies Ltd. All rights reserved
 *
 * The software source code is proprietary and confidential information of GigaSpaces.
 * You may use the software source code solely under the terms and limitations of
 * The license agreement granted to you by GigaSpaces.
 *******************************************************************************/
//JAVA-DOC-STAMP

package org.openspaces.spatial.shapes;

import java.io.Serializable;

/**
 * Markup interface to serve as base for all spatial shapes supported by XAP.
 *
 * @author Yohana Khoury
 * @since 11.0
 */
public interface Shape extends Serializable {
    /**
     * Returns a string representation of the shape using the specified format.
     * @param shapeFormat The format which will be used to format the shape.
     * @return A string representation of the shape.
     */
    String toString(ShapeFormat shapeFormat);

    /**
     * Appends a string representation of the shape using the specified format
     * @param stringBuilder The string builder to append to
     * @param shapeFormat The format which will be used to format the shape
     * @return The string builder
     */
    StringBuilder appendTo(StringBuilder stringBuilder, ShapeFormat shapeFormat);
}
