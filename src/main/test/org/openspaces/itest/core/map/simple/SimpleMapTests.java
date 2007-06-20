package org.openspaces.itest.core.map.simple;

import com.j_spaces.map.GSMapImpl;

/**
 * @author kimchy
 */
public class SimpleMapTests extends AbstractMapTests {

    protected String[] getConfigLocations() {
        return new String[] {"/org/openspaces/itest/core/map/simple/simple-map.xml"};
    }

    public void testIMapType() {
        assertTrue(map instanceof GSMapImpl);
    }
}
