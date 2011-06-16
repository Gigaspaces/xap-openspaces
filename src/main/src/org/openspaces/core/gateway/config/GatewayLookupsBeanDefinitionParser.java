package org.openspaces.core.gateway.config;

import java.util.List;

import org.openspaces.core.gateway.GatewayLookupsFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * A bean definition parser for {@link GatewayLookupsFactoryBean}.
 * 
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewayLookupsBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    private static final String LOOKUP_GROUP = "lookup-group";
    
    @Override
    protected Class<GatewayLookupsFactoryBean> getBeanClass(Element element) {
        return GatewayLookupsFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        
        String lookupGroup = element.getAttribute(LOOKUP_GROUP);
        if (StringUtils.hasLength(lookupGroup))
            builder.addPropertyValue("lookupGroup", lookupGroup);

        List<?> lookups = parserContext.getDelegate().parseListElement(element, builder.getRawBeanDefinition());
        builder.addPropertyValue("gatewayLookups", lookups);
    }
    
    

}
