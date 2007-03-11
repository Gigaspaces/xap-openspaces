package org.openspaces.core.config;

import org.openspaces.core.space.mode.SpaceModeContextLoader;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * @author kimchy
 */
public class ContextLoaderBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    protected Class getBeanClass(Element element) {
        return SpaceModeContextLoader.class;
    }
}
