package org.openspaces.core.space.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Filter callback after all notify trigger. Indicates that all notify templates that are
 * matched to the current entry event were triggered and returned or failed.
 *
 * @author kimchy
 * @see org.openspaces.core.space.filter.AnnotationFilterFactoryBean
 * @see com.j_spaces.core.filters.FilterOperationCodes#AFTER_ALL_NOTIFY_TRIGGER
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AfterAllNotifyTrigger {
}