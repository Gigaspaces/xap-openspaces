package org.openspaces.itest.core.map.simple;

import com.j_spaces.map.IMap;
import org.openspaces.core.GigaMap;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kimchy
 */
public abstract class AbstractMapTests extends AbstractDependencyInjectionSpringContextTests {

    protected IMap map;

    protected GigaMap gigaMap;

    protected AbstractMapTests() {
        setPopulateProtectedVariables(true);
    }

    public void testSimpleMapOperations() {
        map.put("1", "value");

        assertEquals("value", map.get("1"));

        assertEquals("value", map.remove("1"));
    }

    public void testSimpleGigaMapOperations() {
        gigaMap.put("1", "value");

        assertEquals("value", gigaMap.get("1"));

        assertEquals("value", gigaMap.remove("1"));
    }
}
