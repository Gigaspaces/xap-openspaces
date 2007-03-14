package org.openspaces.core.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * A Spring name space handler for OpenSpaces core package.
 *
 * @author kimchy
 */
public class CoreNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("space", new UrlSpaceBeanDefinitionParser());
        registerBeanDefinitionParser("sql-query", new SQLQueryBeanDefinitionParser());
        registerBeanDefinitionParser("view-query", new ViewQueryBeanDefinitionParser());
        registerBeanDefinitionParser("local-cache", new LocalCacheSpaceBeanDefinitionParser());
        registerBeanDefinitionParser("local-view", new LocalViewSpaceBeanDefinitionParser());
        registerBeanDefinitionParser("giga-space", new GigaSpaceBeanDefinitionParser());
        registerBeanDefinitionParser("local-tx-manager", new LocalTxManagerBeanDefinitionParser());
        registerBeanDefinitionParser("distributed-tx-manager", new DistributedTxManagerBeanDefinitionParser());
        // TODO add distributed tx manager
        try {
            registerBeanDefinitionParser("giga-space-context", new GigaSpaceContextBeanDefinitionParser());
        } catch (Throwable t) {
            // do nothing, working under 1.4
        }
        registerBeanDefinitionParser("context-loader", new ContextLoaderBeanDefinitionParser());
    }
}
