package org.openspaces.itest.core.context;

import org.openspaces.core.GigaSpace;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * The test verifies that two different {@link org.openspaces.core.GigaSpace} instances defined
 * within the same application context can be differnicated by using {@link org.openspaces.core.context.GigaSpaceContext#name()}.
 *
 * @author kimchy
 */
public class DoubleGigaSpaceContextTests extends AbstractDependencyInjectionSpringContextTests {

    protected GigaSpace gs1;
    protected GigaSpace gs2;

    protected DoubleContextTestBean testBean;

    public DoubleGigaSpaceContextTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/context/double-context.xml"};
    }

    public void testFieldInjectedGs1() {
        assertSame(gs1, testBean.gs1);
    }

    public void testSetterInjectionGs2() {
        assertSame(gs2, testBean.gs2);
    }
}
