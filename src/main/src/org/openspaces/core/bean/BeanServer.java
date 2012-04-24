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
package org.openspaces.core.bean;

import java.util.List;
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
     * @param interfaceClasses - the interface that the new and old bean need to implement
     * @param beanClassName - the class name of the bean
     * @param properties - the new bean configuration that is to replace the existing specified interface implementation.
     * 
     * @throws IllegalStateException - if more than one enabled bean implements any of the specified interfaces
     * @throws BeanConfigException - if the bean initialization failed.
     */
    void replaceBeanAssignableTo(Class<?>[] interfaceClasses, String beanClassName, Map<String,String> properties);

    void destroy();

    /**
     * @param interfaceClasses - the interface that the bean we are looking for needs to implement
     * @return the enabled bean that implements any one of the specified interfaces.
     */
    List<T> getEnabledBeansAssignableTo(Class<?>[] interfaceClasses);

    /**
     * @param interfaceClasses - the interface that the bean we are looking for needs to implement
     * @return the enabled bean class names that implements any one of the specified interfaces.
     * @throws ClassNotFoundException 
     */
    List<String> getEnabledBeansClassNamesAssignableTo(Class<?>[] interfaceClasses) throws ClassNotFoundException;
}
