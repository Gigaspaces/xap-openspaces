package org.openspaces.core.properties;

/**
 * A callback allowing for bean to be injected with {@link BeanLevelProperties}.
 * 
 * @author kimchy
 */
public interface BeanLevelPropertiesAware {

    /**
     * Sets the {@link BeanLevelProperties}.
     */
    void setBeanLevelProperties(BeanLevelProperties beanLevelProperties);
}
