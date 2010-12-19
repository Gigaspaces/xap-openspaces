package org.openspaces.core.bean;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.bean.BeanConfigNotFoundException;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.bean.BeanInitializationException;
import org.openspaces.admin.bean.EnabledBeanConfigCannotBeChangedException;

public class DefaultBeanServer<T extends Bean> implements BeanServer<T> {

    private static final Log logger = LogFactory.getLog(DefaultBeanServer.class);
    private final Map<String, Map<String, String>> beanProperties = new HashMap<String, Map<String, String>>();
    private final Map<String, T> enabledBeans = new HashMap<String, T>();
    private final BeanFactory<T> beanFactory;

    public DefaultBeanServer(Admin admin) {
        this(new DefaultBeanFactory<T>(admin));
    }

    public DefaultBeanServer(BeanFactory<T> beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void putConfig(String beanClassName, Map<String, String> properties) throws EnabledBeanConfigCannotBeChangedException{

        if (isBeanEnabled(beanClassName)) {
            throw new EnabledBeanConfigCannotBeChangedException("Cannot modify bean [" + beanClassName + "] configuration while it is enabled. Disable it first.");
        } 

        beanProperties.put(beanClassName, properties);
    }

    public void enableBean(final String beanClassName) 
        throws BeanConfigNotFoundException, BeanConfigurationException, BeanInitializationException {
        
        Map<String, String> properties = beanProperties.get(beanClassName);

        if (properties == null) {
            throw new BeanConfigNotFoundException("Failed to enable bean [" + beanClassName + "] since it does not exist.");
        }

        if (!isBeanEnabled(beanClassName)) {
            
    
            final T beanInstance = beanFactory.create(beanClassName, properties);
            enabledBeans.put(beanClassName, beanInstance);
        }
    }

    public void disableBean(String beanClassName) throws BeanConfigNotFoundException {
        
        if (!beanProperties.containsKey(beanClassName)) {
            throw new BeanConfigNotFoundException("Failed to disable bean [" + beanClassName + "] - doesn't exist.");
        }

        final T instance = enabledBeans.remove(beanClassName);
        if (instance != null) {
            disableBeanInternal(instance);
        }
    }

    private void disableBeanInternal(final T instance) {
        try {
            instance.destroy();
        } catch (Exception e) {
            logger.error("Error destroying beanClassName " + instance.getClass().getName(), e);
        }
    }

    public boolean removeConfig(String beanClassName) throws BeanConfigNotFoundException {
        
        if (isBeanEnabled(beanClassName)) {
            throw new EnabledBeanConfigCannotBeChangedException("Cannot remove configuration of beanClassName " + beanClassName + " since it is enabled. disable it first.");
        }
        return beanProperties.remove(beanClassName) != null;
    }

    public boolean isBeanEnabled(String beanClassName){
      
        return enabledBeans.containsKey(beanClassName);
    }

    public Map<String, String> getConfig(String beanClassName) throws BeanConfigNotFoundException {

        if (!beanProperties.containsKey(beanClassName)) {
            throw new BeanConfigNotFoundException("Failed to get bean [" + beanClassName + "] since it does not exist.");
        }

        // clone so client code can't influence contents of map directly.
        Map<String, String> properties = new HashMap<String, String>(beanProperties.get(beanClassName));
        return properties;
    }

    public String[] getBeansClassNames() {
        String[] startegies = beanProperties.keySet().toArray(new String[beanProperties.size()]);
        return startegies;
    }

    public String[] getEnabledBeansClassNames() {
        String[] startegies = enabledBeans.keySet().toArray(new String[enabledBeans.size()]);
        return startegies;
    }

    public void disableAllBeans() {
        for (T instance : enabledBeans.values()) {
            disableBeanInternal(instance);
        }
        enabledBeans.clear();
    }

    public Map<String, T> getEnabledBeansInstances() {
        return new HashMap<String, T>(this.enabledBeans);
    }

    public void destroy() {
        disableAllBeans();
    }
}
