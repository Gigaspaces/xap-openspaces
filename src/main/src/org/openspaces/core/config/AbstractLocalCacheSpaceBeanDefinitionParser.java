package org.openspaces.core.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * A base class for local cache based beans.
 * 
 * @author kimchy
 */
public abstract class AbstractLocalCacheSpaceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    public static final String SPACE = "space";

    public static final String PROPERTIES = "properties";

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        String space = element.getAttribute(SPACE);
        if (StringUtils.hasLength(space)) {
            builder.addPropertyReference("space", space);
        }
        Element propertiesEle = DomUtils.getChildElementByTagName(element, PROPERTIES);
        if (propertiesEle != null) {
            Object properties = parserContext.getDelegate().parsePropertyValue(propertiesEle,
                builder.getRawBeanDefinition(), "properties");

            builder.addPropertyValue("properties", properties);
        }
    }
}
