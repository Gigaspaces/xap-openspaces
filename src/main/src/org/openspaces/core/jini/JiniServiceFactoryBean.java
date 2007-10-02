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

package org.openspaces.core.jini;

import net.jini.core.discovery.LookupLocator;
import net.jini.core.entry.Entry;
import net.jini.core.lookup.ServiceItem;
import net.jini.core.lookup.ServiceTemplate;
import net.jini.discovery.LookupDiscovery;
import net.jini.discovery.LookupDiscoveryManager;
import net.jini.lookup.ServiceDiscoveryManager;
import net.jini.lookup.entry.Name;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * JiniServiceFactoryBean for Jini environments. The class is made up from various samples found on
 * jini.org customized in a Spring specific way. The search will be executed using the provided
 * ServiceTemplate or, if it is null, one will be created using the serviceClass and serviceName. If
 * the lookup operation times out (30 seconds by default), a null service will be returned. For most
 * cases the serviceClass and serviceNames are enough and hide the jini details from the client.
 *
 * <p>
 * The factoryBean can be configured to do a lookup each time before returning the object type by
 * setting the "singleton" property to false.
 *
 * <p>
 * Initiallay taken from Spring modules.
 *
 * @author kimchy
 */
public class JiniServiceFactoryBean extends AbstractFactoryBean {

    private static final Log logger = LogFactory.getLog(JiniServiceFactoryBean.class);

    private ServiceTemplate template;

    // utility properties
    private Class<?> serviceClass;
    private String serviceName;

    private String[] groups = LookupDiscovery.ALL_GROUPS;

    private String[] locators = null;

    // 30 secs
    private long timeout = 30 * 1000;
    // used to pass out information from inner classes
    private Object proxy;

    public Class<?> getObjectType() {
        // try to discover the class type if possible to make it work with autowiring
        if (proxy == null) {
            // no template - look at serviceClass
            if (template == null) {
                return (serviceClass == null ? null : serviceClass);
            }

            // look at the template and
            // return the first class from the template (if there is one)
            if (template.serviceTypes != null && template.serviceTypes.length > 0) {
                return template.serviceTypes[0];
            }
        }
        if (proxy == null) {
            throw new IllegalArgumentException("Failed to identify factory class type");
        }
        return proxy.getClass();
    }

    protected Object createInstance() throws Exception {
        ServiceTemplate templ;

        if (template == null) {
            Class<?>[] types = (serviceClass == null ? null : new Class[]{serviceClass});
            Entry[] entry = (serviceName == null ? null : new Entry[]{new Name(serviceName)});

            templ = new ServiceTemplate(null, types, entry);
        } else {
            templ = template;
        }

        LookupLocator[] lookupLocators = null;
        if (locators != null) {
            lookupLocators = new LookupLocator[locators.length];
            for (int i = 0; i < locators.length; i++) {
                String locator = locators[i];
                if (!locator.startsWith("jini://")) {
                    locator = "jini://" + locator;
                }
                lookupLocators[i] = new LookupLocator(locator);
            }
        }
        LookupDiscoveryManager lookupDiscovery = null;
        ServiceDiscoveryManager serviceDiscovery = null;
        try {
            lookupDiscovery = new LookupDiscoveryManager(groups, lookupLocators, null);
            serviceDiscovery = new ServiceDiscoveryManager(lookupDiscovery, null);
            ServiceItem returnObject = serviceDiscovery.lookup(templ, null, timeout);
            if (returnObject != null) {
                proxy = returnObject.service;
            }
        } finally {
            if (serviceDiscovery != null) {
                try {
                    serviceDiscovery.terminate();
                } catch (Exception e) {
                    logger.warn("Failed to terminate service discovery, ignoring", e);
                }
            }
            if (lookupDiscovery != null) {
                try {
                    lookupDiscovery.terminate();
                } catch (Exception e) {
                    logger.warn("Failed to terminate lookup discovery, ignoring", e);
                }
            }
        }

        return proxy;
    }

    /**
     * Returns the groups.
     */
    public String[] getGroups() {
        return groups;
    }

    /**
     * The groups to set
     */
    public void setGroups(String[] groups) {
        this.groups = groups;
    }

    /**
     * Returns the locators.
     */
    public String[] getLocators() {
        return locators;
    }

    /**
     * Sets the locators.
     */
    public void setLocators(String[] locators) {
        this.locators = locators;
    }

    /**
     * @return Returns the serviceClass.
     */
    public Class<?> getServiceClass() {
        return serviceClass;
    }

    /**
     * @param serviceClass The serviceClass to set.
     */
    public void setServiceClass(Class<?> serviceClass) {
        this.serviceClass = serviceClass;
    }

    /**
     * @return Returns the serviceName.
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @param serviceName The serviceName to set.
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * @return Returns the template.
     */
    public ServiceTemplate getTemplate() {
        return template;
    }

    /**
     * @param template The template to set.
     */
    public void setTemplate(ServiceTemplate template) {
        this.template = template;
    }

    /**
     * @return Returns the timeout.
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * @param timeout The timeout to set.
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

}
