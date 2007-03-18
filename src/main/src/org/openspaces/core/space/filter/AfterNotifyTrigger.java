package org.openspaces.core.space.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Filter callback after notify trigger. Indicates that a notify trigger was
 * successful.
 *
 * @author kimchy
 * @see org.openspaces.core.space.filter.AnnotationFilterFactoryBean
 * @see com.j_spaces.core.filters.FilterOperationCodes#AFTER_NOTIFY_TRIGGER
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterNotifyTrigger {
}