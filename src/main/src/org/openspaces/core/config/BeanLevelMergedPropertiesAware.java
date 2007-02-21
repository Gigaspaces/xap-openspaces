package org.openspaces.core.config;

import java.util.Properties;

/**
 * A callback that sets the merged properties using
 * {@link org.openspaces.core.config.BeanLevelProperties#getMergedBeanProperties(String)}
 * and the bean name that the bean that implements this interface is associated with.
 *
 * @author kimchy
 */
public interface BeanLevelMergedPropertiesAware {

    /**
     * Sets the merged properties using
     * {@link org.openspaces.core.config.BeanLevelProperties#getMergedBeanProperties(String)}
     * and the bean name that the bean that implements this interface is associated with.
     */
    void setMergedBeanLevelProperties(Properties beanLevelProperties);
}
