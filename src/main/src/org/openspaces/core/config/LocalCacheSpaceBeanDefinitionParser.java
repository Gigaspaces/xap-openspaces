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

import org.openspaces.core.space.cache.LocalCacheSpaceFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * A bean definition builder for {@link LocalCacheSpaceFactoryBean}.
 * 
 * @author kimchy
 */
public class LocalCacheSpaceBeanDefinitionParser extends AbstractLocalCacheSpaceBeanDefinitionParser {

    public static final String UPDATE_MODE = "update-mode";

    protected Class<LocalCacheSpaceFactoryBean> getBeanClass(Element element) {
        return LocalCacheSpaceFactoryBean.class;
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        String updateMode = element.getAttribute(UPDATE_MODE);
        if (StringUtils.hasLength(updateMode)) {
            builder.addPropertyValue("updateModeName", updateMode);
        }

        String size = element.getAttribute("size");
        if (StringUtils.hasLength(size)) {
            builder.addPropertyValue("size", Integer.parseInt(size));
        }
    }

}
