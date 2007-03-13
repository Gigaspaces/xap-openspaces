package org.openspaces.core.cluster;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * A Spring {@link BeanPostProcessor} that takes a {@link ClusterInfo} and injects it to all the
 * beans that implements {@link ClusterInfoAware} interface.
 * 
 * @author kimchy
 */
public class ClusterInfoBeanPostProcessor implements BeanPostProcessor {

    private ClusterInfo clusterInfo;

    /**
     * Constructs a new cluster info bean post processor based on the provided cluster info.
     * 
     * @param clusterInfo
     */
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
