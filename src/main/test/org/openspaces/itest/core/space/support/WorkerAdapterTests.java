package org.openspaces.itest.core.space.support;

import org.openspaces.core.GigaSpace;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kimchy
 */
public class WorkerAdapterTests extends AbstractDependencyInjectionSpringContextTests {

    protected GigaSpace gigaSpace1;

    protected GigaSpace gigaSpace2;

    protected MyWorker worker1;

    protected MyWorker worker2;

    public WorkerAdapterTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/space/support/context.xml"};
    }

    public void testCorrectCalls() throws Exception {
        assertTrue(worker1.isInitCalled());
        
        assertFalse(worker2.isInitCalled());

        // sleep to wait for the thread to start
        Thread.sleep(500);
        
        assertTrue(worker1.isRunCalled());
        assertFalse(worker2.isRunCalled());
    }
}

