package org.openspaces.core.bean;

import java.util.Map;

import org.openspaces.admin.bean.BeanConfigNotFoundException;
import org.openspaces.admin.bean.BeanConfigurationException;

public interface BeanFactory<T extends Bean> {

    T create(String className, Map<String,String> properties) throws BeanConfigurationException, BeanConfigNotFoundException;
}
