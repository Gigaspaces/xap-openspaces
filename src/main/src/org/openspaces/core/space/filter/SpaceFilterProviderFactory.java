package org.openspaces.core.space.filter;

import com.j_spaces.core.filters.FilterProvider;
import com.j_spaces.core.filters.ISpaceFilter;

/**
 * A {@link com.j_spaces.core.filters.FilterProvider FilterProvider} factory that accepts
 * a concrete {@link com.j_spaces.core.filters.ISpaceFilter ISpaceFilter} implemenation
 * in addition to all the operation codes it will listen to.
 *
 * @author kimchy
 * @see com.j_spaces.core.filters.FilterProvider
 * @see com.j_spaces.core.filters.ISpaceFilter
 * @see com.j_spaces.core.filters.FilterOperationCodes
 */
public class SpaceFilterProviderFactory extends AbstractFilterProviderFactoryBean {

    private int[] operationCodes;

    /**
     * Returns a new filter provider based on the provided
     * {@link #setFilter(com.j_spaces.core.filters.ISpaceFilter) filter} and operation
     * codes.
     */
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
