/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.remoting.scripting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows to inject a {@link org.openspaces.remoting.scripting.ScriptingExecutor} remoting proxy using
 * {@link org.openspaces.remoting.EventDrivenSpaceRemotingProxyFactoryBean}.
 *
 * @author kimchy
 * @see org.openspaces.remoting.RemotingAnnotationBeanPostProcessor
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventDrivenScriptingExecutor {

    /**
     * The name of the {@link org.openspaces.core.GigaSpace} instance (representing the Space)
     * that this remote invocation will occur on.
     *
     * <p>If there is only one instance of {@link org.openspaces.core.GigaSpace}, will defualt to
     * it. If not, will throw an exception if not defined.
     */
    public abstract String gigaSpace() default "";

    /**
     * The timeout value when using this async call in a sync manner.
     *
     * @see org.openspaces.remoting.EventDrivenSpaceRemotingProxyFactoryBean#setTimeout(long)
     */
    public abstract long timeout() default 5000;

    /**
     * Should the remote invocation operate in a fifo manner.
     */
    public abstract boolean fifo() default false;
}