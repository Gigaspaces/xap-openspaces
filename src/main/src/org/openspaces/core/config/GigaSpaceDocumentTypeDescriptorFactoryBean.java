package org.openspaces.core.config;

import java.util.SortedMap;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.gigaspaces.annotation.pojo.FifoSupport;
import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpacePropertyDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
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
                    typeDescriptorBuilder.setIdProperty(_idProperty.getPropertyName(), _idProperty.isAutoGenerate());
                else
                    typeDescriptorBuilder.setIdProperty(_idProperty.getPropertyName(), _idProperty.isAutoGenerate(),
                            _idProperty.getIndex());
            }

            if(_routingProperty != null)
            {
                if (_routingProperty.getIndex() == null)
                    typeDescriptorBuilder.setRoutingProperty(_routingProperty.getPropertyName());
                else
                    typeDescriptorBuilder.setRoutingProperty(_routingProperty.getPropertyName(),
                            _routingProperty.getIndex());
            }

            if(_fifoSupport != null)
                typeDescriptorBuilder.setFifoSupport(_fifoSupport);
            
            if(_supportsOptimisticLocking != null)
                typeDescriptorBuilder.setSupportsOptimisticLocking(_supportsOptimisticLocking);
            
            if(_replicable != null)
                typeDescriptorBuilder.setReplicable(_replicable);
            
            if(_documentWrapperClassName != null)
            {
                Class<? extends SpaceDocument> documentWrapperClass = ClassLoaderHelper.loadClass(_documentWrapperClassName);
                typeDescriptorBuilder.setDocumentWrapperClass(documentWrapperClass);
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
    

}
