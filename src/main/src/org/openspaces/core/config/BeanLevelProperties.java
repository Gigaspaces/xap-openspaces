package org.openspaces.core.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author kimchy
 */
public class BeanLevelProperties {

    private Properties contextProperties = new Properties();

    private Map beanProperties = new HashMap();

    public Properties getContextProperties() {
        return contextProperties;
    }

    public void setContextProperties(Properties contextProperties) {
        this.contextProperties = contextProperties;
    }

    public Properties getBeanProperties(String beanName) {
        Properties props = (Properties) beanProperties.get(beanName);
        if (props == null) {
            props = new Properties();
            beanProperties.put(beanName, props);
        }
        return props;
    }

    public void setBeanProperties(String name, Properties nameBasedProperties) {
        this.beanProperties.put(name, nameBasedProperties);
    }

    public boolean hasBeanProperties(String name) {
        return getBeanProperties(name).size() != 0;
    }

    public Properties getMergedBeanProperties(String name) {
        Properties props = new Properties();
        props.putAll(contextProperties);
        Properties nameBasedProperties = getBeanProperties(name);
        if (nameBasedProperties != null) {
            props.putAll(nameBasedProperties);
        }
        return props;
    }
}
