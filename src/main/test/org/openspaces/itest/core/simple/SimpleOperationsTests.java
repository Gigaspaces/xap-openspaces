package org.openspaces.itest.core.simple;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.support.WaitForAllListener;
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

    public void testAsyncOperations() throws Exception {
        assertEquals(0, gigaSpace.count(null));
        assertNull(gigaSpace.asyncRead(new Message()).get());
        assertNull(gigaSpace.asyncRead(new Message(), 0).get());
        assertNull(gigaSpace.asyncRead(new Message(), 0, 0).get());

        WaitForAllListener listener = new WaitForAllListener(1);
        gigaSpace.asyncRead(new Message(), listener);
        assertNull(listener.waitForResult()[0].get());

        listener = new WaitForAllListener(1);
        gigaSpace.asyncRead(new Message(), 0, listener);
        assertNull(listener.waitForResult()[0].get());

        listener = new WaitForAllListener(1);
        gigaSpace.asyncRead(new Message(), 0, 0, listener);
        assertNull(listener.waitForResult()[0].get());

        assertNull(gigaSpace.asyncTake(new Message()).get());
        assertNull(gigaSpace.asyncTake(new Message(), 0).get());
        assertNull(gigaSpace.asyncTake(new Message(), 0, 0).get());

        listener = new WaitForAllListener(1);
        gigaSpace.asyncTake(new Message(), listener);
        assertNull(listener.waitForResult()[0].get());

        listener = new WaitForAllListener(1);
        gigaSpace.asyncTake(new Message(), 0, listener);
        assertNull(listener.waitForResult()[0].get());

        listener = new WaitForAllListener(1);
        gigaSpace.asyncTake(new Message(), 0, 0, listener);
        assertNull(listener.waitForResult()[0].get());

        gigaSpace.write(new Message("test"));
        assertEquals(1, gigaSpace.count(null));
        assertEquals(gigaSpace.asyncRead(new Message()).get().getValue(), "test");
        assertEquals(1, gigaSpace.count(null));
        assertEquals(gigaSpace.asyncTake(new Message()).get().getValue(), "test");
        assertEquals(0, gigaSpace.count(null));
    }
}
