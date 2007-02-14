package org.openspaces.itest.core.context;

import org.openspaces.core.GigaSpace;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * A simple test that verifies that {@link org.openspaces.core.GigaSpace} gets injected using
 * {@link org.openspaces.core.context.GigaSpaceContext} for both field level and setter level
 * annotation based injection.
 *
 * @author kimchy
 */
public class SingleGigaSpaceContextTests extends AbstractDependencyInjectionSpringContextTests {

    protected GigaSpace gigaSpace;

    protected SingleContextTestBean testBean;

    public SingleGigaSpaceContextTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/context/single-context.xml"};
    }

    public void testFieldInjected() {
        assertSame(gigaSpace, testBean.gs1);
    }

    public void testSetterInjection() {
        assertSame(gigaSpace, testBean.gs2);
    }
}
