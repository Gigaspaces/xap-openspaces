package org.openspaces.core.config;

/**
 * A callback allowing for bean to be injected with {@link org.openspaces.core.config.BeanLevelProperties}.
 *
 * @author kimchy
 */
public interface BeanLevelPropertiesAware {

    /**
     * Sets the {@link org.openspaces.core.config.BeanLevelProperties}.
     */
    void setBeanLevelProperties(BeanLevelProperties beanLevelProperties);
}
