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
package org.openspaces.core.gateway.config;

import org.openspaces.core.transaction.config.DistributedTransactionProcessingConfigurationBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * A spring namespace handler for the "gateway" namespace.
 * 
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewayNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("targets", new GatewayTargetsBeanDefinitionParser());
        registerBeanDefinitionParser("target", new GatewayTargetBeanDefinitionParser());
        registerBeanDefinitionParser("lookups", new GatewayLookupsBeanDefinitionParser());
        registerBeanDefinitionParser("lookup", new GatewayLookupBeanDefinitionParser());
        registerBeanDefinitionParser("sink", new GatewaySinkBeanDefinitionParser());
        registerBeanDefinitionParser("source", new GatewaySourceBeanDefinitionParser());
        registerBeanDefinitionParser("delegator", new GatewayDelegatorBeanDefinitionParser());
        registerBeanDefinitionParser("delegation", new GatewayDelegationBeanDefinitionParser());
        registerBeanDefinitionParser("error-handling", new SinkErrorHandlingBeanDefinitionParser());
        registerBeanDefinitionParser("tx-support", new DistributedTransactionProcessingConfigurationBeanDefinitionParser());
    }

}
