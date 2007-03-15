package org.openspaces.core.space.filter;

import com.j_spaces.core.filters.FilterProvider;
import com.j_spaces.core.filters.ISpaceFilter;

/**
 * @author kimchy
 */
public class SpaceFilterProviderFactory extends AbstractFilterProviderFactoryBean {

    private int[] operationCodes;

    protected FilterProvider doGetFilterProvider() throws IllegalArgumentException {
        FilterProvider filterProvider = new FilterProvider(getBeanName(), (ISpaceFilter) getFilter());
        filterProvider.setOpCodes(operationCodes);
        return filterProvider;
    }

    /**
     * Sets a list of the operation codes mapping to filter operations.
     *
     * @see com.j_spaces.core.filters.FilterOperationCodes
     */
    public void setOperationCodes(int[] operationCodes) {
        this.operationCodes = operationCodes;
    }

    /**
     * Override the parent method since this factory requires a concrete implementation of
     * the an {@link com.j_spaces.core.filters.ISpaceFilter}.
     */
    public void setFilter(ISpaceFilter filter) {
        super.setFilter(filter);
    }
}
