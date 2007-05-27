package org.openspaces.core.config;

import org.openspaces.core.space.mode.SpaceModeContextLoader;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * A bean definition builder for {@link SpaceModeContextLoader}.
 * 
 * @author kimchy
 */
public class ContextLoaderBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    private static final String GIGA_SPACE = "giga-space";

    protected Class<SpaceModeContextLoader> getBeanClass(Element element) {
        return SpaceModeContextLoader.class;
    }

    protected boolean isEligibleAttribute(String attributeName) {
        return super.isEligibleAttribute(attributeName) && !GIGA_SPACE.equals(attributeName);
    }

    protected void postProcess(BeanDefinitionBuilder beanDefinition, Element element) {
        beanDefinition.addPropertyReference("gigaSpace", element.getAttribute(GIGA_SPACE));
    }
}
