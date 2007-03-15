package org.openspaces.core.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to directly inject a {@link org.openspaces.core.GigaSpace} implementation into a class field or setter
 * property. A name can be specified in cases where more than one {@link org.openspaces.core.GigaSpace} are defined
 * within a spring application context. The name will be the bean name / id. This annotation will cause the GigaSpace
 * instance to be injected late during the bean lifecycle (after its properies were set). It allows to break cyclic
 * relationships on beans that needs a GigaSpace instance but must be initalized before the space.
 *
 * @author kimchy
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GigaSpaceLateContext {

    /**
     * The name of the {@link org.openspaces.core.GigaSpace} bean. Used when more than one {@link org.openspaces.core.GigaSpace} is defined
     * and corresponds to the bean name / id.
     */
    String name() default "";
}