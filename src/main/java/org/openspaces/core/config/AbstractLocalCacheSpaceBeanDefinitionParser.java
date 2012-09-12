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
 * A base class for local cache based beans.
 * 
 * @author kimchy
 */
public abstract class AbstractLocalCacheSpaceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    public static final String SPACE = "space";

    public static final String PROPERTIES = "properties";

    @Override
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
        
        String maxDisconnectionDuration = element.getAttribute("max-disconnection-duration");
        if (StringUtils.hasLength(maxDisconnectionDuration))
            builder.addPropertyValue("maxDisconnectionDuration", maxDisconnectionDuration);
        String batchSize = element.getAttribute("batch-size");
        if (StringUtils.hasLength(batchSize))
            builder.addPropertyValue("batchSize", batchSize);
        String batchTimeout = element.getAttribute("batch-timeout");
        if (StringUtils.hasLength(batchTimeout))
            builder.addPropertyValue("batchTimeout", batchTimeout);
    }
}
