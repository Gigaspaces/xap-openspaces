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

import org.openspaces.events.AbstractEventListenerContainer;
import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kimchy
 */
public class EventContainersBus implements DisposableBean {

    public static final String SUFFIX = "_eventContainer";

    private ConcurrentHashMap<String, AbstractEventListenerContainer> containers = new ConcurrentHashMap<String, AbstractEventListenerContainer>();

    public void registerContaienr(String name, AbstractEventListenerContainer container) {
        containers.put(name + SUFFIX, container);
    }

    public void unregisterContainer(String name) {
        AbstractEventListenerContainer container = containers.remove(name + SUFFIX);
        if (container != null) {
            container.destroy();
        }
    }

    public AbstractEventListenerContainer getEventContaienr(String name) {
        return containers.get(name + SUFFIX);
    }

    public void destroy() throws Exception {
        for (AbstractEventListenerContainer container : containers.values()) {
            container.destroy();
        }
        containers.clear();
    }
}
