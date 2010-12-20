package org.openspaces.core.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.bean.BeanConfigException;
import org.openspaces.admin.bean.BeanConfigNotFoundException;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.bean.BeanInitializationException;
import org.openspaces.admin.bean.EnabledBeanConfigCannotBeChangedException;

public class DefaultBeanServer<T extends Bean> implements BeanServer<T> {

    private static final Log logger = LogFactory.getLog(DefaultBeanServer.class);
    private final Map<String, Map<String, String>> beansProperties = new HashMap<String, Map<String, String>>();
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

        beansProperties.put(beanClassName, properties);
    }

    public void enableBean(final String beanClassName) 
        throws BeanConfigNotFoundException, BeanConfigurationException, BeanInitializationException {
        
        Map<String, String> properties = beansProperties.get(beanClassName);

        if (properties == null) {
            throw new BeanConfigNotFoundException("Failed to enable bean [" + beanClassName + "] since it does not exist.");
        }

        if (!isBeanEnabled(beanClassName)) {
            
    
            final T beanInstance = beanFactory.create(beanClassName, properties, this);
            enabledBeans.put(beanClassName, beanInstance);
        }
    }

    public void disableBean(String beanClassName) throws BeanConfigNotFoundException {
        
        if (!beansProperties.containsKey(beanClassName)) {
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
        return beansProperties.remove(beanClassName) != null;
    }

    public boolean isBeanEnabled(String beanClassName){
      
        return enabledBeans.containsKey(beanClassName);
    }

    public Map<String, String> getConfig(String beanClassName) throws BeanConfigNotFoundException {

        if (!beansProperties.containsKey(beanClassName)) {
            throw new BeanConfigNotFoundException("Failed to get bean [" + beanClassName + "] since it does not exist.");
        }

        // clone so client code can't influence contents of map directly, and won't get influence from future changes.
        Map<String, String> properties = new HashMap<String, String>(beansProperties.get(beanClassName));
        return properties;
    }

    public String[] getBeansClassNames() {
        String[] startegies = beansProperties.keySet().toArray(new String[beansProperties.size()]);
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

    public T getEnabledBean(String beanClassName) {
        return this.enabledBeans.get(beanClassName);
    }

    public void disableAllBeansAssignableTo(Class<?> interfaceClass) {
        
        for (String beanClassName : getEnabledBeansClassNamesAssignableTo(interfaceClass)) {
            disableBean(beanClassName);
        }
    }

    private List<String> getEnabledBeansClassNamesAssignableTo(Class<?> interfaceClass) {
        
        List<String> beanClassNames = new ArrayList<String>();
        
        for (String beanClassName : this.enabledBeans.keySet()) {
            if (interfaceClass.isInstance(enabledBeans.get(beanClassName))) {
                beanClassNames.add(beanClassName);
            }
        }
        
        return beanClassNames;
    }

    public void replaceBeanAssignableTo(Class<?>[] interfaceClasses, String newBeanClassName, Map<String,String> newBeanProperties) {
        
        if (newBeanClassName == null) {
            throw new IllegalArgumentException("config.getBeanClassName() cannot be null.");
        }
        
        if (newBeanProperties == null) {
            throw new IllegalArgumentException("config.getProperties() cannot be null.");
        }
        
        List<String> beansClassNames = getEnabledBeansClassNamesAssignableTo(interfaceClasses);
        
        if (beansClassNames.size() > 1) {
            throw new IllegalStateException(
                    "Calling replaceBeanAssignableTo assumes there is only one enabled bean assignable to " + 
                    Arrays.toString(interfaceClasses) + ". "+
                    "Instead there are " + beansClassNames.size() + ": " + 
                    Arrays.toString(beansClassNames.toArray(new String[]{})));
        }
        
        // old bean
        String beanClassName = null;
        Map<String,String> beanProperties =null;
        
        // should we change the old bean with the new bean?
        boolean noChangeRequired = false;
        if (beansClassNames.size() == 1) {
            beanClassName = beansClassNames.get(0);
            beanProperties = getConfig(beanClassName);
            noChangeRequired = 
                newBeanClassName.equals(beanClassName) &&
                newBeanProperties.equals(beanProperties);            
        }
        
        if (!noChangeRequired) {
    
            if (beanClassName != null) {
                disableBean(beanClassName);
            }
        
            putConfig(newBeanClassName, newBeanProperties);
            enableBean(newBeanClassName);
            if (getEnabledBeansClassNamesAssignableTo(interfaceClasses).size() == 0) {
                throw new BeanConfigException(newBeanClassName + " does not implement any of the following: " + Arrays.toString(interfaceClasses)); 
            }                
        }
    }

    private List<String> getEnabledBeansClassNamesAssignableTo(Class<?>[] interfaceClasses) {
        List<String> beansClassNames = new ArrayList<String>();
        for (Class<?> interfaceClass : interfaceClasses) {
            beansClassNames.addAll(getEnabledBeansClassNamesAssignableTo(interfaceClass));
        }
        return beansClassNames;
    }

    public void destroy() {
        disableAllBeans();
    }

    @SuppressWarnings("unchecked")
    public T[] getEnabledBeanAssignableTo(Class<?>[] interfaceClasses) {
        List<T> beanInstances = new ArrayList<T>();
        for (String beanClassName : getEnabledBeansClassNamesAssignableTo(interfaceClasses)) {
            beanInstances.add(this.enabledBeans.get(beanClassName));
        }
        return (T[]) beanInstances.toArray();
    }
}
