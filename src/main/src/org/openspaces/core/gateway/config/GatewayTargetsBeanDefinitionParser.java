package org.openspaces.core.gateway.config;

import java.util.List;

import org.openspaces.core.gateway.GatewayTargetsFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * A bean definition parser for {@link GatewayTargetsFactoryBean}.
 * 
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewayTargetsBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    public static final String LOCAL_GATEWAY_NAME = "local-gateway-name";
    public static final String TARGET_NAME = "name";
    
    @Override
    protected Class<GatewayTargetsFactoryBean> getBeanClass(Element element) {
        return GatewayTargetsFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        
        String localSiteName = element.getAttribute(LOCAL_GATEWAY_NAME);
        builder.addPropertyValue("localGatewayName", localSiteName);
        
        GatewayTargetBeanDefinitionParser.parseGatewayTargetAttributes(element, builder);
        
        List<?> targets = parserContext.getDelegate().parseListElement(element, builder.getRawBeanDefinition());
        builder.addPropertyValue("gatewayTargets", targets);
    }

}
