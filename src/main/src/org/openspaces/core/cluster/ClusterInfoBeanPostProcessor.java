package org.openspaces.core.cluster;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * A Spring {@link org.springframework.beans.factory.config.BeanPostProcessor} that takes a
 * {@link org.openspaces.core.cluster.ClusterInfo} and injects it to all the beans that implements
 * {@link org.openspaces.core.cluster.ClusterInfoAware} interface.
 *
 * @author kimchy
 */
public class ClusterInfoBeanPostProcessor implements BeanPostProcessor {

    private ClusterInfo clusterInfo;

    public ClusterInfoBeanPostProcessor(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ClusterInfoAware) {
            ((ClusterInfoAware) bean).setClusterInfo(clusterInfo);
        }
        return bean;
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
