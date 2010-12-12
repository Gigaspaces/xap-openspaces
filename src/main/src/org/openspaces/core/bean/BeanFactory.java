package org.openspaces.core.bean;

import java.util.Map;

public interface BeanFactory<T extends Bean> {

    T create(String className, Map<String,String> properties);
    
}
