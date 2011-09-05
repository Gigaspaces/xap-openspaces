package org.openspaces.core.gateway.config;

import java.util.List;

import org.openspaces.core.gateway.GatewaySinkFactoryBean;
import org.openspaces.core.space.SecurityConfig;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * A bean definition parser for {@link GatewaySinkFactoryBean}.
 * 
 * @author idan
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
    private static final String TRANSACTION_TIMEOUT = "tx-timeout";
    private static final String LOOKUP_TIMEOUT = "local-space-lookup-timeout";
    private static final String COMMUNICATION_PORT = "communication-port";
    private static final String TRANSACTION_SUPPORT = "tx-support";
    private static final String SECURITY = "security";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String USER_DETAILS = "user-details";
    
    @Override
    protected Class<GatewaySinkFactoryBean> getBeanClass(Element element) {
        return GatewaySinkFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        
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

        String transactionTimeout = element.getAttribute(TRANSACTION_TIMEOUT);
        if (StringUtils.hasLength(transactionTimeout))
            builder.addPropertyValue("transactionTimeout", transactionTimeout);

        String localSpaceLookupTimeout = element.getAttribute(LOOKUP_TIMEOUT);
        if (StringUtils.hasLength(localSpaceLookupTimeout))
            builder.addPropertyValue("localSpaceLookupTimeout", localSpaceLookupTimeout);

        String communicationPort = element.getAttribute(COMMUNICATION_PORT);
        if (StringUtils.hasLength(communicationPort))
            builder.addPropertyValue("communicationPort", communicationPort);
        
        Element gatewaySourcesElement = DomUtils.getChildElementByTagName(element, GATEWAY_SOURCES);        
        List<?> sources = parserContext.getDelegate().parseListElement(gatewaySourcesElement, builder.getRawBeanDefinition());
        builder.addPropertyValue("gatewaySources", sources);
        
        Element errorHandlingElement = DomUtils.getChildElementByTagName(element, ERROR_HANDLING);        
        if (errorHandlingElement != null) {
            Object errorHandlingConfiguration = parserContext.getDelegate().parsePropertySubElement(errorHandlingElement, builder.getRawBeanDefinition());
            builder.addPropertyValue("errorHandlingConfiguration", errorHandlingConfiguration);
        }
        
        // Distributed transaction processing parameters (since 8.0.4)
        final Element transactionProcessingConfigurationElement = DomUtils.getChildElementByTagName(element,
                TRANSACTION_SUPPORT);
        if (transactionProcessingConfigurationElement != null) {
            Object transactionProcessingConfiguration = parserContext.getDelegate().parsePropertySubElement(transactionProcessingConfigurationElement, builder.getRawBeanDefinition());
            builder.addPropertyValue("distributedTransactionProcessingConfiguration", transactionProcessingConfiguration);
        }
        
        // Security - since 8.0.4
        final Element securityElement = DomUtils.getChildElementByTagName(element, SECURITY);
        if (securityElement != null) {
            final String username = securityElement.getAttribute(USERNAME);
            final String password = securityElement.getAttribute(PASSWORD);
            if (StringUtils.hasText(username)) {
                SecurityConfig securityConfig = new SecurityConfig();
                securityConfig.setUsername(username);
                if (StringUtils.hasText(password)) {
                    securityConfig.setPassword(password);
                }
                builder.addPropertyValue("securityConfig", securityConfig);
            }
            final String userDetailsRef = securityElement.getAttribute(USER_DETAILS);
            if (StringUtils.hasText(userDetailsRef)) {
                builder.addPropertyReference("userDetails", userDetailsRef);
            }
        }
        
    }

}
