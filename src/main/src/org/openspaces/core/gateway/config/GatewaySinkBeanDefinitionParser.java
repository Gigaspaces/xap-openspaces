package org.openspaces.core.gateway.config;

import java.util.List;

import org.openspaces.core.gateway.GatewaySinkFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * A bean definition parser for {@link GatewaySinkFactoryBean}.
 * 
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewaySinkBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    public static final String LOCAL_GATEWAY_NAME = "local-gateway-name";
    public static final String GATEWAY_LOOKUPS = "gateway-lookups";
    public static final String LOCAL_SPACE_URL = "local-space-url";
    public static final String START_EMBEDDED_LUS = "start-embedded-lus";
    public static final String RELOCATE_IF_WRONG_PORTS = "relocate-if-wrong-ports";
    public static final String GATEWAY_SOURCE_NAME = "name";
    public static final String REQUIRES_BOOTSTRAP = "requires-bootstrap";
    public static final String CUSTOM_JVM_PROPERTIES = "custom-jvm-properties";
    public static final String ERROR_HANDLING = "error-handling";
    public static final String GATEWAY_SOURCES = "sources";
    
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
        
        String requiresBootstrap = element.getAttribute(REQUIRES_BOOTSTRAP);
        if (StringUtils.hasLength(requiresBootstrap))
            builder.addPropertyValue("requiresBootstrap", requiresBootstrap);

        String customJvmProperties = element.getAttribute(CUSTOM_JVM_PROPERTIES);
        if (StringUtils.hasLength(customJvmProperties))
            builder.addPropertyValue("customJvmProperties", customJvmProperties);
        
        Element gatewaySourcesElement = DomUtils.getChildElementByTagName(element, GATEWAY_SOURCES);        
        List<?> sources = parserContext.getDelegate().parseListElement(gatewaySourcesElement, builder.getRawBeanDefinition());
        builder.addPropertyValue("gatewaySources", sources);
        
        Element errorHandlingElement = DomUtils.getChildElementByTagName(element, ERROR_HANDLING);        
        if (errorHandlingElement != null)
        {
            Object errorHandlingConfiguration = parserContext.getDelegate().parsePropertySubElement(errorHandlingElement, builder.getRawBeanDefinition());
            builder.addPropertyValue("errorHandlingConfiguration", errorHandlingConfiguration);
        }
        
    }

}
