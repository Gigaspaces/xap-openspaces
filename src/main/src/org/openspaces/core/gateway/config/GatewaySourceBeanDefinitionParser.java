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

import org.openspaces.core.gateway.GatewaySource;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * A bean definition parser for a {@link GatewaySource}.
 * 
 * @author idan
 * @since 8.0.3
 *
 */
public class GatewaySourceBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    public static final String GATEWAY_SOURCE_NAME = "name";
    
    @Override
    protected Class<GatewaySource> getBeanClass(Element element) {
        return GatewaySource.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        
        String gatewaySourceName = element.getAttribute(GATEWAY_SOURCE_NAME);
        if (StringUtils.hasLength(gatewaySourceName))
            builder.addPropertyValue("name", gatewaySourceName);
        
    }
    
}
