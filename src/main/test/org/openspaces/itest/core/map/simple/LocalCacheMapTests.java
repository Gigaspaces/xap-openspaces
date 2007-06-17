package org.openspaces.itest.core.map.simple;

/**
 * @author kimchy
 */
public class LocalCacheMapTests extends AbstractMapTests {

    protected String[] getConfigLocations() {
        return new String[] {"/org/openspaces/itest/core/map/simple/local-cache-map.xml"};
    }
}