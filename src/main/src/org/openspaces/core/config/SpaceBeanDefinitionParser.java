package org.openspaces.core.config;

import org.openspaces.core.space.UrlSpaceFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.Properties;

/**
 * @author kimchy
 */
public class SpaceBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    public static final String PARAMETERS = "parameters";

    public static final String PROPERTIES = "properties";

    public static final String URL_PROPERTIES = "url-properties";
    
    protected Class getBeanClass(Element element) {
        return UrlSpaceFactoryBean.class;
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        Element parametersEle = DomUtils.getChildElementByTagName(element, PARAMETERS);
        if (parametersEle != null) {
            Map parameters = parserContext.getDelegate().parseMapElement(parametersEle, builder.getRawBeanDefinition());
            builder.addPropertyValue("parameters", parameters);
        }
        Element propertiesEle = DomUtils.getChildElementByTagName(element, PROPERTIES);
        if (propertiesEle != null) {
            Properties properties = parserContext.getDelegate().parsePropsElement(propertiesEle);
            builder.addPropertyValue("properties", properties);
        }
        Element urlPropertiesEle = DomUtils.getChildElementByTagName(element, URL_PROPERTIES);
        if (urlPropertiesEle != null) {
            Properties properties = parserContext.getDelegate().parsePropsElement(urlPropertiesEle);
            builder.addPropertyValue("urlProperties", properties);
        }
    }
}
