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

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * A bean definition builder for {@link SQLQueryFactoryBean}.
 * 
 * @author kimchy
 */
public class SQLQueryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static final String WHERE = "where";

    private static final String CLASS = "class";

    private static final String CLASS_NAME = "class-name";

    private static final String TEMPLATE = "template";

    protected Class<? extends SQLQueryFactoryBean> getBeanClass(Element element) {
        return SQLQueryFactoryBean.class;
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        String where = element.getAttribute(WHERE);
        builder.addPropertyValue("where", where);
        String clazz = element.getAttribute(CLASS);
        if (StringUtils.hasLength(clazz)) {
            builder.addPropertyValue("type", clazz);
        }
        String className = element.getAttribute(CLASS_NAME);
        if (StringUtils.hasLength(className)) {
            builder.addPropertyValue("className", className);
        }

        Element templateEle = DomUtils.getChildElementByTagName(element, TEMPLATE);
        if (templateEle != null) {
            Object template = parserContext.getDelegate().parsePropertyValue(templateEle, builder.getRawBeanDefinition(), "template");
            builder.addPropertyValue("template", template);
        }
    }
}
