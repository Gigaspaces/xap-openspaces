package org.openspaces.core.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Conventions;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * @author kimchy
 */
public abstract class AbstractFilterBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static final String FILTER = "filter";

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        NamedNodeMap attributes = element.getAttributes();
        for (int x = 0; x < attributes.getLength(); x++) {
            Attr attribute = (Attr) attributes.item(x);
            String name = attribute.getLocalName();
            if (ID_ATTRIBUTE.equals(name)) {
                continue;
            }
            String propertyName = extractPropertyName(name);
            Assert.state(StringUtils.hasText(propertyName),
                "Illegal property name returned from 'extractPropertyName(String)': cannot be null or empty.");
            builder.addPropertyValue(propertyName, attribute.getValue());
        }

        Element filterEle = DomUtils.getChildElementByTagName(element, FILTER);
        if (filterEle != null) {
            Object filter = parserContext.getDelegate().parsePropertyValue(filterEle, builder.getRawBeanDefinition(), "filter");
            builder.addPropertyValue("filter", filter);
        }
    }

    protected String extractPropertyName(String attributeName) {
        return Conventions.attributeNameToPropertyName(attributeName);
    }
}
