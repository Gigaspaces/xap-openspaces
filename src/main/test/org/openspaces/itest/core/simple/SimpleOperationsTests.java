package org.openspaces.itest.core.simple;

import org.openspaces.core.GigaSpace;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kimchy
 */
public class SimpleOperationsTests extends AbstractDependencyInjectionSpringContextTests {

    protected GigaSpace gigaSpace;

    public SimpleOperationsTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/simple/context.xml"};
    }

    protected void onSetUp() throws Exception {
        gigaSpace.clear(null);
    }

    protected void onTearDown() throws Exception {
        gigaSpace.clear(null);
    }

    public void testSimpleOperations() throws Exception {
        assertEquals(0, gigaSpace.count(null));
        gigaSpace.write(new Message("test"));
        assertEquals(1, gigaSpace.count(null));

        Message val = gigaSpace.read(null);
        assertNotNull(val);

        val = gigaSpace.take(null);
        assertNotNull(val);
    }
}
