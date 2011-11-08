package org.openspaces.itest.core.space.filter.security.adapter;

public class AdapterMethodFilterTest extends AbstractSecurityAdapterCustomFilterTests {
        
    protected String[] getConfigLocations() {
        System.setProperty("com.gs.security.properties-file", "org/openspaces/itest/core/space/filter/security/spring-security.properties");
        return new String[]{"/org/openspaces/itest/core/space/filter/security/adapter/adapter-method-custom-filter.xml"};
    }
}
