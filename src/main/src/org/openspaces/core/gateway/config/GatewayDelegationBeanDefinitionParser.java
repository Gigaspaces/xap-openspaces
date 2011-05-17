package org.openspaces.core.gateway.config;

import org.openspaces.core.gateway.GatewayDelegation;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * A bean definition parser for a {@link GatewayDelegation}.
 * 
 * @author idan
 * @since 8.0.3
 *
 */
public class GatewayDelegationBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    public static final String DELEGATION_TARGET = "target";
    public static final String DELEGATION_DELEGATE_THROUGH = "delegate-through";
    
    @Override
    protected Class<GatewayDelegation> getBeanClass(Element element) {
        return GatewayDelegation.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        
        String target = element.getAttribute(DELEGATION_TARGET);
        if (StringUtils.hasLength(target))
            builder.addPropertyValue("target", target);
        
        String delegateThrough = element.getAttribute(DELEGATION_DELEGATE_THROUGH);
        if (StringUtils.hasLength(delegateThrough))
            builder.addPropertyValue("delegateThrough", delegateThrough);
        
    }
    

}
