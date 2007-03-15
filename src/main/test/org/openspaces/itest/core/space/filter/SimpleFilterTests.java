package org.openspaces.itest.core.space.filter;

import com.j_spaces.core.filters.FilterOperationCodes;
import org.openspaces.core.GigaSpace;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kimchy
 */
public class SimpleFilterTests extends AbstractDependencyInjectionSpringContextTests {

    protected SimpleFilter simpleFilter;

    protected GigaSpace gigaSpace;

    public SimpleFilterTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/space/filter/simple-filter.xml"};
    }


    public void testFilter() {
        assertNotNull(simpleFilter.gigaSpace);
        assertNull(simpleFilter.getStats().get(FilterOperationCodes.BEFORE_WRITE));
        assertNull(simpleFilter.getStats().get(FilterOperationCodes.AFTER_WRITE));
        assertNull(simpleFilter.getStats().get(FilterOperationCodes.BEFORE_READ));
        assertNull(simpleFilter.getStats().get(FilterOperationCodes.BEFORE_TAKE));

        gigaSpace.write(new Object());
        assertEquals(1, simpleFilter.getStats().get(FilterOperationCodes.BEFORE_WRITE).intValue());
        assertEquals(1, simpleFilter.getStats().get(FilterOperationCodes.AFTER_WRITE).intValue());
        assertNull(simpleFilter.getStats().get(FilterOperationCodes.BEFORE_READ));
        assertNull(simpleFilter.getStats().get(FilterOperationCodes.BEFORE_TAKE));
    }
}
