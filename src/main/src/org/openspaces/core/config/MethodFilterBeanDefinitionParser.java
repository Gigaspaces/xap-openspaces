package org.openspaces.core.config;

import org.openspaces.core.space.filter.MethodFilterFactoryBean;
import org.w3c.dom.Element;

/**
 * @author kimchy
 */
public class MethodFilterBeanDefinitionParser extends AbstractFilterBeanDefinitionParser {

    protected Class getBeanClass(Element element) {
        return MethodFilterFactoryBean.class;
    }
}