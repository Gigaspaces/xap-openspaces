/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.core.config;

import org.openspaces.core.space.filter.replication.DefaultReplicationFilterProviderFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * A bean definition parser for {@link org.openspaces.core.space.filter.replication.DefaultReplicationFilterProviderFactory}.
 *
 * @author kimchy
 */
public class SpaceReplicationFilterBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    protected Class getBeanClass(Element element) {
        return DefaultReplicationFilterProviderFactory.class;
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        Element filterEle = DomUtils.getChildElementByTagName(element, "input-filter");
        if (filterEle != null) {
            Object filter = parserContext.getDelegate().parsePropertyValue(filterEle, builder.getRawBeanDefinition(), "input-filter");
            builder.addPropertyValue("inputFilter", filter);
        }

        filterEle = DomUtils.getChildElementByTagName(element, "output-filter");
        if (filterEle != null) {
            Object filter = parserContext.getDelegate().parsePropertyValue(filterEle, builder.getRawBeanDefinition(), "output-filter");
            builder.addPropertyValue("outputFilter", filter);
        }
    }
}
