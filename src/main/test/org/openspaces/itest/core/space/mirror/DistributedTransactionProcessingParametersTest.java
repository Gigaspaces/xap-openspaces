package org.openspaces.itest.core.space.mirror;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * 
 * @author idan
 * @since 8.0.4
 *
 */
public class DistributedTransactionProcessingParametersTest extends AbstractDependencyInjectionSpringContextTests {


    protected Object mirror;

    public DistributedTransactionProcessingParametersTest() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[] { "/org/openspaces/itest/core/space/mirror/mirror-dist-tx.xml" };
    }

    public void testDataSource() {
        assertNotNull(mirror);
    }

}
