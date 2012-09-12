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

import org.openspaces.core.context.GigaSpaceLateContextBeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * A bean definition builder for {@link org.openspaces.core.context.GigaSpaceContextBeanPostProcessor}.
 *
 * @author kimchy
 */
public class GigaSpaceLateContextBeanDefinitionParser implements BeanDefinitionParser {

    private boolean registered;

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        if (!this.registered) {
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(GigaSpaceLateContextBeanFactoryPostProcessor.class);
            builder.setSource(parserContext.extractSource(element));
            BeanDefinitionReaderUtils.registerWithGeneratedName(builder.getBeanDefinition(), parserContext.getRegistry());
            this.registered = true;
        }
        return null;
    }
}