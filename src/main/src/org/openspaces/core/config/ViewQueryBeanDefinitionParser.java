package org.openspaces.core.config;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author kimchy
 */
public class ViewQueryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    public static final String WHERE = "where";

    public static final String CLASS = "class";

    public static final String CLASS_NAME = "class-name";

    public static final String TEMPLATE = "template";

    protected Class getBeanClass(Element element) {
        return ViewQueryFactoryBean.class;
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        String where = element.getAttribute(WHERE);
        builder.addPropertyValue("where", where);
        String clazz = element.getAttribute(CLASS);
        if (clazz != null) {
            builder.addPropertyValue("type", clazz);
        }
        String className = element.getAttribute(CLASS_NAME);
        if (className != null) {
            builder.addPropertyValue("className", className);
        }

        Element templateEle = DomUtils.getChildElementByTagName(element, TEMPLATE);
        if (templateEle != null) {
            BeanDefinitionHolder bdHolder = parserContext.getDelegate().parseBeanDefinitionElement(templateEle, builder.getRawBeanDefinition());
            if (bdHolder != null) {
                bdHolder = parserContext.getDelegate().decorateBeanDefinitionIfRequired(templateEle, bdHolder);
                builder.addPropertyValue("template", bdHolder);
            }
        }
    }
}