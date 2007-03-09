package org.openspaces.core.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author kimchy
 */
public class CoreNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("space", new SpaceBeanDefinitionParser());
    }
}
