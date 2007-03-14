package org.openspaces.core.properties;

import java.util.Properties;

/**
 * A callback that sets the merged properties using
 * {@link BeanLevelProperties#getMergedBeanProperties(String) mergedProperties} and the bean name
 * that the bean implementing this interface is associated with.
 * 
 * @author kimchy
 */
public interface BeanLevelMergedPropertiesAware {

    /**
     * Sets the merged properties using
     * {@link BeanLevelProperties#getMergedBeanProperties(String) mergedProperties} and the bean
     * name that the bean implementing this interface is associated with.
     */
    void setMergedBeanLevelProperties(Properties beanLevelProperties);
}
