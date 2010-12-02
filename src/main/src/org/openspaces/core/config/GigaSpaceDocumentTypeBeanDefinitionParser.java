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

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Conventions;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.gigaspaces.metadata.index.SpaceIndexType;

/**
 * Defines space-type tag parsing
 * @author anna
 */
public class GigaSpaceDocumentTypeBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    protected Class getBeanClass(Element element) {
        return GigaSpaceDocumentTypeDescriptorFactoryBean.class;
    }

    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        NamedNodeMap attributes = element.getAttributes();
        
        String typeName = null;
        String superType = null;
        for (int x = 0; x < attributes.getLength(); x++) {
            Attr attribute = (Attr) attributes.item(x);
            String name = attribute.getLocalName();
            if (ID_ATTRIBUTE.equals(name)) {
                continue;
            }
            String propertyName = extractPropertyName(name);
            Assert.state(StringUtils.hasText(propertyName),
                "Illegal property name returned from 'extractPropertyName(String)': cannot be null or empty.");
            
            if(name.equals("type-name"))
                typeName = attribute.getValue();
              
            if(propertyName.equals("replicable"))
                builder.addPropertyValue(propertyName,attribute.getValue());
            
            if(propertyName.equals("optimisticLock"))
                builder.addPropertyValue(propertyName,attribute.getValue());
            
            if(propertyName.equals("fifoSupport"))
                builder.addPropertyValue(propertyName,attribute.getValue());
            
        }
        
        
        builder.addConstructorArgValue(typeName);
        
        if(StringUtils.hasText(superType))
            builder.addConstructorArgReference(superType);
        
        Element documentClassElem = DomUtils.getChildElementByTagName(element, "document-class");
        if (documentClassElem != null) {
            String className =documentClassElem.getTextContent();
           
            builder.addPropertyValue("documentClass", className);
        }

        HashMap<String,SpaceIndex> indexes = new HashMap<String,SpaceIndex>();
        
        List<Element> indexedElements = DomUtils.getChildElementsByTagName(element, "basic-index");

        for (int i = 0; i < indexedElements.size(); i++) {
            String indexPropertyPath = indexedElements.get(i).getAttribute("path");
            if (StringUtils.hasText(indexPropertyPath)) {
                indexes.put(indexPropertyPath,new BasicIndex(indexPropertyPath));
            }
           
        }
        indexedElements = DomUtils.getChildElementsByTagName(element, "extended-index");
        
        for (int i = 0; i < indexedElements.size(); i++) {
            String indexPropertyPath = indexedElements.get(i).getAttribute("path");
            if (StringUtils.hasText(indexPropertyPath)) {
                indexes.put(indexPropertyPath,new ExtendedIndex(indexPropertyPath));
            }
            
        }
        
        builder.addPropertyValue("indexes", indexes.values().toArray());
        
        Element idElem = DomUtils.getChildElementByTagName(element, "id");
        if (idElem != null) {
            String idPropertyName =idElem.getAttribute("property");
            String idAutogenerate =idElem.getAttribute("auto-generate");
            
            SpaceIdProperty idProperty= new SpaceIdProperty();
            idProperty.setPropertyName(idPropertyName);
            
            if(StringUtils.hasText(idAutogenerate))
                idProperty.setAutoGenerate(Boolean.parseBoolean(idAutogenerate));
            
            if(indexes.containsKey(idPropertyName))
                idProperty.setIndex(SpaceIndexType.NONE);
            
            builder.addPropertyValue("idProperty", idProperty);
        }
        
        Element routingElem = DomUtils.getChildElementByTagName(element, "routing");
        if (routingElem != null) {
            String propertyName =idElem.getAttribute("property");
            
            SpaceRoutingProperty routing= new SpaceRoutingProperty();
            routing.setPropertyName(propertyName);
            
            if(indexes.containsKey(propertyName))
                routing.setIndex(SpaceIndexType.NONE);
         
            
            builder.addPropertyValue("routingProperty", routing);
        }
    }
    private String extractPropertyName(String attributeName) {
        return Conventions.attributeNameToPropertyName(attributeName);
    }
}
