package org.openspaces.core.space.filter;

import com.j_spaces.core.filters.FilterProvider;

/**
 * A filter provider factory is a bean responsible for creating / providing
 * {@link com.j_spaces.core.filters.FilterProvider} implementation.
 *
 * @author kimchy
 * @see org.openspaces.core.space.filter.AnnotationFilterFactoryBean
 * @see org.openspaces.core.space.filter.MethodFilterFactoryBean
 */
public interface FilterProviderFactory {

    /**
     * Returns a {@link com.j_spaces.core.filters.FilterProvider}.
     */
    FilterProvider getFilterProvider();
}
