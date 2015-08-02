package org.openspaces.core.executor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Barak Bar Orion
 * 6/24/15.
 * Mark a DistributedTask to be garbage after usage.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneTimeTask {
}
