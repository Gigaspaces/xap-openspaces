package org.openspaces.core.gateway.config;

import java.util.List;

import org.openspaces.core.gateway.GatewaySinkFactoryBean;
import org.openspaces.core.gateway.GatewaySource;
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
public class GatewaySinkBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    public static final String LOCAL_GATEWAY_NAME = "local-gateway-name";
    public static final String GATEWAY_LOOKUPS = "gateway-lookups";
    public static final String LOCAL_SPACE_URL = "local-space-url";
    public static final String START_EMBEDDED_LUS = "start-embedded-lus";
    public static final String RELOCATE_IF_WRONG_PORTS = "relocate-if-wrong-ports";
    public static final String GATEWAY_SOURCE_NAME = "name";
    
    @Override
    protected Class<GatewaySinkFactoryBean> getBeanClass(Element element) {
        return GatewaySinkFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        
        String localGateywayName = element.getAttribute(LOCAL_GATEWAY_NAME);
        if (StringUtils.hasLength(localGateywayName))
            builder.addPropertyValue("localGatewayName", localGateywayName);
        
        String gatewayLookupsRef = element.getAttribute(GATEWAY_LOOKUPS);
        if (StringUtils.hasLength(gatewayLookupsRef))
            builder.addPropertyReference("gatewayLookups", gatewayLookupsRef);
        
        String localSpaceUrl = element.getAttribute(LOCAL_SPACE_URL);
        if (StringUtils.hasLength(localSpaceUrl))
            builder.addPropertyValue("localSpaceUrl", localSpaceUrl);
        
        String startEmbeddedLus = element.getAttribute(START_EMBEDDED_LUS);
        if (StringUtils.hasLength(startEmbeddedLus))
            builder.addPropertyValue("startEmbeddedLus", Boolean.parseBoolean(startEmbeddedLus));

        String relocateIfWrongPorts = element.getAttribute(RELOCATE_IF_WRONG_PORTS);
        if (StringUtils.hasLength(relocateIfWrongPorts))
            builder.addPropertyValue("relocateIfWrongPorts", Boolean.parseBoolean(relocateIfWrongPorts));
        
        List<Element> gatewaySourcesElement = DomUtils.getChildElementsByTagName(element, "sources");        

        if (gatewaySourcesElement != null && gatewaySourcesElement.size() > 0) {
            List<Element> gatewaySourcesElements = DomUtils.getChildElementsByTagName(
                    gatewaySourcesElement.get(0), "source");
            if (gatewaySourcesElements != null) {
                GatewaySource[] gatewaySources = new GatewaySource[gatewaySourcesElements.size()];
                for (int i = 0; i < gatewaySourcesElements.size(); i++) {
                    String gatewaySourceName = gatewaySourcesElements.get(i).getAttribute(GATEWAY_SOURCE_NAME);
                    gatewaySources[i] = new GatewaySource(gatewaySourceName);
                }
                builder.addPropertyValue("gatewaySources", gatewaySources);
            }
        }
        
    }
    
    

}
