package org.openspaces.core.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.gigaspaces.annotation.pojo.FifoSupport;
import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import com.gigaspaces.metadata.index.SpaceIndexType;
import com.j_spaces.kernel.ClassLoaderHelper;

public class GigaSpaceDocumentTypeDescriptorFactoryBean  implements FactoryBean<SpaceTypeDescriptor> ,InitializingBean{


    private SpaceTypeDescriptor typeDescriptor;
    private final SpaceTypeDescriptorBuilder typeDescriptorBuilder;
    
    public GigaSpaceDocumentTypeDescriptorFactoryBean(String typeName)
    {
        typeDescriptorBuilder = new SpaceTypeDescriptorBuilder(typeName);
    }
    
    public GigaSpaceDocumentTypeDescriptorFactoryBean(String typeName,SpaceTypeDescriptor superTypeDescriptor)
    {
        typeDescriptorBuilder = new SpaceTypeDescriptorBuilder(typeName,superTypeDescriptor);
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
        if(typeDescriptor == null)
            typeDescriptor = typeDescriptorBuilder.create();
    }
  
    public void setFifoSupport(FifoSupport fifoSupport) {
        typeDescriptorBuilder.setFifoSupport(fifoSupport);
    }

    public void setDocumentClass(String documentWrapperClassName) throws ClassNotFoundException {
        Class<? extends SpaceDocument> documentWrapperClass = ClassLoaderHelper.loadClass(documentWrapperClassName);
         typeDescriptorBuilder.setDocumentWrapperClass(documentWrapperClass);
    }

    public void setReplicable(boolean replicable) {
         typeDescriptorBuilder.setReplicable(replicable);
    }

    public void setOptimisticLock(boolean optimisticLocking) {
         typeDescriptorBuilder.setSupportsOptimisticLocking(optimisticLocking);
    }

    public void setIdProperty(SpaceIdProperty idProperty) {
        if(idProperty.getIndex() == null)
            typeDescriptorBuilder.setIdProperty(idProperty.getPropertyName(), idProperty.isAutoGenerate());
        else
            typeDescriptorBuilder.setIdProperty(idProperty.getPropertyName(), idProperty.isAutoGenerate(),idProperty.getIndex());
    }

    public void setRoutingProperty(SpaceRoutingProperty routingProperty) {
        if(routingProperty.getIndex() == null)
            typeDescriptorBuilder.setRoutingProperty(routingProperty.getPropertyName());
        else
            typeDescriptorBuilder.setRoutingProperty(routingProperty.getPropertyName(), routingProperty.getIndex());
    }

    public void setIndexes(SpaceIndex... indexes)
    {
        for (Object index : indexes) {
            
            if (index instanceof BasicIndex)
            {
                BasicIndex basicIndex = (BasicIndex)index;
                typeDescriptorBuilder.addPathIndex(basicIndex.getPath(), SpaceIndexType.BASIC);
            }
            else if (index instanceof ExtendedIndex)
            {
                ExtendedIndex extendedIndex = (ExtendedIndex)index;
                typeDescriptorBuilder.addPathIndex(extendedIndex.getPath(), SpaceIndexType.EXTENDED);
            }
            else
            {
                throw new IllegalArgumentException("Illegal index type " + index);
            }
        }
    }
    
}
