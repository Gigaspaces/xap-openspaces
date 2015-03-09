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

package org.openspaces.pu.container;

import org.openspaces.admin.quiesce.QuiesceStateChangedListener;
import org.openspaces.core.cluster.MemberAliveIndicator;
import org.openspaces.core.cluster.ProcessingUnitUndeployingListener;
import org.openspaces.pu.service.ServiceDetailsProvider;
import org.openspaces.pu.service.ServiceMetricProvider;
import org.openspaces.pu.service.ServiceMonitorsProvider;

import java.util.Collection;

/**
 * A processing unit container represents a currently running processing unit context.
 *
 * @author kimchy
 */
public abstract class ProcessingUnitContainer {

    /**
     * Closes the given processing unit container.
     *
     * @throws CannotCloseContainerException
     */
    public void close() throws CannotCloseContainerException {
    }

    public abstract Collection<ServiceMetricProvider> getServiceMetricProviders();

    public abstract Collection<ServiceDetailsProvider> getServiceDetailsProviders();

    public abstract Collection<ServiceMonitorsProvider> getServiceMonitorsProviders();

    public abstract Collection<QuiesceStateChangedListener> getQuiesceStateChangedListeners();

    public abstract Collection<ProcessingUnitUndeployingListener> getUndeployListeners();

    public abstract Collection<MemberAliveIndicator> getMemberAliveIndicators();

}
