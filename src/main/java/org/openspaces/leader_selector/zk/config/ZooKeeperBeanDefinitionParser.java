package org.openspaces.leader_selector.zk.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author kobi on 11/23/15.
 * @since 11.0
 */
public class ZooKeeperBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    private static final String SESSION_TIMEOUT = "session-timeout";
    private static final String CONNECTION_TIMEOUT = "connection-timeout";

    @Override
    protected Class<ZooKeeperLeaderSelectorFactoryBean> getBeanClass(Element element) {
        return ZooKeeperLeaderSelectorFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

        final String sessionTimeout = element.getAttribute(SESSION_TIMEOUT);
        if (StringUtils.hasText(sessionTimeout))
            builder.addPropertyValue("sessionTimeout", sessionTimeout);
        else
            builder.addPropertyValue("sessionTimeout", "60000");

        final String connectionTimeout = element.getAttribute(CONNECTION_TIMEOUT);
        if (StringUtils.hasText(connectionTimeout))
            builder.addPropertyValue("connectionTimeout", connectionTimeout);
        else
            builder.addPropertyValue("connectionTimeout", "15000");
    }
}
