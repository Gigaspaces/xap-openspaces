package org.openspaces.leader_selector.zookeeper.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author kobi on 11/23/15.
 * @since 11.0
 */
public class ZooKeeperNameSpaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("zookeeper", new ZooKeeperBeanDefinitionParser());
    }
}
