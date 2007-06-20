package org.openspaces.itest.core.map.simple;

import com.j_spaces.core.client.cache.map.MapCache;

/**
 * @author kimchy
 */
public class LocalCacheMapTests extends AbstractMapTests {

    protected String[] getConfigLocations() {
        return new String[] {"/org/openspaces/itest/core/map/simple/local-cache-map.xml"};
    }

    public void testIMapType() {
        assertTrue(map instanceof MapCache);
    }
}