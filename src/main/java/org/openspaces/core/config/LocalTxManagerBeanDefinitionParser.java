/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.core.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.transaction.manager.DistributedJiniTransactionManager;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
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
 * @deprecated
 */
@Deprecated
public class LocalTxManagerBeanDefinitionParser extends AbstractJiniTxManagerBeanDefinitionParser {

    protected final Log log = LogFactory.getLog(getClass());
    
    public static final String SPACE = "space";

    public static final String CLUSTERED = "clustered";

    protected Class<DistributedJiniTransactionManager> getBeanClass(Element element) {
        return DistributedJiniTransactionManager.class;
    }

    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        log.warn("Local transaction manager is deprecated, use distributed transaction manager instead ('distributed-tx-manager')");
        super.doParse(element, builder);
        NamedNodeMap attributes = element.getAttributes();
        for (int x = 0; x < attributes.getLength(); x++) {
            Attr attribute = (Attr) attributes.item(x);
            String name = attribute.getLocalName();
            if (ID_ATTRIBUTE.equals(name)) {
                continue;
            }
            String propertyName = extractPropertyName(name);
            if (SPACE.equals(name)) {
                continue;
            }
            if (CLUSTERED.equals(name)) {
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