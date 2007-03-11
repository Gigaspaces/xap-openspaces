package org.openspaces.core.config;

import org.openspaces.core.GigaSpaceFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.core.Conventions;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * @author kimchy
 */
public class GigaSpaceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    public static final String DEFAULT_ISOLATION = "default-isolation";

    public static final String SPACE = "space";

    protected Class getBeanClass(Element element) {
        return GigaSpaceFactoryBean.class;
    }

    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        NamedNodeMap attributes = element.getAttributes();
        for (int x = 0; x < attributes.getLength(); x++) {
            Attr attribute = (Attr) attributes.item(x);
            String name = attribute.getLocalName();
            if (ID_ATTRIBUTE.equals(name)) {
                continue;
            }
            String propertyName = extractPropertyName(name);
            if (DEFAULT_ISOLATION.equals(name)) {
                builder.addPropertyValue("defaultIsolationLevelName", GigaSpaceFactoryBean.PREFIX_ISOLATION + attribute.getValue());
                continue;
            }
            if (SPACE.equals(name)) {
                builder.addPropertyReference("space", attribute.getValue());
                continue;
            }

            Assert.state(StringUtils.hasText(propertyName),
                    "Illegal property name returned from 'extractPropertyName(String)': cannot be null or empty.");
            builder.addPropertyValue(propertyName, attribute.getValue());
        }
    }

    protected String extractPropertyName(String attributeName) {
        return Conventions.attributeNameToPropertyName(attributeName);
    }
}