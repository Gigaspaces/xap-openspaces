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

import org.openspaces.core.space.UrlSpaceFactoryBean;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

/**
 * A bean definition builder for {@link UrlSpaceFactoryBean}.
 *
 * @author kimchy
 */
public class UrlSpaceBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    public static final String DATA_SOURCE = "external-data-source";

    public static final String PARAMETERS = "parameters";

    public static final String PROPERTIES = "properties";

    public static final String URL_PROPERTIES = "url-properties";

    protected Class<UrlSpaceFactoryBean> getBeanClass(Element element) {
        return UrlSpaceFactoryBean.class;
    }

    protected boolean isEligibleAttribute(String attributeName) {
        return super.isEligibleAttribute(attributeName) && !DATA_SOURCE.equals(attributeName);
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        String dataSource = element.getAttribute(DATA_SOURCE);
        if (StringUtils.hasLength(dataSource)) {
            builder.addPropertyReference("externalDataSource", dataSource);
        }

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
        spaceFilterElements = DomUtils.getChildElementsByTagName(element, "filter-provider");
        for (Element ele : spaceFilterElements) {
            String refName = ele.getAttribute("ref");
            RuntimeBeanReference ref = new RuntimeBeanReference(refName, false);
            list.add(ref);
        }
        builder.addPropertyValue("filterProviders", list);
    }
}
