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
package org.openspaces.core.bean;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.bean.BeanConfig;
import org.openspaces.admin.bean.BeanConfigManager;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.bean.BeanException;
import org.openspaces.admin.bean.BeanInitializationException;
import org.openspaces.admin.bean.BeanPropertiesManager;

/**
 * A simple bean construct with resemblance to a Spring bean (interfaces InitializingBean,
 * DisposableBean).
 * <p>
 * The administrative Bean is configured by either a strongly typed API (see {@link BeanConfig}), or
 * by a weakly typed String key-value pair property API (see {@link BeanPropertiesManager}). These
 * properties are supplied upon the bean's construction (see {@link #setProperties(Map)}).
 * <p>
 * A request to add a bean (see {@link BeanConfigManager#addBean(BeanConfig)}), will store the
 * configuration properties at the server until the bean is enabled (or removed).
 * <p>
 * A request to enable a bean (see {@link BeanConfigManager#enableBean(Class)}), will be accepted by
 * the bean factory - which initializes the bean, sets the properties and invokes a call to
 * {@link #afterPropertiesSet()}.
 * <p>
 * A request to disable a bean (see {@link BeanConfigManager#disableBean(Class)}), will destroy the
 * bean ({@link #destroy()}). The configuration properties will remain at the server until the bean
 * is completely removed (see {@link BeanConfigManager#removeBean(Class)}).
 * <p>
 * A request to set a bean with different properties (see
 * {@link BeanConfigManager#setBean(BeanConfig)}), will destroy the bean if it is already enabled,
 * and re-enable it with the new configuration properties. If the bean wasn't enabled, the
 * properties are stored at the server until the bean is enabled (or removed).
 * 
 * @author Moran Avigdor
 * @author Itai Frenkel
 * @since 8.0
 */
public interface Bean {
    
    /**
     * Set the Admin instance to be used by this Bean.
     * @param admin an Admin instance.
     */
	void setAdmin(Admin admin);

    /**
     * The bean properties supplied using a client side bean configuration object, or by plain
     * String key-value pairs.
     * 
     * @param properties
     *            properties to configure this bean.
     */
	void setProperties(Map<String, String> properties);
	
	/**
	 * @return properties used to configure this bean.
	 */
	Map<String, String> getProperties();

    /**
     * Invoked by a bean factory after it has set all bean properties. This method allows the bean
     * instance to perform non-blocking initialization, which is only possible when all bean properties have been
     * set and to throw an exception in the event of misconfiguration.
     * 
     * @throws Exception
     *             in the event of misconfiguration (such as failure to set an essential property)
     *             or if initialization fails.
     * @see BeanConfigurationException
     * @see BeanInitializationException
     */
	void afterPropertiesSet() throws Exception;

    /**
     * Invoked by a bean factory on destruction of a singleton.
     * 
     * @throws Exception
     *             in case of shutdown errors. Exceptions will get logged but not re-thrown to allow
     *             other beans to release their resources too.
     * @see BeanException
     */
	void destroy() throws Exception;
}
