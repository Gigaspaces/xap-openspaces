/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.core.gateway.config;

import org.openspaces.core.gateway.GatewaySinkDistributedTransactionProcessingConfigurationFactoryBean;
import org.openspaces.core.transaction.config.DistributedTransactionProcessingConfigurationBeanDefinitionParser;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * A bean definition parser for distributed transaction support configuration for gateway sink component.
 * 
 * @author eitany
 * @since 9.0.1
 */
public class GatewaySinkDistributedTransactionProcessingConfigurationBeanDefinitionParser extends
        DistributedTransactionProcessingConfigurationBeanDefinitionParser {

    final private static String CONSOLIDATION_FAILURE_ACTION = "dist-tx-consolidation-failure-action";

    /* (non-Javadoc)
     * @see org.openspaces.core.transaction.config.DistributedTransactionProcessingConfigurationBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
     */
    @Override
    protected Class<GatewaySinkDistributedTransactionProcessingConfigurationFactoryBean> getBeanClass(Element element) {
        return GatewaySinkDistributedTransactionProcessingConfigurationFactoryBean.class;
    }
    
    /* (non-Javadoc)
     * @see org.openspaces.core.transaction.config.DistributedTransactionProcessingConfigurationBeanDefinitionParser#doParse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext, org.springframework.beans.factory.support.BeanDefinitionBuilder)
     */
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
     
        super.doParse(element, parserContext, builder);
        
        final String distributedTransactionConsolidationFailureAction = element.getAttribute(CONSOLIDATION_FAILURE_ACTION);
        if (StringUtils.hasLength(distributedTransactionConsolidationFailureAction))
            builder.addPropertyValue("distributedTransactionConsolidationFailureAction", distributedTransactionConsolidationFailureAction);
    }
    
}
