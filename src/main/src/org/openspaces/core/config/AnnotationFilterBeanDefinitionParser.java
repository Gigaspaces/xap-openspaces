package org.openspaces.core.config;

import org.openspaces.core.space.filter.AnnotationFilterFactoryBean;
import org.w3c.dom.Element;

/**
 * @author kimchy
 */
public class AnnotationFilterBeanDefinitionParser extends AbstractFilterBeanDefinitionParser {

    protected Class getBeanClass(Element element) {
        return AnnotationFilterFactoryBean.class;
    }
}