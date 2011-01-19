/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        registerBeanDefinitionParser("jini-tx-manager", new LookupJiniTxManagerBeanDefinitionParser());
        registerBeanDefinitionParser("distributed-tx-manager", new DistributedTxManagerBeanDefinitionParser());
        try {
            registerBeanDefinitionParser("giga-space-context", new GigaSpaceContextBeanDefinitionParser());
            registerBeanDefinitionParser("giga-space-late-context", new GigaSpaceLateContextBeanDefinitionParser());
        } catch (Throwable t) {
            // do nothing, working under 1.4
        }
        registerBeanDefinitionParser("context-loader", new ContextLoaderBeanDefinitionParser());
        registerBeanDefinitionParser("refreshable-context-loader", new RefreshableContextLoaderBeanDefinitionParser());

        registerBeanDefinitionParser("space-filter", new SpaceFilterBeanDefinitionParser());
        registerBeanDefinitionParser("annotation-adapter-filter", new AnnotationFilterBeanDefinitionParser());
        registerBeanDefinitionParser("method-adapter-filter", new MethodFilterBeanDefinitionParser());

        registerBeanDefinitionParser("space-replication-filter", new SpaceReplicationFilterBeanDefinitionParser());

        registerBeanDefinitionParser("map", new MapBeanDefinitionParser());
        registerBeanDefinitionParser("local-cache-support", new MapLocalCacheSettingsBeanDefinitionParser());
        registerBeanDefinitionParser("giga-map", new GigaMapBeanDefinitionParser());
        registerBeanDefinitionParser("annotation-support", new AnnotationSupportBeanDefinitionParser());
        registerBeanDefinitionParser("space-type", new GigaSpaceDocumentTypeBeanDefinitionParser());
        registerBeanDefinitionParser("mirror", new MirrorSpaceBeanDefinitionParser());
    }
}
