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
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

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

import com.gigaspaces.metadata.StorageType;
import com.gigaspaces.metadata.index.SpaceIndexType;

/**
 * Defines space-type tag parsing
 * @author anna
 */
public class GigaSpaceDocumentTypeBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class getBeanClass(Element element) {
        return GigaSpaceDocumentTypeDescriptorFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        super.doParse(element, parserContext, builder);
        NamedNodeMap attributes = element.getAttributes();
        
        for (int x = 0; x < attributes.getLength(); x++) {
            Attr attribute = (Attr) attributes.item(x);
            String name = attribute.getLocalName();
            if (ID_ATTRIBUTE.equals(name)) {
                continue;
            }
            String propertyName = extractPropertyName(name);
            Assert.state(StringUtils.hasText(propertyName),
                "Illegal property name returned from 'extractPropertyName(String)': cannot be null or empty.");
            
            if(propertyName.equals("typeName"))
                builder.addPropertyValue(propertyName,attribute.getValue());
              
            if(propertyName.equals("replicable"))
                builder.addPropertyValue(propertyName,attribute.getValue());
            
            if(propertyName.equals("optimisticLock"))
                builder.addPropertyValue(propertyName,attribute.getValue());
            
            if(propertyName.equals("fifoSupport"))
                builder.addPropertyValue(propertyName,attribute.getValue());
            
            if(propertyName.equals("storageType"))
                builder.addPropertyValue(propertyName, Enum.valueOf(StorageType.class, attribute.getValue().toUpperCase()));
        }
        
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
        //cater for compound indices after all non-compounds were added
        indexedElements = DomUtils.getChildElementsByTagName(element, "compound-index");
        
        for (int i = 0; i < indexedElements.size(); i++) {
            CompoundIndex cs = createCompoundIndexDef(indexedElements.get(i).getAttribute("paths"), indexedElements.get(i).getAttribute("type"));            
            if (cs != null)
                indexes.put(cs.getPath(),cs);
        }
        
        builder.addPropertyValue("indexes", indexes.values().toArray());

        Element idElem = DomUtils.getChildElementByTagName(element, "id");
        if (idElem != null) {
            String propertyName =idElem.getAttribute("property");
            String idAutogenerate = idElem.getAttribute("auto-generate");
            
            SpaceIdProperty idProperty= new SpaceIdProperty();
            idProperty.setPropertyName(propertyName);            
            if (StringUtils.hasText(idAutogenerate))
                idProperty.setAutoGenerate(Boolean.parseBoolean(idAutogenerate));
            
            if (indexes.containsKey(propertyName))
                idProperty.setIndex(SpaceIndexType.NONE);
            
            builder.addPropertyValue("idProperty", idProperty);
        }
        
        Element routingElem = DomUtils.getChildElementByTagName(element, "routing");
        if (routingElem != null) {
            String propertyName = routingElem.getAttribute("property");
            
            SpaceRoutingProperty routingProperty = new SpaceRoutingProperty();
            routingProperty.setPropertyName(propertyName);
            
            if (indexes.containsKey(propertyName))
                routingProperty.setIndex(SpaceIndexType.NONE);
                     
            builder.addPropertyValue("routingProperty", routingProperty);
        }
        
        Element fifoGroupingPropertyElem = DomUtils.getChildElementByTagName(element, "fifo-grouping-property");
        if (fifoGroupingPropertyElem != null) {
            String path = fifoGroupingPropertyElem.getAttribute("path");
                     
            builder.addPropertyValue("fifoGroupingPropertyPath", path);
        }
        
        List<String> fifoGroupingIndexes = new LinkedList<String>();
        List<Element> fifoGroupingIndexedElements = DomUtils.getChildElementsByTagName(element, "fifo-grouping-index");
        for(int i=0; i < fifoGroupingIndexedElements.size(); i++) {
            String path = fifoGroupingIndexedElements.get(i).getAttribute("path");
            fifoGroupingIndexes.add(path);
        }
        
        builder.addPropertyValue("fifoGroupingIndexesPaths", fifoGroupingIndexes);
        
        
    }

    private String extractPropertyName(String attributeName) {
        return Conventions.attributeNameToPropertyName(attributeName);
    }
    
    private CompoundIndex createCompoundIndexDef(String paths, String indexType)
    {//in any case of invalid setting we return null- no execption thrown
		if (!StringUtils.hasText(paths))
			return null;
		StringBuffer sb = new StringBuffer();	
		//parse the paths
		String delim = paths.indexOf(",") != -1 ? "," : " ";
		StringTokenizer st = new StringTokenizer(paths, delim);
		int pnum = st.countTokens();
		if (pnum < 2)
			return null;
		
		String[] ps = new String[pnum];
		for (int i = 0; i < pnum; i++)
		{
			ps[i] = st.nextToken().trim();
			if (!StringUtils.hasText(ps[i]))
				return null;
			if (i > 0)
				sb.append("+");
			sb.append(ps[i]);
		}
    	
		CompoundIndex.CompoundIndexTypes type = null;
		indexType = StringUtils.hasText(indexType) ? indexType.trim() : indexType;
		type = (!StringUtils.hasText(indexType) || indexType.equalsIgnoreCase("basic")) ? CompoundIndex.CompoundIndexTypes.BASIC :
			(indexType.equalsIgnoreCase("extended") ? CompoundIndex.CompoundIndexTypes.EXTENDED : null);
    	if (type == null)
    		return null;
    	return new CompoundIndex(sb.toString(), ps, type);
    	
    }
}
