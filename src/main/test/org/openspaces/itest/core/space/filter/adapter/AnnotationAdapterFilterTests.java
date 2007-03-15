package org.openspaces.itest.core.space.filter.adapter;

import com.j_spaces.core.filters.FilterOperationCodes;
import com.j_spaces.core.filters.entry.ISpaceFilterEntry;
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

    public void testOnInit() {
        assertTrue(simpleFilter.isOnInitCalled());
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
        assertEquals(1, simpleFilter.getLastExecutions().size());
        params = simpleFilter.getLastExecutions().get(0);
        assertEquals(1, params.length);
        assertEquals("test", ((Echo) params[0]).getMessage());
    }

    public void testRead() throws Exception {
        Message message = new Message("test");
        gigaSpace.read(message);
        assertEquals(1, simpleFilter.getLastExecutions().size());
        Object[] params = simpleFilter.getLastExecutions().get(0);
        assertEquals(1, params.length);
        assertEquals("test", ((Message)((ISpaceFilterEntry) params[0]).getObject(gigaSpace.getSpace())).getMessage());
    }

    public void testTake() {
        Message message = new Message("test");
        gigaSpace.take(message);
        assertEquals(1, simpleFilter.getLastExecutions().size());
        Object[] params = simpleFilter.getLastExecutions().get(0);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.BEFORE_TAKE, params[1]);
    }
}