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

import java.util.List;

import org.openspaces.core.config.xmlparser.SecurityDefinitionsParser;
import org.openspaces.core.space.AllInCachePolicy;
import org.openspaces.core.space.CachePolicy;
import org.openspaces.core.space.LruCachePolicy;
import org.openspaces.core.space.UrlSpaceFactoryBean;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

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
    
    public static final String GATEWAY_TARGETS = "gateway-targets";
    
    public static final String REGISTER_FOR_SPACE_MODE_EVENTS = "register-for-space-mode-notifications";

    private static final String SPACE_DATA_SOURCE = "space-data-source";
    private static final String SPACE_SYNC_ENDPOINT = "space-sync-endpoint";
    
    @Override
    protected Class<UrlSpaceFactoryBean> getBeanClass(Element element) {
        return UrlSpaceFactoryBean.class;
    }

    @Override
    protected boolean isEligibleAttribute(String attributeName) {
        return super.isEligibleAttribute(attributeName) && !DATA_SOURCE.equals(attributeName)
                && !SPACE_DATA_SOURCE.equals(attributeName) && !SPACE_SYNC_ENDPOINT.equals(attributeName);
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);

        String dataSource = element.getAttribute(DATA_SOURCE);
        if (StringUtils.hasLength(dataSource)) {
            builder.addPropertyReference("externalDataSource", dataSource);
        }
        
        String spaceDataSource = element.getAttribute(SPACE_DATA_SOURCE);        
        if (StringUtils.hasLength(spaceDataSource)) {
            builder.addPropertyReference("spaceDataSource", spaceDataSource);
        }

        String spaceSynchronizationEndpoint = element.getAttribute(SPACE_SYNC_ENDPOINT);
        if (StringUtils.hasLength(spaceSynchronizationEndpoint)) {
            builder.addPropertyReference("spaceSynchronizationEndpoint", spaceSynchronizationEndpoint);
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

        Element replicationFilterProviderEle = DomUtils.getChildElementByTagName(element, "replication-filter-provider");
        if (replicationFilterProviderEle != null) {
            String refName = replicationFilterProviderEle.getAttribute("ref");
            RuntimeBeanReference ref = new RuntimeBeanReference(refName, false);
            builder.addPropertyValue("replicationFilterProvider", ref);
        }

        Element spaceReplicationFilterEle = DomUtils.getChildElementByTagName(element, "space-replication-filter");
        if (spaceReplicationFilterEle != null) {
            builder.addPropertyValue("replicationFilterProvider", parserContext.getDelegate().parsePropertySubElement(spaceReplicationFilterEle, builder.getRawBeanDefinition(), null));
        }

        Element securityEle = DomUtils.getChildElementByTagName(element, "security");
        if (securityEle != null) {
            SecurityDefinitionsParser.parseXml(securityEle, builder);
            String secured = securityEle.getAttribute("secured");
            if (StringUtils.hasText(secured)) {
                builder.addPropertyValue("secured", Boolean.parseBoolean(secured));
            }
        }

        CachePolicy cachePolicy = null;
        Element allInCacheEle = DomUtils.getChildElementByTagName(element, "all-in-cache-policy");
        if (allInCacheEle != null) {
            cachePolicy = new AllInCachePolicy();
        }
        Element lruCacheEle = DomUtils.getChildElementByTagName(element, "lru-cache-policy");
        if (lruCacheEle != null) {
            cachePolicy = new LruCachePolicy();
            String size = lruCacheEle.getAttribute("size");
            if (StringUtils.hasText(size)) {
                ((LruCachePolicy) cachePolicy).setSize(Integer.parseInt(size));
            }
            String initialLoadPercentage = lruCacheEle.getAttribute("initialLoadPercentage");
            if (StringUtils.hasText(initialLoadPercentage)) {
                ((LruCachePolicy) cachePolicy).setInitialLoadPercentage(Integer.parseInt(initialLoadPercentage));
            }
        }
        Element customCacheEle = DomUtils.getChildElementByTagName(element, "custom-cache-policy");
        if (customCacheEle != null) {
            Object customCachePolicyFactoryBean = parserContext.getDelegate().parsePropertySubElement(customCacheEle, builder.getRawBeanDefinition());
            builder.addPropertyValue("customCachePolicy", customCachePolicyFactoryBean);
        }
        
        if (cachePolicy != null) {
            builder.addPropertyValue("cachePolicy", cachePolicy);
        }
        
        List<Element> documentTypeElements = DomUtils.getChildElementsByTagName(element, "space-type");
        ManagedList typesList = new ManagedList();
        for (Element ele : documentTypeElements) {
            typesList.add(parserContext.getDelegate().parsePropertySubElement(ele, builder.getRawBeanDefinition()));
        }
        
        builder.addPropertyValue("spaceTypes", typesList);
        
        String gatewayTargetsRef = element.getAttribute(GATEWAY_TARGETS);
        if (StringUtils.hasLength(gatewayTargetsRef))
            builder.addPropertyReference("gatewayTargets", gatewayTargetsRef);
        
        String registerForSpaceModeEvents = element.getAttribute(REGISTER_FOR_SPACE_MODE_EVENTS);
        if (StringUtils.hasLength(registerForSpaceModeEvents))
            builder.addPropertyValue("registerForSpaceModeNotifications", registerForSpaceModeEvents);
    }

}
