package org.openspaces.itest.core.space.filter.adapter;

/**
 * @author kimchy
 */
public class AnnotationAdapterFilterTests extends AbstractAdapterFilterTests {

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/space/filter/adapter/adapter-annotation-filter.xml"};
    }
}