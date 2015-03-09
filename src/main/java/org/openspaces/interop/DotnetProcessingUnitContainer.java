/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.interop;

import com.gigaspaces.internal.dump.InternalDumpProcessor;
import org.openspaces.admin.quiesce.QuiesceStateChangedListener;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.MemberAliveIndicator;
import org.openspaces.core.cluster.ProcessingUnitUndeployingListener;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.pu.container.CannotCloseContainerException;
import org.openspaces.pu.container.CannotCreateContainerException;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.service.InvocableService;
import org.openspaces.pu.service.ServiceDetailsProvider;
import org.openspaces.pu.service.ServiceMetricProvider;
import org.openspaces.pu.service.ServiceMonitorsProvider;

import java.util.Collection;
import java.util.Map;

/**
 * @author kimchy
 * @since 6.6
 */
public class DotnetProcessingUnitContainer extends ProcessingUnitContainer {
       
    private final DotnetProcessingUnitBean dotnetProcessingUnitBean;
    
    public DotnetProcessingUnitContainer(ClusterInfo clusterInfo, BeanLevelProperties beanLevelProperties) throws CannotCreateContainerException{       
        dotnetProcessingUnitBean = new DotnetProcessingUnitBean();
        dotnetProcessingUnitBean.setClusterInfo(clusterInfo);
        dotnetProcessingUnitBean.setBeanLevelProperties(beanLevelProperties);
        try {
            dotnetProcessingUnitBean.afterPropertiesSet();
        } catch (Exception e) {
            throw new CannotCreateContainerException(e.getMessage(), e);
        } 
    }

    @Override
    public void close() throws CannotCloseContainerException {
        try {
            dotnetProcessingUnitBean.destroy();
        } catch (Exception e) {
            throw new CannotCloseContainerException(e.getMessage(), e);
        }
        super.close();
    }
    
    @Override
    public Collection<ServiceMetricProvider> getServiceMetricProviders() {
        return dotnetProcessingUnitBean.getServiceMetricProviders();
    }

    @Override
    public Collection<ServiceDetailsProvider> getServiceDetailsProviders() {
        return dotnetProcessingUnitBean.getServiceDetailsProviders();
    }

    @Override
    public Collection<ServiceMonitorsProvider> getServiceMonitorsProviders() {
        return dotnetProcessingUnitBean.getServiceMonitorsProviders();
    }

    @Override
    public Collection<QuiesceStateChangedListener> getQuiesceStateChangedListeners() {
        return dotnetProcessingUnitBean.getQuiesceStateChangedListeners();
    }

    @Override
    public Collection<ProcessingUnitUndeployingListener> getUndeployListeners() {
        return dotnetProcessingUnitBean.getUndeployListeners();
    }

    @Override
    public Collection<MemberAliveIndicator> getMemberAliveIndicators() {
        return dotnetProcessingUnitBean.getMemberAliveIndicators();
    }

    @Override
    public Collection<InternalDumpProcessor> getDumpProcessors() {
        return dotnetProcessingUnitBean.getDumpProcessors();
    }

    @Override
    public Map<String, InvocableService> getInvocableServices() {
        return dotnetProcessingUnitBean.getInvocableServices();
    }
}
