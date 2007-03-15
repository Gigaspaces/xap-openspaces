package org.openspaces.itest.core.space.filter.adapter;

/**
 * @author kimchy
 */
public class MethodAdapterFilterTests extends AbstractAdapterFilterTests {

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/space/filter/adapter/adapter-method-filter.xml"};
    }
}