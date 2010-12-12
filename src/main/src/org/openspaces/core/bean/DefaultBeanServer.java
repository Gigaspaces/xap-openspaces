package org.openspaces.core.bean;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.bean.BeanAlreadyExistsException;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.bean.BeanException;
import org.openspaces.admin.bean.BeanInitializationException;
import org.openspaces.admin.bean.BeanNotFoundException;

public class DefaultBeanServer<T extends Bean> implements BeanServer<T> {

    private static final Log logger = LogFactory.getLog(DefaultBeanServer.class);
    private final Map<String, Map<String, String>> beanProperties = new HashMap<String, Map<String, String>>();
    private final Map<String, T> enabledBeans = new HashMap<String, T>();
    private final BeanFactory<T> beanFactory;
    private final Object lock = new Object();
    
    public DefaultBeanServer(Admin admin) {
        this(new DefaultBeanFactory<T>(admin));
    }
    
    public DefaultBeanServer(BeanFactory<T> beanFactory) {
        this.beanFactory = beanFactory; 
    }
    
    public void addBean(String bean, Map<String, String> properties) throws BeanAlreadyExistsException {
        synchronized(lock) {

            if (beanProperties.containsKey(bean)) {
                throw new BeanAlreadyExistsException("Failed to add bean [" + bean + "] - already exists.");
            }
            beanProperties.put(bean, properties);
        }
    }

    public void setBean(String bean, Map<String, String> properties) throws BeanNotFoundException {
        synchronized(lock) {

            if (!beanProperties.containsKey(bean)) {
                throw new BeanNotFoundException("Failed to set bean [" + bean + "] - doesn't exist.");
            }

            if (enabledBeans.containsKey(bean)) {
                disableBean(bean);
                beanProperties.put(bean, properties);
                enableBean(bean);
            } else {
                beanProperties.put(bean, properties);
            }
        }
    }

    public void enableBean(final String bean) throws BeanNotFoundException, BeanConfigurationException, BeanInitializationException {
        synchronized(lock) {

            Map<String, String> properties = beanProperties.get(bean);
            
            if (properties == null) {
                throw new BeanNotFoundException("Failed to enable bean [" + bean + "] - doesn't exist.");
            }
        
            if (enabledBeans.containsKey(bean)) {
                return; // idempotent - already enabled just return
            }
    
            
            final T beanInstance = beanFactory.create(bean,properties);
            enabledBeans.put(bean, beanInstance);

            try {
                beanInstance.afterPropertiesSet();
            } catch (BeanException e) {
                throw e;
            } catch (Exception e) {
                throw new BeanException("Failed to enabled bean ["+bean+"]", e);
            }            
        }
    }

    public void disableBean(String bean) throws BeanNotFoundException {
        synchronized(lock) {
            if (!beanProperties.containsKey(bean)) {
                throw new BeanNotFoundException("Failed to disable bean [" + bean + "] - doesn't exist.");
            }
        
            final T instance = enabledBeans.remove(bean);
            if (instance != null) {
                disableBeanInternal(instance);
            }
        }
    }

    private void disableBeanInternal(final T instance) {
        try {
            instance.destroy();
        }catch (Exception e) {
            logger.error("Error destroying bean " + instance.getClass().getName(),e);
        }
    }

    public void removeBean(String bean) throws BeanNotFoundException {
        synchronized(lock) {
            if (!beanProperties.containsKey(bean)) {
                throw new BeanNotFoundException("Failed to remove bean [" + bean + "] - doesn't exist.");
            }
        
            disableBean(bean);
            beanProperties.remove(bean);
        }
    }

    public Map<String, String> getBean(String bean) throws BeanNotFoundException {
     
        synchronized(lock) {

            if (!beanProperties.containsKey(bean)) {
                throw new BeanNotFoundException("Failed to get bean [" + bean + "] - doesn't exist.");
            }
    
            // clone to simulate serialization - client code can't influence contents of map directly.
            Map<String, String> properties = new HashMap<String, String>(beanProperties.get(bean));
            return properties;
        }
    }

    public String[] getBeans() {
        synchronized(lock) {
            String[] startegies = beanProperties.keySet().toArray(new String[beanProperties.size()]);
            return startegies;
        }
    }

    public String[] getEnabledBeans() {
        synchronized(lock) {
            String[] startegies = enabledBeans.keySet().toArray(new String[enabledBeans.size()]);
            return startegies;
        }
    }

    public void disableAllBeans() {
        synchronized(lock) {
            for (T instance : enabledBeans.values()) {
                disableBeanInternal(instance);
            }
            enabledBeans.clear();
        }
    }

    public Map<String, T> getEnabledBeansInstances() {
        synchronized(lock) {
            return new HashMap<String,T>(this.enabledBeans);
        }
    }

}
