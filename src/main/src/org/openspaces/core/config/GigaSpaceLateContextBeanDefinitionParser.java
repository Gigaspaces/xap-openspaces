package org.openspaces.core.config;

import org.openspaces.core.context.GigaSpaceLateContextBeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * A bean definition builder for {@link org.openspaces.core.context.GigaSpaceContextBeanPostProcessor}.
 *
 * @author kimchy
 */
public class GigaSpaceLateContextBeanDefinitionParser implements BeanDefinitionParser {

    private boolean registered;

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        if (!this.registered) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(GigaSpaceLateContextBeanFactoryPostProcessor.class);
            builder.setSource(parserContext.extractSource(element));
            BeanDefinitionReaderUtils.registerWithGeneratedName(builder.getBeanDefinition(), parserContext.getRegistry());
            this.registered = true;
        }
        return null;
    }
}