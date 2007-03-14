package org.openspaces.core.config;

import org.openspaces.core.transaction.manager.LocalJiniTransactionManager;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.core.Conventions;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * A bean definition builder for {@link LocalJiniTransactionManager}.
 * 
 * @author kimchy
 */
public class LocalTxManagerBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    public static final String GIGA_SPACE = "giga-space";

    protected Class<LocalJiniTransactionManager> getBeanClass(Element element) {
        return LocalJiniTransactionManager.class;
    }

    // TODO allow for transactional context to be set as well
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        NamedNodeMap attributes = element.getAttributes();
        for (int x = 0; x < attributes.getLength(); x++) {
            Attr attribute = (Attr) attributes.item(x);
            String name = attribute.getLocalName();
            if (ID_ATTRIBUTE.equals(name)) {
                continue;
            }
            String propertyName = extractPropertyName(name);
            if (GIGA_SPACE.equals(name)) {
                builder.addPropertyReference(propertyName, attribute.getValue());
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