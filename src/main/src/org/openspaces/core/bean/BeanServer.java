package org.openspaces.core.bean;

import java.util.Map;

import org.openspaces.admin.bean.BeanPropertiesManager;

public interface BeanServer<T extends Bean> extends BeanPropertiesManager{
    
    Map<String, T> getEnabledBeansInstances();
    
}
