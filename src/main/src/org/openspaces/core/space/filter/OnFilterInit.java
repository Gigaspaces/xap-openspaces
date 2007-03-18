package org.openspaces.core.space.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A callback method when the filter is initialized. Can either have no arguments or a single
 * argument thata accepts {@link com.j_spaces.core.IJSpace}.
 *
 * @author kimchy
 * @see org.openspaces.core.space.filter.AnnotationFilterFactoryBean
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OnFilterInit {
}