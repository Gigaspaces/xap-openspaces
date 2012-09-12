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

package org.openspaces.events;

import org.springframework.aop.support.AopUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple base class that provides support methods for Template based event listeners.
 *
 * <p>There are several ways a template can be provided. The first is by explicitly setting it using
 * {@link #setTemplate(Object)}. The second option is for the event listener to implement an interface
 * called {@link org.openspaces.events.EventTemplateProvider}. The last option is to annotate a method
 * within the event listener that will return the actual template using {@link org.openspaces.events.EventTemplate}
 * annotation.
 *
 * @author kimchy
 */
public abstract class AbstractTemplateEventListenerContainer extends AbstractEventListenerContainer {

    private Object template;

    private boolean performSnapshot = true;

    private Object receiveTemplate;

    @Override
    public void afterPropertiesSet() {
        if (template == null) {
            Class eventListenerType = getEventListenerClass();
            if (eventListenerType != null) {
                Object listener = getActualEventListener();

                // check if it implements an interface to provide the template
                if (EventTemplateProvider.class.isAssignableFrom(listener.getClass())) {
                    setTemplate(((EventTemplateProvider) getEventListener()).getTemplate());
                }

                // check if there is an annotation for it
                final AtomicReference<Method> ref = new AtomicReference<Method>();
                ReflectionUtils.doWithMethods(AopUtils.getTargetClass(listener), new ReflectionUtils.MethodCallback() {
                    public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                        if (method.isAnnotationPresent(EventTemplate.class)) {
                            ref.set(method);
                        }
                    }
                });
                if (ref.get() != null) {
                    ref.get().setAccessible(true);
                    try {
                        setTemplate(ref.get().invoke(listener));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Failed to get template from method [" + ref.get().getName() + "]", e);
                    }
                }
            }
        }
        super.afterPropertiesSet();
    }

    @Override
    public void initialize() throws DataAccessException {
        if (performSnapshot && template != null) {
            if (logger.isTraceEnabled()) {
                logger.trace(message("Performing snapshot on template [" + template + "]"));
            }
            receiveTemplate = getGigaSpace().snapshot(template);
        } else {
            receiveTemplate = template;
        }

        super.initialize();
    }

    /**
     * Sets the specified template to be used with the polling space operation.
     *
     * @see org.openspaces.core.GigaSpace#take(Object,long)
     */
    public void setTemplate(Object template) {
        this.template = template;
    }

    /**
     * Returns the template that will be used. Note, in order to perform receive operations, the
     * {@link #getReceiveTemplate()} should be used.
     */
    protected Object getTemplate() {
        return this.template;
    }

    /**
     * Returns the template to be used for receive operations. If
     * {@link #setPerformSnapshot(boolean)} is set to <code>true</code> (the default)
     * will return the snapshot of the provided template.
     */
    protected Object getReceiveTemplate() {
        return receiveTemplate;
    }

    /**
     * If set to <code>true</code> will perform snapshot operation on the provided template
     * before invoking registering as an event listener.
     *
     * @see org.openspaces.core.GigaSpace#snapshot(Object)
     */
    public void setPerformSnapshot(boolean performSnapshot) {
        this.performSnapshot = performSnapshot;
    }

    protected boolean isPerformSnapshot() {
        return this.performSnapshot;
    }
}
