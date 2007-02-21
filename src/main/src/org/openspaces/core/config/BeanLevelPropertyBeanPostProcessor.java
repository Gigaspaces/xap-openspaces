package org.openspaces.core.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * A Spring {@link org.springframework.beans.factory.config.BeanPostProcessor} that process
 * {@link org.openspaces.core.config.BeanLevelPropertiesAware} and
 * {@link org.openspaces.core.config.BeanLevelMergedPropertiesAware} based on the provided
 * {@link org.openspaces.core.config.BeanLevelProperties}.
 *
 * @author kimchy
 * @see org.openspaces.core.config.BeanLevelProperties
 * @see org.openspaces.core.config.BeanLevelMergedPropertiesAware
 * @see org.openspaces.core.config.BeanLevelPropertiesAware
 */
public class BeanLevelPropertyBeanPostProcessor implements BeanPostProcessor {

    private BeanLevelProperties beanLevelProperties;

    /**
     * Constructs a new bean level bean post processor based on the provided bean
     * level proeprties.
     *
     * @param beanLevelProperties The bean level properites to be used for injection
     */
    public BeanLevelPropertyBeanPostProcessor(BeanLevelProperties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }

    /**
     * Post process a given bean. If the bean implements {@link org.openspaces.core.config.BeanLevelPropertiesAware}
     * the provided {@link org.openspaces.core.config.BeanLevelProperties} will be injected to it. If the bean
     * implements {@link org.openspaces.core.config.BeanLevelMergedPropertiesAware} then the merged properties based
     * on the provided beanName will be injected
     * (using {@link org.openspaces.core.config.BeanLevelProperties#getMergedBeanProperties(String)}).
     *
     * @param bean     The bean to possibly perform injection of {@link org.openspaces.core.config.BeanLevelProperties}
     * @param beanName The bean name
     * @return The bean unmodified
     * @throws BeansException
     */
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof BeanLevelMergedPropertiesAware) {
            ((BeanLevelMergedPropertiesAware) bean).setMergedBeanLevelProperties(beanLevelProperties.getMergedBeanProperties(beanName));
        }
        if (bean instanceof BeanLevelPropertiesAware) {
            ((BeanLevelPropertiesAware) bean).setBeanLevelProperties(beanLevelProperties);
        }
        return bean;
    }

    /**
     * A no op, just returned the bean itself.
     */
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
