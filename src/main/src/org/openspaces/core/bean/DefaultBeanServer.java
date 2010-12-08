package org.openspaces.core.bean;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.bean.BeanAlreadyExistsException;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.bean.BeanException;
import org.openspaces.admin.bean.BeanInitializationException;
import org.openspaces.admin.bean.BeanNotFoundException;
import org.openspaces.admin.bean.BeanPropertiesManager;

public class DefaultBeanServer<T extends Bean> implements BeanPropertiesManager {

    private final Map<String, Map<String, String>> beanProperties = new HashMap<String, Map<String, String>>();
    private final Map<String, T> enabledBeans = new HashMap<String, T>();
    private final Admin admin;

    public DefaultBeanServer(Admin admin) {
        this.admin = admin;
    }

    public void addBean(String bean, Map<String, String> properties) throws BeanAlreadyExistsException {
        if (beanProperties.containsKey(bean)) {
            throw new BeanAlreadyExistsException("Failed to add bean [" + bean + "] - already exists.");
        }
        beanProperties.put(bean, properties);
    }

    public void setBean(String bean, Map<String, String> properties) throws BeanNotFoundException {

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

    public void enableBean(String bean) throws BeanNotFoundException, BeanConfigurationException, BeanInitializationException {
        if (!beanProperties.containsKey(bean)) {
            throw new BeanNotFoundException("Failed to enable bean [" + bean + "] - doesn't exist.");
        }

        if (enabledBeans.containsKey(bean)) {
            return; // idempotent - already enabled just return
        }

        T beanInstance = newInstance(bean);
        enabledBeans.put(bean, beanInstance);

        // TODO - due to sharing of statistics monitor, need to have reference counter for
        // start/stop monitor calls.
        // until we do, we will create a different admin object for each bean.
        beanInstance.setAdmin(admin);
        beanInstance.setProperties(beanProperties.get(bean));
        try {
            beanInstance.afterPropertiesSet();
        } catch (BeanException e) {
            throw e;
        } catch (Exception e) {
            throw new BeanException("Failed to enabled bean ["+bean+"]", e);
        }
    }

    public void disableBean(String bean) throws BeanNotFoundException {
        if (!beanProperties.containsKey(bean)) {
            throw new BeanNotFoundException("Failed to disable bean [" + bean + "] - doesn't exist.");
        }
        if (enabledBeans.containsKey(bean)) {
            T beanBean = enabledBeans.remove(bean);
            try {
                beanBean.destroy();
            }catch (Exception e) {
                //TODO log it!
            }
        }
    }

    public void removeBean(String bean) throws BeanNotFoundException {
        if (!beanProperties.containsKey(bean)) {
            throw new BeanNotFoundException("Failed to remove bean [" + bean + "] - doesn't exist.");
        }
        disableBean(bean);
        beanProperties.remove(bean);
    }

    public Map<String, String> getBean(String bean) throws BeanNotFoundException {
        if (!beanProperties.containsKey(bean)) {
            throw new BeanNotFoundException("Failed to get bean [" + bean + "] - doesn't exist.");
        }

        // clone to simulate serialization - client code can't influence contents of map directly.
        Map<String, String> properties = new HashMap<String, String>(beanProperties.get(bean));
        return properties;
    }

    public String[] getBeans() {
        String[] startegies = beanProperties.keySet().toArray(new String[beanProperties.size()]);
        return startegies;
    }

    public String[] getEnabledBeans() {
        String[] startegies = enabledBeans.keySet().toArray(new String[enabledBeans.size()]);
        return startegies;
    }

    public void disableAllBeans() {
        for (Iterator<T> iter = enabledBeans.values().iterator(); iter.hasNext();) {
            T beanBean = iter.next();
            try {
                beanBean.destroy();
            }catch (Exception e) {
                //TODO log it!
            }
            iter.remove();
        }
    }

    @SuppressWarnings("unchecked") 
    private T newInstance(String beanClassName) throws BeanNotFoundException {
        try {
            Class<T> clazz = (Class<T>) Thread.currentThread().getContextClassLoader().loadClass(
                    beanClassName);
            T newInstance = clazz.newInstance();
            return newInstance;
        } catch (Exception e) {
            throw new BeanNotFoundException("Failed to instantiate bean bean class [" + beanClassName
                    + "]", e);
        }
    }
}
