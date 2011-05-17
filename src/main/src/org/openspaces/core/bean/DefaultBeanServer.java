package org.openspaces.core.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
    private final Map<String, Map<String, String>> beansProperties = new HashMap<String, Map<String, String>>();
    private final Map<String, T> enabledBeans = new HashMap<String, T>();
    private final BeanFactory<T> beanFactory;

    public DefaultBeanServer(Admin admin) {
        this(new DefaultBeanFactory<T>(admin));
    }

    public DefaultBeanServer(BeanFactory<T> beanFactory) {
        this.beanFactory = beanFactory;
    }

    public void setBeanConfig(String beanClassName, Map<String, String> properties) throws EnabledBeanConfigCannotBeChangedException{

        if (isBeanEnabled(beanClassName)) {
            throw new EnabledBeanConfigCannotBeChangedException("Cannot modify bean [" + beanClassName + "] configuration while it is enabled. Disable it first.");
        } 

        beansProperties.put(beanClassName, properties);
    }

    public void enableBean(final String beanClassName) 
        throws BeanConfigNotFoundException, BeanConfigurationException, BeanInitializationException {
        
        if (beanClassName == null) {
            throw new IllegalArgumentException ("beanClassName cannot be null");
        }
        Map<String, String> properties = beansProperties.get(beanClassName);

        if (properties == null) {
            throw new BeanConfigNotFoundException("Failed to enable bean [" + beanClassName + "] since it does not exist.");
        }

        if (!isBeanEnabled(beanClassName)) {
            final T beanInstance = beanFactory.create(beanClassName, properties, this);
            enabledBeans.put(beanClassName, beanInstance);
            if (logger.isDebugEnabled()) {
                logger.debug("Bean " + beanClassName + " enabled.");
            }
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
            if (logger.isDebugEnabled()) {
                logger.debug("Bean " + instance.getClass() + " disabled.");
            }
        } catch (Exception e) {
            logger.error("Error destroying beanClassName " + instance.getClass().getName(), e);
        }
    }

    public boolean removeBeanConfig(String beanClassName) throws BeanConfigNotFoundException {
        
        if (isBeanEnabled(beanClassName)) {
            throw new EnabledBeanConfigCannotBeChangedException("Cannot remove configuration of beanClassName " + beanClassName + " since it is enabled. disable it first.");
        }
        return beansProperties.remove(beanClassName) != null;
    }

    public boolean isBeanEnabled(String beanClassName){
      
        return enabledBeans.containsKey(beanClassName);
    }

    public Map<String, String> getBeanConfig(String beanClassName) throws BeanConfigNotFoundException {

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

    public void replaceBeanAssignableTo(Class<?>[] interfaceClasses, String newBeanClassName, Map<String,String> newBeanProperties) throws BeanInitializationException {
        
        if (newBeanClassName == null) {
            throw new IllegalArgumentException("config.getBeanClassName() cannot be null.");
        }
        
        if (newBeanProperties == null) {
            throw new IllegalArgumentException("config.getProperties() cannot be null.");
        }
        
        List<String> enabledBeansClassNames ;
        try {
            
            if (!isClassNameAssignableFrom(newBeanClassName, interfaceClasses)) {
                throw new BeanConfigurationException(newBeanClassName + " does not implement any of the supplied classes " + Arrays.toString(interfaceClasses));
            }
        
        enabledBeansClassNames = getEnabledBeansClassNamesAssignableTo(interfaceClasses);

        if (enabledBeansClassNames.size() > 1) {
            throw new IllegalStateException(
                    "Calling replaceBeanAssignableTo assumes there is only one enabled bean assignable to " + 
                    Arrays.toString(interfaceClasses) + ". "+
                    "Instead there are " + enabledBeansClassNames.size() + ": " + 
                    Arrays.toString(enabledBeansClassNames.toArray(new String[]{})));
        }
        
        } catch (ClassNotFoundException e) {
            throw new BeanConfigurationException("Problem creating new bean instance.",e);
        }
        
        
        // old bean
        String enabledBeanClassName = null;
        Map<String,String> beanProperties =null;
        
        // should we change the old bean with the new bean?
        boolean noChangeRequired = false;
        if (enabledBeansClassNames.size() == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Request was made to enable bean instance " + newBeanClassName);
            }
        }
        else {
            enabledBeanClassName = enabledBeansClassNames.get(0);
            beanProperties = getBeanConfig(enabledBeanClassName);
            noChangeRequired = 
                newBeanClassName.equals(enabledBeanClassName) &&
                newBeanProperties.equals(beanProperties);
            
            if (logger.isDebugEnabled()) {
                if (!newBeanClassName.equals(enabledBeanClassName)) {
                    logger.debug(
                            "Request was made to replace enabled bean instance " + enabledBeanClassName + " "+
                            "with " + newBeanClassName);
                }
                else if (!newBeanProperties.equals(beanProperties)) {
                    logger.debug(
                            "Request was made to update enabled bean instance " + enabledBeanClassName + " "+
                            "with new configuration.");
                }
                else {
                    logger.debug(
                            "Request to update enabled bean instance " + enabledBeanClassName + " "+
                            "is ignored since no configuration change detected.");
                }
            }
        }
        
        if (!noChangeRequired) {
    
            if (enabledBeanClassName != null) {
                disableBean(enabledBeanClassName);
            }
        
            setBeanConfig(newBeanClassName, newBeanProperties);
            enableBean(newBeanClassName);
        }
    }

    public List<String> getEnabledBeansClassNamesAssignableTo(Class<?>[] interfaceClasses) throws ClassNotFoundException {
        List<String> beansClassNames = new ArrayList<String>();
        
        for (String beanClassName : this.enabledBeans.keySet()) {
            if (isClassNameAssignableFrom(beanClassName, interfaceClasses)) {
                beansClassNames.add(beanClassName);
            }
        }
        return beansClassNames;
   }

    private boolean isClassNameAssignableFrom(String implementationClassName, Class<?>[] interfaceClassNames) throws ClassNotFoundException {
        boolean classNameInstanceof = false;
        for (Class<?> interfaceClass : interfaceClassNames) {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(implementationClassName);
            if (interfaceClass.isAssignableFrom(clazz)) {
                classNameInstanceof = true;
                break;
            }
        }
        return classNameInstanceof;
    }

    public void destroy() {
        disableAllBeans();
    }

    public List<T> getEnabledBeansAssignableTo(Class<?>[] interfaceClasses) {
        List<T> beanInstances = new ArrayList<T>();
        try {
            for (String beanClassName : getEnabledBeansClassNamesAssignableTo(interfaceClasses)) {
                beanInstances.add(this.enabledBeans.get(beanClassName));
            }
        } catch (ClassNotFoundException e) {
            logger.error("Problem occured while scanning list of enabled containers", e);
        }
        return beanInstances;
    }
}
