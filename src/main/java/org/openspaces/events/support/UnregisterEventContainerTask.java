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

package org.openspaces.events.support;

import org.openspaces.core.executor.Task;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * A task that unregisters (stops and disposes it) a dynamically added event container
 * (using {@link org.openspaces.events.polling.Polling} or {@link org.openspaces.events.notify.Notify}.
 * Returns <code>true</code> if the event container was found and unregistered correctly.
 *
 * @author kimchy
 */
public class UnregisterEventContainerTask implements Task<Boolean>, ApplicationContextAware {

    private static final long serialVersionUID = -6927651658526034507L;

    private transient ApplicationContext applicationContext;

    private String containerName;

    protected UnregisterEventContainerTask() {
    }

    public UnregisterEventContainerTask(String containerName) {
        this.containerName = containerName;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Boolean execute() throws Exception {
        EventContainersBus bus = AnnotationProcessorUtils.findBus(applicationContext);
        return bus.unregisterContainer(containerName);
    }
}
