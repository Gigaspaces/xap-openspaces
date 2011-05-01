package org.openspaces.core.gateway.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * 
 * @author Idan Moyal
 * @since 8.0.2
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
