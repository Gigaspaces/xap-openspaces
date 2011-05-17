package org.openspaces.core.gateway.config;

import org.openspaces.core.gateway.GatewayTarget;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * 
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewayTargetBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {
    
    public static final String TARGET_NAME = "name";
    
    @Override
    protected Class<GatewayTarget> getBeanClass(Element element) {
        return GatewayTarget.class;
    }

    @Override
    protected boolean shouldGenerateId() {
        return true;
    }
    
    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        String targetName = element.getAttribute(TARGET_NAME);
        if (StringUtils.hasLength(targetName))
            builder.addPropertyValue("name", targetName);
        
    }

}
