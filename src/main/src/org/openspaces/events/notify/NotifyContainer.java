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

package org.openspaces.events.notify;

import com.j_spaces.core.client.INotifyDelegatorFilter;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an event listener as polled event listener. It will be wrapped automtically with
 * {@link SimpleNotifyEventListenerContainer}.
 *
 * <p>Template can be provided using {@link org.openspaces.events.EventTemplate} marked on
 * a general method that returns the template.
 *
 * <p>The event listener method should be marked with {@link org.openspaces.events.adapter.SpaceDataEvent}.
 *
 * @see org.openspaces.events.TransactionalEventContainer
 * @see org.openspaces.events.notify.NotifyBatch
 * @see org.openspaces.events.notify.NotifyLease
 * @see org.openspaces.events.notify.NotifyType
 * @author kimchy
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface NotifyContainer {

    /**
     * The name of the bean that that is the {@link org.openspaces.core.GigaSpace} this container will
     * used.
     *
     * <p>Note, this is optional. If there is a field of type {@link org.openspaces.core.GigaSpace} it
     * will be used. If there is none, and there is only one {@link org.openspaces.core.GigaSpace}
     * defined in the application context, it will be used.
     */
    String gigaSpace() default "";

    /**
     * @see SimpleNotifyEventListenerContainer#setPerformSnapshot(boolean)
     */
    boolean performSnapshot() default true;

    /**
     * @see SimpleNotifyEventListenerContainer#setPerformTakeOnNotify(boolean)
     */
    boolean performTakeOnNotify() default false;

    /**
     * @see SimpleNotifyEventListenerContainer#setIgnoreEventOnNullTake(boolean)
     */
    boolean ignoreEventOnNullTake() default false;

    /**
     * @see org.openspaces.events.notify.SimpleNotifyEventListenerContainer#setComType(int)
     */
    NotifyComType commType() default NotifyComType.UNICAST;

    /**
     * @see org.openspaces.events.notify.SimpleNotifyEventListenerContainer#setFifo(boolean)
     */
    boolean fifo() default false;

    /**
     * @see org.openspaces.events.notify.SimpleNotifyEventListenerContainer#setNotifyFilter(com.j_spaces.core.client.INotifyDelegatorFilter) 
     */
    Class<INotifyDelegatorFilter> notifyFilter() default INotifyDelegatorFilter.class;
}