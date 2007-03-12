package org.openspaces.events.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * @author kimchy
 */
public class AnnotationEventAdapterBeanDefinitionParser extends AbstractResultEventAdapterBeanDefinitionParser {

    public static final String DELEGATE = "delegate";

    protected Class getBeanClass(Element element) {
        return AnnotationEventAdapterFactoryBean.class;
    }


    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        Element delegateEle = DomUtils.getChildElementByTagName(element, DELEGATE);
        builder.addPropertyValue("delegate", parserContext.getDelegate().parsePropertyValue(delegateEle, builder.getRawBeanDefinition(), "delegate"));
    }
}
