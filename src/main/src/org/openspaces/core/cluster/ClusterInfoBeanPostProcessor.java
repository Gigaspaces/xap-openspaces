/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
