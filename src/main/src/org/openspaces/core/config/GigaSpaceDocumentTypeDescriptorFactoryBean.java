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

import java.util.Set;
import java.util.SortedMap;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.gigaspaces.annotation.pojo.FifoSupport;
import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpacePropertyDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import com.gigaspaces.metadata.StorageType;
import com.gigaspaces.metadata.index.SpaceIndexType;
import com.j_spaces.kernel.ClassLoaderHelper;

public class GigaSpaceDocumentTypeDescriptorFactoryBean implements FactoryBean<SpaceTypeDescriptor>, InitializingBean {

    private SpaceTypeDescriptor typeDescriptor;

    private String _typeName;
    private SpaceTypeDescriptor _superTypeDescriptor;
    private SortedMap<String, SpacePropertyDescriptor> _fixedProperties;
    private FifoSupport _fifoSupport;
    private Boolean _replicable;
    private Boolean _supportsOptimisticLocking;
    private SpaceIndex[] _indexes;
    private SpaceRoutingProperty _routingProperty;
    private SpaceIdProperty _idProperty;
    private StorageType _storageType;
    private String _fifoGroupingPropertyPath;
    private Set<String> _fifoGroupingIndexesPaths;

    private String _documentWrapperClassName;

    public GigaSpaceDocumentTypeDescriptorFactoryBean() {
    }

    public SpaceTypeDescriptor getObject() throws Exception {
        return typeDescriptor;
    }

    public Class<?> getObjectType() {
        return SpaceTypeDescriptor.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        if (typeDescriptor == null) {
            SpaceTypeDescriptorBuilder typeDescriptorBuilder = new SpaceTypeDescriptorBuilder(_typeName,
                    _superTypeDescriptor);
            
            if(_idProperty != null)
            {
                if ( _idProperty.getIndex() == null)
                    typeDescriptorBuilder.idProperty(_idProperty.getPropertyName(), _idProperty.isAutoGenerate());
                else
                    typeDescriptorBuilder.idProperty(_idProperty.getPropertyName(), _idProperty.isAutoGenerate(),
                            _idProperty.getIndex());
            }

            if(_routingProperty != null)
            {
                if (_routingProperty.getIndex() == null)
                    typeDescriptorBuilder.routingProperty(_routingProperty.getPropertyName());
                else
                    typeDescriptorBuilder.routingProperty(_routingProperty.getPropertyName(),
                            _routingProperty.getIndex());
            }

            if(_fifoSupport != null)
                typeDescriptorBuilder.fifoSupport(_fifoSupport);
            
            if(_supportsOptimisticLocking != null)
                typeDescriptorBuilder.supportsOptimisticLocking(_supportsOptimisticLocking);
            
            if(_replicable != null)
                typeDescriptorBuilder.replicable(_replicable);
            
            if(_documentWrapperClassName != null)
            {
                _documentWrapperClassName = _documentWrapperClassName.trim();
                Class<? extends SpaceDocument> documentWrapperClass = ClassLoaderHelper.loadClass(_documentWrapperClassName);
                typeDescriptorBuilder.documentWrapperClass(documentWrapperClass);
            }
            
            if(_indexes != null)
            {
                for (SpaceIndex index : _indexes) {

                    if (index instanceof BasicIndex) {
                        BasicIndex basicIndex = (BasicIndex) index;
                        typeDescriptorBuilder.addPathIndex(basicIndex.getPath(), SpaceIndexType.BASIC);
                    } else if (index instanceof ExtendedIndex) {
                        ExtendedIndex extendedIndex = (ExtendedIndex) index;
                        typeDescriptorBuilder.addPathIndex(extendedIndex.getPath(), SpaceIndexType.EXTENDED);
                    } else {
                        throw new IllegalArgumentException("Illegal index type " + index);
                    }
                }
            }
            
            if(_storageType != null)
                typeDescriptorBuilder.storageType(_storageType);
            if(_fifoGroupingPropertyPath != null)
                typeDescriptorBuilder.fifoGroupingProperty(_fifoGroupingPropertyPath);
            if(_fifoGroupingIndexesPaths != null) {
                for (String fifoGroupingIndexPath : _fifoGroupingIndexesPaths) {
                    typeDescriptorBuilder.addFifoGroupingIndex(fifoGroupingIndexPath);
                }
            }
            
            typeDescriptor = typeDescriptorBuilder.create();
        }
    }

    public void setTypeName(String typeName) {
        _typeName = typeName;
    }

    public void setSuperType(SpaceTypeDescriptor superTypeDescriptor) {
        _superTypeDescriptor = superTypeDescriptor;
    }

    public void setFifoSupport(FifoSupport fifoSupport) {
        _fifoSupport = fifoSupport;
    }

    public void setReplicable(boolean replicable) {
        _replicable = replicable;
    }

    public void setOptimisticLock(boolean optimisticLocking) {
        _supportsOptimisticLocking = optimisticLocking;
    }

    public void setIdProperty(SpaceIdProperty idProperty) {
        _idProperty = idProperty;
    }

    public void setRoutingProperty(SpaceRoutingProperty routingProperty) {
        _routingProperty = routingProperty;
    }

    public void setIndexes(SpaceIndex... indexes) {
        _indexes = indexes;
    }
   
     public void setDocumentClass(String documentWrapperClassName) throws ClassNotFoundException {
        _documentWrapperClassName = documentWrapperClassName;
    }

    public StorageType getStorageType() {
        return _storageType;
    }

    public void setStorageType(StorageType storageType) {
        this._storageType = storageType;
    }

    public String getFifoGroupingPropertyPath() {
        return _fifoGroupingPropertyPath;
    }

    public void setFifoGroupingPropertyPath(String fifoGroupingPropertyPath) {
        this._fifoGroupingPropertyPath = fifoGroupingPropertyPath;
    }

    public Set<String> getFifoGroupingIndexesPaths() {
        return _fifoGroupingIndexesPaths;
    }

    public void setFifoGroupingIndexesPaths(Set<String> fifoGroupingIndexesPaths) {
        this._fifoGroupingIndexesPaths = fifoGroupingIndexesPaths;
    }    

}
