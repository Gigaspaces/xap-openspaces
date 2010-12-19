package org.openspaces.core.bean;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.bean.BeanConfigNotFoundException;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.bean.BeanInitializationException;

public class DefaultBeanFactory<T extends Bean> implements BeanFactory<T> {

    Admin admin;
    
    public DefaultBeanFactory(Admin admin) {
        this.admin = admin;
    }

    @SuppressWarnings("unchecked")
    public T create(String beanClassName, Map<String,String> properties) throws BeanConfigNotFoundException {
    
        T instance = null;
        try {
            Class<T> clazz = 
                (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(beanClassName);

            instance = clazz.newInstance();
        } catch (Exception e) {
            throw new BeanConfigNotFoundException("Failed to instantiate bean bean class [" + beanClassName + "]", e);
        }
        // TODO - due to sharing of statistics monitor, need to have reference counter for
        // start/stop monitor calls.
        // until we do, we will create a different admin object for each bean.
        instance.setAdmin(admin);
        instance.setProperties(properties);
        
        try {
            instance.afterPropertiesSet();
        } catch (BeanInitializationException e) {
            throw e;
        } catch (BeanConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new BeanInitializationException(beanClassName + " initialization error",e);
        }
        return instance;       
    }

}
