package org.openspaces.core.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openspaces.core.GigaSpace;

/**
 * Allows to directly inject a {@link GigaSpace} implementation into a class field or setter
 * property. A name can be specified in cases where more than one {@link GigaSpace} are defined
 * within a spring application context. The name will be the bean name / id.
 * 
 * @author kimchy
 */
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface GigaSpaceContext {

    /**
     * The name of the {@link GigaSpace} bean. Used when more than one {@link GigaSpace} is defined
     * and corresponds to the bean name / id.
     */
    String name() default "";
}
