package org.openspaces.core.config;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.BeansException;

/**
 * @author kimchy
 */
public class BeanLevelPropertyBeanPostProcessor implements BeanPostProcessor {

    private BeanLevelProperties beanLevelProperties;

    public BeanLevelPropertyBeanPostProcessor(BeanLevelProperties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof BeanLevelMergedPropertiesAware) {
            ((BeanLevelMergedPropertiesAware) bean).setMergedBeanLevelProperties(beanLevelProperties.getMergedBeanProperties(beanName));
        }
        if (bean instanceof BeanLevelPropertiesAware) {
            ((BeanLevelPropertiesAware) bean).setBeanLevelProperties(beanLevelProperties);
        }
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
