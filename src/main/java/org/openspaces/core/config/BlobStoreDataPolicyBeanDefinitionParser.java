/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.core.config;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;


/**
 * 
 * @author Kobi
 * @since 10.0.0
 *
 */
public class BlobStoreDataPolicyBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

    private static final String OFFHEAP_CACHE_SIZE_PROPERTY = "offheap-cache-size";
    private static final String STORAGE_HANDLER = "blob-store-handler";

    @Override
    protected Class<BlobStoreDataPolicyFactoryBean> getBeanClass(Element element) {
        return BlobStoreDataPolicyFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        
        final String size = element.getAttribute(OFFHEAP_CACHE_SIZE_PROPERTY);
        if (StringUtils.hasText(size))
            builder.addPropertyValue("offHeapCacheSize", size);

        final String offheapStorageHandler = element.getAttribute(STORAGE_HANDLER);
        if (StringUtils.hasText(offheapStorageHandler))
            builder.addPropertyReference("blobStoreHandler", offheapStorageHandler);

        if(!StringUtils.hasText(offheapStorageHandler))
            throw new IllegalArgumentException("A reference to a space blob store handler bean must be specified");
    }    
    
}
