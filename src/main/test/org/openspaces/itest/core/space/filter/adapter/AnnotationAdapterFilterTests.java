package org.openspaces.itest.core.space.filter.adapter;

import org.openspaces.core.GigaSpace;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kimchy
 */
public class AnnotationAdapterFilterTests extends AbstractDependencyInjectionSpringContextTests {

    protected SimpleFilter simpleFilter;

    protected GigaSpace gigaSpace;

    public AnnotationAdapterFilterTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/space/filter/adapter/adapter-filter.xml"};
    }

    protected void onSetUp() throws Exception {
        gigaSpace.clear(new Object());
        simpleFilter.clearExecutions();
    }

    public void testWrite() {
        Message message = new Message("test");
        gigaSpace.write(message);
        assertEquals(1, simpleFilter.getLastExecutions().size());
        Object[] params = simpleFilter.getLastExecutions().get(0);
        assertEquals(1, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());

        simpleFilter.clearExecutions();
        
        Echo echo = new Echo("test");
        gigaSpace.write(echo);
        assertEquals(0, simpleFilter.getLastExecutions().size());
    }
}