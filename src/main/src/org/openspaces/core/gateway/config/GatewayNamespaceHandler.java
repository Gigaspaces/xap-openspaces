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
        registerBeanDefinitionParser("lookups", new GatewayLookupsBeanDefinitionParser());
        registerBeanDefinitionParser("sink", new GatewaySinkBeanDefinitionParser());
        registerBeanDefinitionParser("delegator", new GatewayDelegatorBeanDefinitionParser());
    }

}
