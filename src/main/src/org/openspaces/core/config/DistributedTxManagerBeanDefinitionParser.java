package org.openspaces.core.config;

import org.openspaces.core.transaction.manager.LookupJiniTransactionManager;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.core.Conventions;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * A bean definition builder for {@link LookupJiniTransactionManager}.
 * 
 * @author kimchy
 */
public class DistributedTxManagerBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    public static final String TX_MANAGER_NAME = "tx-manager-name";

    protected Class<LookupJiniTransactionManager> getBeanClass(Element element) {
        return LookupJiniTransactionManager.class;
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
            if (TX_MANAGER_NAME.equals(name)) {
                builder.addPropertyValue("transactionManagerName", attribute.getValue());
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