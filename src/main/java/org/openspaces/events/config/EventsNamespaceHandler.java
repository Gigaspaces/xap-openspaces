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

package org.openspaces.events.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author kimchy
 */
public class EventsNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("polling-container", new PollingContainerBeanDefinitionParser());
        registerBeanDefinitionParser("async-polling-container", new AsyncPollingContainerBeanDefinitionParser());
        registerBeanDefinitionParser("notify-container", new NotifyContainerBeanDefinitionParser());
        registerBeanDefinitionParser("annotation-adapter", new AnnotationEventAdapterBeanDefinitionParser());
        registerBeanDefinitionParser("method-adapter", new MethodEventAdapterBeanDefinitionParser());
        registerBeanDefinitionParser("annotation-support", new AnnotationSupportBeanDefinitionParser());
    }
}
