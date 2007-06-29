package org.openspaces.core.space.filter;

import com.j_spaces.core.filters.FilterProvider;

/**
 * @author kimchy
 */
public interface FilterProviderFactory {

    FilterProvider getFilterProvider();
}
