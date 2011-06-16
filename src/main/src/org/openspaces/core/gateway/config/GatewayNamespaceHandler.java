package org.openspaces.core.gateway.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * A spring namespace handler for the "gateway" namespace.
 * 
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewayNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("targets", new GatewayTargetsBeanDefinitionParser());
        registerBeanDefinitionParser("target", new GatewayTargetBeanDefinitionParser());
        registerBeanDefinitionParser("lookups", new GatewayLookupsBeanDefinitionParser());
        registerBeanDefinitionParser("lookup", new GatewayLookupBeanDefinitionParser());
        registerBeanDefinitionParser("sink", new GatewaySinkBeanDefinitionParser());
        registerBeanDefinitionParser("source", new GatewaySourceBeanDefinitionParser());
        registerBeanDefinitionParser("delegator", new GatewayDelegatorBeanDefinitionParser());
        registerBeanDefinitionParser("delegation", new GatewayDelegationBeanDefinitionParser());
        registerBeanDefinitionParser("error-handling", new SinkErrorHandlingBeanDefinitionParser());
    }

}
