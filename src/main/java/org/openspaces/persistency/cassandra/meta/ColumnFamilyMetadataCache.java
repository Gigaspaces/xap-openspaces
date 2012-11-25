/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
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
 *******************************************************************************/
package org.openspaces.persistency.cassandra.meta;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A simple {@link ConcurrentMap} based cache to store {@link ColumnFamilyMetadata} in-memory.
 * @since 9.5
 * @author Dan Kilman
 */
public class ColumnFamilyMetadataCache {
    
    private final ConcurrentMap<String, ColumnFamilyMetadata> knownColumnFamilies = new ConcurrentHashMap<String, ColumnFamilyMetadata>();

    /**
     * @param typeName The type name matching to requested {@link ColumnFamilyMetadata}
     * @return the matching {@link ColumnFamilyMetadata} or null if not found.
     */
    public ColumnFamilyMetadata getColumnFamily(String typeName) {
        return knownColumnFamilies.get(typeName);
    }
    
    /**
     * Adds the given {@link ColumnFamilyMetadata} using the typeName as key.
     * Will override entries if they already exist.
     * @param typeName the type name.
     * @param metadata the metadata.
     */
    public void addColumnFamily(String typeName, ColumnFamilyMetadata metadata) {
        knownColumnFamilies.put(typeName, metadata);
    }
    
    /**
     * @return A map from type name to column family metadata of all currently in-memory
     * {@link ColumnFamilyMetadata}.
     */
    public ConcurrentMap<String, ColumnFamilyMetadata> getKnownColumnFamilies() {
        return knownColumnFamilies;
    }
    
}
