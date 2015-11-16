/*******************************************************************************
 *
 * Copyright (c) 2015 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.openspaces.spatial.spatial4j;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Shape;

/**
 * A helper interface for converting XAP shapes to Spatial4j shapes.
 *
 * @author Niv Ingberg
 * @since 11.0
 */
public interface Spatial4jShapeProvider {
    /**
     * Gets a Spatial4j shape from the current object
     * @param spatialContext The SpatialContext which will be used to construct the shape, if needed.
     * @return A Spatial4j shape
     */
    Shape getSpatial4jShape(SpatialContext spatialContext);
}
