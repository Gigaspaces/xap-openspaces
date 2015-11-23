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
 * Encapsulates Shape formats supported by XAP GeoSpatial API
 *
 * @author Niv Ingberg
 * @since 11.0
 */
public enum ShapeFormat {
    /**
     * Well-Known text.
     * @see <a href="https://en.wikipedia.org/wiki/Well-known_text">WKT</a>
     */
    WKT,
    /**
     * GeoJson.
     * @see <a href="http://geojson.org/">GeoJson</a>
     */
    GEOJSON
}
