package org.openspaces.core.bean;

import java.util.Map;

import org.openspaces.admin.bean.BeanConfigPropertiesManager;

public interface BeanServer<T extends Bean> extends BeanConfigPropertiesManager{
    
    Map<String, T> getEnabledBeansInstances();
    
}
