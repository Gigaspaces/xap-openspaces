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
