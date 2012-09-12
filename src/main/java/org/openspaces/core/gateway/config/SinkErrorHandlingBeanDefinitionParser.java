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

import org.openspaces.core.gateway.SinkErrorHandlingFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * A bean definition parser for {@link SinkErrorHandlingFactoryBean}.
 * 
 * @author idan
 * @since 8.0.3
 *
 */
public class SinkErrorHandlingBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    private static final String CONFLICT_RESOLVER = "conflict-resolver";
    private static final String MAXIMUM_RETRIES = "max-retries-on-tx-lock";
    private static final String RETRIES_INTERVAL = "tx-lock-retry-interval";
    
    @Override
    protected Class<SinkErrorHandlingFactoryBean> getBeanClass(Element element) {
        return SinkErrorHandlingFactoryBean.class;
    }
    
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        
        String conflictResolver = element.getAttribute(CONFLICT_RESOLVER);
        if (StringUtils.hasLength(conflictResolver))
            builder.addPropertyReference("conflictResolver", conflictResolver);
        
        String maximumRetries = element.getAttribute(MAXIMUM_RETRIES);
        if (StringUtils.hasLength(maximumRetries))
            builder.addPropertyValue("maximumRetriesOnTransactionLock", maximumRetries);

        String retriesInterval = element.getAttribute(RETRIES_INTERVAL);
        if (StringUtils.hasLength(retriesInterval))
            builder.addPropertyValue("transactionLockRetryInterval", retriesInterval);

    }
    
}
