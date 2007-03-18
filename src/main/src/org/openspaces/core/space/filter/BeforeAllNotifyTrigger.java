package org.openspaces.core.space.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Filter callback before all notify trigger. Indicates that at least one notify template is
 * matched to the current entry event.
 *
 * @author kimchy
 * @see org.openspaces.core.space.filter.AnnotationFilterFactoryBean
 * @see com.j_spaces.core.filters.FilterOperationCodes#BEFORE_ALL_NOTIFY_TRIGGER
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface BeforeAllNotifyTrigger {
}