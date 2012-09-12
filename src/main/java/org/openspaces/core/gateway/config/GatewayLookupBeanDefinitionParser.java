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

import org.openspaces.core.gateway.GatewayLookup;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * A bean definition parser for {@link GatewayLookup}.
 * 
 * @author idan
 * @since 8.0.3
 *
 */
public class GatewayLookupBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    public static final String LOOKUP_GATEWAY_NAME = "gateway-name";
    public static final String LOOKUP_HOST = "host";
    public static final String LOOKUP_DISCOVERY_PORT = "discovery-port";
    public static final String COMMUNICATION_PORT = "communication-port";
    
    @Override
    protected Class<GatewayLookup> getBeanClass(Element element) {
        return GatewayLookup.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        
        String gatewayName = element.getAttribute(LOOKUP_GATEWAY_NAME);
        if (StringUtils.hasLength(gatewayName))
            builder.addPropertyValue("gatewayName", gatewayName);

        String host = element.getAttribute(LOOKUP_HOST);
        if (StringUtils.hasLength(host))
            builder.addPropertyValue("host", host);

        String discoveryPort = element.getAttribute(LOOKUP_DISCOVERY_PORT);
        if (StringUtils.hasLength(discoveryPort))
            builder.addPropertyValue("discoveryPort", discoveryPort);

        String lrmiPort = element.getAttribute(COMMUNICATION_PORT);
        if (StringUtils.hasLength(lrmiPort))
            builder.addPropertyValue("communicationPort", lrmiPort);

    }
    

}
