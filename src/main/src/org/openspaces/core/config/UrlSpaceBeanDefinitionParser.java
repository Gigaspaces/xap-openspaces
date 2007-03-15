package org.openspaces.core.config;

import org.openspaces.core.space.UrlSpaceFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

/**
 * A bean definition builder for {@link UrlSpaceFactoryBean}.
 * 
 * @author kimchy
 */
public class UrlSpaceBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    public static final String PARAMETERS = "parameters";

    public static final String PROPERTIES = "properties";

    public static final String URL_PROPERTIES = "url-properties";

    protected Class<UrlSpaceFactoryBean> getBeanClass(Element element) {
        return UrlSpaceFactoryBean.class;
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        Element parametersEle = DomUtils.getChildElementByTagName(element, PARAMETERS);
        if (parametersEle != null) {
            Object parameters = parserContext.getDelegate().parsePropertyValue(parametersEle,
                builder.getRawBeanDefinition(), "parameters");
            builder.addPropertyValue("parameters", parameters);
        }
        Element propertiesEle = DomUtils.getChildElementByTagName(element, PROPERTIES);
        if (propertiesEle != null) {
            Object properties = parserContext.getDelegate().parsePropertyValue(propertiesEle,
                builder.getRawBeanDefinition(), "properties");
            builder.addPropertyValue("properties", properties);
        }
        Element urlPropertiesEle = DomUtils.getChildElementByTagName(element, URL_PROPERTIES);
        if (urlPropertiesEle != null) {
            Object properties = parserContext.getDelegate().parsePropertyValue(urlPropertiesEle,
                builder.getRawBeanDefinition(), "urlProperties");
            builder.addPropertyValue("urlProperties", properties);
        }
        
        List<Element> spaceFilterElements = DomUtils.getChildElementsByTagName(element, "space-filter");
        ManagedList list = new ManagedList();
        for (Element ele : spaceFilterElements) {
            list.add(parserContext.getDelegate().parsePropertySubElement(ele, builder.getRawBeanDefinition()));
        }
        spaceFilterElements = DomUtils.getChildElementsByTagName(element, "annotation-adapter-filter");
        for (Element ele : spaceFilterElements) {
            list.add(parserContext.getDelegate().parsePropertySubElement(ele, builder.getRawBeanDefinition(), null));
        }
        spaceFilterElements = DomUtils.getChildElementsByTagName(element, "method-adapter-filter");
        for (Element ele : spaceFilterElements) {
            list.add(parserContext.getDelegate().parsePropertySubElement(ele, builder.getRawBeanDefinition(), null));
        }
        builder.addPropertyValue("filterProviders", list);
    }
}
