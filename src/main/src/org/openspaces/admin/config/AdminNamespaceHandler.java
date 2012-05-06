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
package org.openspaces.admin.config;

import org.openspaces.admin.application.config.ApplicationConfig;
import org.openspaces.admin.internal.pu.elastic.config.ElasticStatefulProcessingUnitConfig;
import org.openspaces.admin.memcached.config.MemcachedConfig;
import org.openspaces.admin.pu.config.ProcessingUnitConfig;
import org.openspaces.admin.pu.dependency.config.ProcessingUnitDependencyConfig;
import org.openspaces.admin.pu.elastic.config.ElasticStatelessProcessingUnitConfig;
import org.openspaces.admin.space.ElasticSpaceConfig;
import org.openspaces.admin.space.config.SpaceConfig;
import org.openspaces.core.config.xmlparser.XmlBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * A spring namespace handler for the "admin" namespace.
 * 
 * @author itaif
 * @since 9.0.1
 * @see openspaces-admin.xsd
 *
 */
public class AdminNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        register("depends-on", ProcessingUnitDependencyConfig.class);
        register("pu", ProcessingUnitConfig.class);
        register("space", SpaceConfig.class);
        register("elastic-stateful-pu", ElasticStatefulProcessingUnitConfig.class);
        register("elastic-stateless-pu", ElasticStatelessProcessingUnitConfig.class);
        register("elastic-space", ElasticSpaceConfig.class);
        register("memcached",MemcachedConfig.class);
        register("application", ApplicationConfig.class);
    }
    
    private void register(String elementName, Class<?> clazz) {
        registerBeanDefinitionParser(elementName, new XmlBeanDefinitionParser(clazz));
    }

}
