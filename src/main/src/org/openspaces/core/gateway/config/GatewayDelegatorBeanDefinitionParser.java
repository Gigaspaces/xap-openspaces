package org.openspaces.core.gateway.config;

import java.util.List;

import org.openspaces.core.gateway.GatewayDelegatorFactoryBean;
import org.openspaces.core.space.SecurityConfig;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * A bean definition parser for {@link GatewayDelegatorFactoryBean}.
 * 
 * @author idan
 * @since 8.0.3
 *
 */
public class GatewayDelegatorBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    public static final String LOCAL_GATEWAY_NAME = "local-gateway-name";
    public static final String GATEWAY_LOOKUPS = "gateway-lookups";
    public static final String START_EMBEDDED_LUS = "start-embedded-lus";
    public static final String RELOCATE_IF_WRONG_PORTS = "relocate-if-wrong-ports";
    public static final String DELEGATION_TARGET = "target";
    public static final String DELEGATION_DELEGATE_THROUGH = "delegate-through";
    public static final String CUSTOM_JVM_PROPERTIES = "custom-jvm-properties";
    private static final String COMMUNICATION_PORT = "communication-port";
    private static final String DELEGATIONS = "delegations";
    private static final String SECURITY = "security";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String USER_DETAILS = "user-details";
    private static final String DELEGATION = "delegation";
    
    @Override
    protected Class<GatewayDelegatorFactoryBean> getBeanClass(Element element) {
        return GatewayDelegatorFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        
        String localGateywayName = element.getAttribute(LOCAL_GATEWAY_NAME);
        if (StringUtils.hasLength(localGateywayName))
            builder.addPropertyValue("localGatewayName", localGateywayName);
        
        String gatewayLookupsRef = element.getAttribute(GATEWAY_LOOKUPS);
        if (StringUtils.hasLength(gatewayLookupsRef))
            builder.addPropertyReference("gatewayLookups", gatewayLookupsRef);
        
        String startEmbeddedLus = element.getAttribute(START_EMBEDDED_LUS);
        if (StringUtils.hasLength(startEmbeddedLus))
            builder.addPropertyValue("startEmbeddedLus", Boolean.parseBoolean(startEmbeddedLus));
        
        String relocateIfWrongPorts = element.getAttribute(RELOCATE_IF_WRONG_PORTS);
        if (StringUtils.hasLength(relocateIfWrongPorts))
            builder.addPropertyValue("relocateIfWrongPorts", Boolean.parseBoolean(relocateIfWrongPorts));

        String customJvmProperties = element.getAttribute(CUSTOM_JVM_PROPERTIES);
        if (StringUtils.hasLength(customJvmProperties))
            builder.addPropertyValue("customJvmProperties", customJvmProperties);

        String communicationPort = element.getAttribute(COMMUNICATION_PORT);
        if (StringUtils.hasLength(communicationPort))
            builder.addPropertyValue("communicationPort", communicationPort);

        // Security configuration - since 8.0.4
        final Element securityElement = DomUtils.getChildElementByTagName(element, SECURITY);
        if (securityElement != null) {
            String username = securityElement.getAttribute(USERNAME);
            String password = securityElement.getAttribute(PASSWORD);
            if (StringUtils.hasText(username)) {
                SecurityConfig securityConfig = new SecurityConfig();
                securityConfig.setUsername(username);
                if (StringUtils.hasText(password)) {
                    securityConfig.setPassword(password);
                }
                builder.addPropertyValue("securityConfig", securityConfig);
            }
            String userDetailsRef = securityElement.getAttribute(USER_DETAILS);
            if (StringUtils.hasText(userDetailsRef)) {
                builder.addPropertyReference("userDetails", userDetailsRef);
            }
        }
                
        // Using security and delegation in the same level is not allowed
        final Element deprecatedDelegationElement = DomUtils.getChildElementByTagName(element, DELEGATION);
        if (deprecatedDelegationElement != null) {
            if (securityElement != null)
                throw new IllegalArgumentException("delegation element should be set within a delegations element");
            final List<?> delegations = parserContext.getDelegate().parseListElement(element, builder.getRawBeanDefinition());
            builder.addPropertyValue("gatewayDelegations", delegations);
        }

        // Delegations - since 8.0.4
        final Element delegationsElement = DomUtils.getChildElementByTagName(element, DELEGATIONS);
        if (delegationsElement != null) {
            if (deprecatedDelegationElement != null)
                throw new IllegalArgumentException("delegation should be set within a delegations element");
            final List<?> gatewayDelegations = parserContext.getDelegate().parseListElement(delegationsElement, builder.getRawBeanDefinition());
            builder.addPropertyValue("gatewayDelegations", gatewayDelegations);
        }

    }

}
