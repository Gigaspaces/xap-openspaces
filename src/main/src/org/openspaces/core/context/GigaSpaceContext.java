package org.openspaces.core.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to directly inject a {@link org.openspaces.core.GigaSpace} implementation into a
 * class field or setter property. A name can be specified in cases where more than one
 * {@link org.openspaces.core.GigaSpace} are defined within the processing unit. The name
 * will be the bean name.
 *
 * @author kimchy
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GigaSpaceContext {

    /**
     * The name of the {@link org.openspaces.core.GigaSpace} bean. Used when more
     * than one {@link org.openspaces.core.GigaSpace} is defined.
     */
    String name() default "";
}
