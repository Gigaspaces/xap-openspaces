package org.openspaces.core.bean;

import org.openspaces.admin.bean.BeanConfigPropertiesManager;

public interface BeanServer<T extends Bean> extends BeanConfigPropertiesManager{
       
    void destroy();
}
