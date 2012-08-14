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

import org.openspaces.core.space.cache.LocalViewSpaceFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

/**
 * A bean definition builder for {@link LocalViewSpaceFactoryBean}.
 * 
 * @author kimchy
 */
public class LocalViewSpaceBeanDefinitionParser extends AbstractLocalCacheSpaceBeanDefinitionParser {

    @Override
    protected Class<LocalViewSpaceFactoryBean> getBeanClass(Element element) {
        return LocalViewSpaceFactoryBean.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        List<Element> viewQueryElements = DomUtils.getChildElementsByTagName(element, "view-query");
        ManagedList list = new ManagedList(viewQueryElements.size());
        for (Element ele : viewQueryElements) {
            list.add(parserContext.getDelegate().parsePropertySubElement(ele, builder.getRawBeanDefinition(), null));
        }
        builder.addPropertyValue("localViews", list);        
        
        String initialSynchronizationTimeout = element.getAttribute("initial-synchronization-timeout");
        if (StringUtils.hasLength(initialSynchronizationTimeout))
            builder.addPropertyValue("initialSynchronizationTimeout", initialSynchronizationTimeout);
    }
}