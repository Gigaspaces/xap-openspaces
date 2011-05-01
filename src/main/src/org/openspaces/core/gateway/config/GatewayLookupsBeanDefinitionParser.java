package org.openspaces.core.gateway.config;

import java.util.List;

import org.openspaces.core.gateway.GatewayLookup;
import org.openspaces.core.gateway.GatewayLookupsFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * 
 * @author Idan Moyal
 * @since 8.0.2
 *
 */
public class GatewayLookupsBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    public static final String LOOKUP_GROUP = "lookup-group";
    public static final String LOOKUP_GATEWAY_NAME = "gateway-name";
    public static final String LOOKUP_HOST = "host";
    public static final String LOOKUP_LUS_PORT = "lus-port";
    public static final String LOOKUP_LRMI_PORT = "lrmi-port";
    
    @Override
    protected Class<GatewayLookupsFactoryBean> getBeanClass(Element element) {
        return GatewayLookupsFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        
        String lookupGroup = element.getAttribute(LOOKUP_GROUP);
        builder.addPropertyValue("lookupGroup", lookupGroup);
        
        List<Element> gatewayLookupsElement = DomUtils.getChildElementsByTagName(element, "lookup");
        GatewayLookup[] gatewayLookups = new GatewayLookup[gatewayLookupsElement.size()];
        for (int i = 0; i < gatewayLookupsElement.size(); i++) {
            gatewayLookups[i] = new GatewayLookup();
            Element gatewayLookupElement = gatewayLookupsElement.get(i);
            
            String siteName = gatewayLookupElement.getAttribute(LOOKUP_GATEWAY_NAME);
            gatewayLookups[i].setGatewayName(siteName);
            
            String host = gatewayLookupElement.getAttribute(LOOKUP_HOST);
            gatewayLookups[i].setHost(host);
            
            String lusPort = gatewayLookupElement.getAttribute(LOOKUP_LUS_PORT);
            if (StringUtils.hasLength(lusPort))
                gatewayLookups[i].setLusPort(Integer.parseInt(lusPort));
            
            String lrmiPort = gatewayLookupElement.getAttribute(LOOKUP_LRMI_PORT);
            if (StringUtils.hasLength(lrmiPort))
                gatewayLookups[i].setLrmiPort(Integer.parseInt(lrmiPort));
        }
            
        builder.addPropertyValue("gatewayLookups", gatewayLookups);
    }
    
    

}
