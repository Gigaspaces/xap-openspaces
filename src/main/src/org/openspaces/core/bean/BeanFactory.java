package org.openspaces.core.bean;

import java.util.Map;

import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.bean.BeanInitializationException;

public interface BeanFactory<T extends Bean> {

    T create(String className, Map<String,String> properties, BeanServer<T> beanServer) throws BeanConfigurationException, BeanInitializationException;
}
