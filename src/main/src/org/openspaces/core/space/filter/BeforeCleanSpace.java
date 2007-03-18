package org.openspaces.core.space.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Filter callback after clean space operation.
 *
 * @author kimchy
 * @see org.openspaces.core.space.filter.AnnotationFilterFactoryBean
 * @see com.j_spaces.core.filters.FilterOperationCodes#BEFORE_CLEAN_SPACE
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BeforeCleanSpace {
}