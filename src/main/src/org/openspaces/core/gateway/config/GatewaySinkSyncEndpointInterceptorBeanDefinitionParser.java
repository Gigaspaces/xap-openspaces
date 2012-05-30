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

import org.openspaces.core.gateway.GatewaySinkSyncEndpointInterceptorFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * A bean definition parser for {@link GatewaySinkSyncEndpointInterceptorFactoryBean}.
 * 
 * @author eitany
 * @since 9.0.1
 */
public class GatewaySinkSyncEndpointInterceptorBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser{

    private static final String INTERCEPTOR = "interceptor";
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser#getBeanClass(org.w3c.dom.Element)
     */
    @Override
    protected Class<GatewaySinkSyncEndpointInterceptorFactoryBean> getBeanClass(Element element) {
        return GatewaySinkSyncEndpointInterceptorFactoryBean.class;
    }
    
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        
        String interceptor = element.getAttribute(INTERCEPTOR);
        if (StringUtils.hasLength(interceptor))
            builder.addPropertyReference("interceptor", interceptor);
        
    }

}
