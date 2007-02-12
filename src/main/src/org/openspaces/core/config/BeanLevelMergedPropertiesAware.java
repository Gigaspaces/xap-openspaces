package org.openspaces.core.config;

import java.util.Properties;

/**
 * @author kimchy
 */
public interface BeanLevelMergedPropertiesAware {

    void setMergedBeanLevelProperties(Properties beanLevelProperties);
}
