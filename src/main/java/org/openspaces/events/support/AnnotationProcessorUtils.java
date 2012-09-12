/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.events.support;

import org.openspaces.core.GigaSpace;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @author kimchy
 */
public class AnnotationProcessorUtils {

    private AnnotationProcessorUtils() {

    }

    public static EventContainersBus findBus(ApplicationContext applicationContext) {
        Map eventContainerBuses = applicationContext.getBeansOfType(EventContainersBus.class);
        if (eventContainerBuses.isEmpty()) {
            throw new IllegalArgumentException("No event container bus found, can't register polling container");
        } else if (eventContainerBuses.size() > 1) {
            throw new IllegalArgumentException("Multiple event container buses found, can't register polling container");
        }
        return (EventContainersBus) eventContainerBuses.values().iterator().next();
    }

    public static GigaSpace findGigaSpace(final Object bean, String gigaSpaceName, ApplicationContext applicationContext, String beanName) throws IllegalArgumentException {
        GigaSpace gigaSpace = null;
        if (StringUtils.hasLength(gigaSpaceName)) {
            gigaSpace = (GigaSpace) applicationContext.getBean(gigaSpaceName);
            if (gigaSpace == null) {
                throw new IllegalArgumentException("Failed to lookup GigaSpace using name [" + gigaSpaceName + "] in bean [" + beanName + "]");
            }
            return gigaSpace;
        }

        Map gigaSpaces = applicationContext.getBeansOfType(GigaSpace.class);
        if (gigaSpaces.size() == 1) {
            return (GigaSpace) gigaSpaces.values().iterator().next();
        } 
        throw new IllegalArgumentException("Failed to resolve GigaSpace to use with event container, " +
                "[" + beanName + "] does not specifiy one, has no fields of that type, and there are more than one GigaSpace beans within the context");
    }

    public static PlatformTransactionManager findTxManager(String txManagerName, ApplicationContext applicationContext, String beanName) {
        PlatformTransactionManager txManager;
        if (StringUtils.hasLength(txManagerName)) {
            txManager = (PlatformTransactionManager) applicationContext.getBean(txManagerName);
            if (txManager == null) {
                throw new IllegalArgumentException("Failed to find transaction manager for name [" + txManagerName + "] in bean [" + beanName + "]");
            }
        } else {
            Map txManagers = applicationContext.getBeansOfType(PlatformTransactionManager.class);
            if (txManagers.size() == 1) {
                txManager = (PlatformTransactionManager) txManagers.values().iterator().next();
            } else {
                throw new IllegalArgumentException("More than one transaction manager defined in context, and no transactionManager specified, can't detect one");
            }
        }
        return txManager;
    }
}
