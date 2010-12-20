package org.openspaces.core.bean;

import java.util.Map;

import org.openspaces.admin.bean.BeanConfigException;
import org.openspaces.admin.bean.BeanConfigPropertiesManager;

public interface BeanServer<T extends Bean> extends BeanConfigPropertiesManager{
       
    /**
     * @param beanClassName
     * @return the bean instance of the specified class name, or null if not enabled.
     */
    T getEnabledBean(String beanClassName);
    
    /**
     * Disables all beans that implement the specified interface.
     * @param interfaceClass
     */
    void disableAllBeansAssignableTo(Class<?> interfaceClass);

    /**
     * Assuming there is at most one enabled bean that implements one of the specified interfaces, 
     * puts the specified configuration and enables a new bean in its place. 
     * 
     * @param interfaceClass - the interface that the new and old bean need to implement
     * @param newBeanConfig - the new bean configuration that is to replace the existing specified interface implementation.
     * 
     * @throws IllegalStateException - if more than one enabled bean implements any of the specified interfaces
     * @throws BeanConfigException - if the bean initialization failed.
     */
    void replaceBeanAssignableTo(Class<?>[] interfaceClasses, String beanClassName, Map<String,String> properties);

    void destroy();

    /**
     * Assuming there is at most one enabled bean that implements one of the specified interfaces, 
     * returns the enabled bean.
     * @param interfaceClasses
     * @return the enabled bean that implements any one of the specified interfaces.
     */
    T[] getEnabledBeanAssignableTo(Class<?>[] interfaceClasses);
}
