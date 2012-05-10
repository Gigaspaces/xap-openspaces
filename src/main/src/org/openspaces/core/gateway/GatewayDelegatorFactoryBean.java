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
package org.openspaces.core.gateway;

import java.util.List;

import org.openspaces.core.space.SecurityConfig;
import org.openspaces.pu.service.ServiceDetails;
import org.openspaces.pu.service.ServiceDetailsProvider;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.gigaspaces.internal.cluster.node.impl.gateway.ReplicationDelegationConfig;
import com.gigaspaces.internal.cluster.node.impl.gateway.delegator.ReplicationConnectionDelegatorConfig;
import com.gigaspaces.internal.cluster.node.impl.gateway.delegator.ReplicationConnectionDelegatorContainer;
import com.gigaspaces.internal.cluster.node.impl.gateway.lus.ReplicationLookupParameters;

/**
 * A gateway delegator factory bean for creating a {@link ReplicationConnectionDelegatorContainer}.
 * 
 * @author idan
 * @since 8.0.3
 *
 */
public class GatewayDelegatorFactoryBean extends AbstractGatewayComponentFactoryBean implements DisposableBean, InitializingBean, ServiceDetailsProvider {

    private ReplicationConnectionDelegatorContainer replicationConnectiondelegatorContainer;
    private List<GatewayDelegation> gatewayDelegations;
    
    public GatewayDelegatorFactoryBean() {
    }

    /**
     * @return Gateway delegator factory bean's delegations as a {@link GatewayDelegation} array. 
     */
    public List<GatewayDelegation> getGatewayDelegations() {
        return gatewayDelegations;
    }
    
    /**
     * Sets the gateway delegations for the configured {@link ReplicationConnectionDelegatorContainer}.
     * @param gatewayDelegations {@link GatewayDelegation}s array.
     */
    public void setGatewayDelegations(List<GatewayDelegation> gatewayDelegations) {
        this.gatewayDelegations = gatewayDelegations;
    }

    /**
     * Initializes a {@link ReplicationConnectionDelegatorContainer} using the provided bean's properties.
     */
    @Override
    protected void afterPropertiesSetImpl(SecurityConfig securityConfig) {
        
        ReplicationConnectionDelegatorConfig config = new ReplicationConnectionDelegatorConfig(getLocalGatewayName());
        config.setStartLookupService(isStartEmbeddedLus());
        if (gatewayDelegations != null) {
            for (GatewayDelegation delegation : gatewayDelegations) {
                ReplicationDelegationConfig replicationRoutingConfig = new ReplicationDelegationConfig();
                replicationRoutingConfig.setTargetName(delegation.getTarget());
                replicationRoutingConfig.setDelegation(delegation.getDelegateThrough());
                config.addDelegator(replicationRoutingConfig);
            }
        }
        if (getGatewayLookups() != null) {
            ReplicationLookupParameters lookupParameters = getGatewayLookups().asReplicationLookupParameters();
            config.setGatewayLookupParameters(lookupParameters);
        }
        // TODO WAN: add finder timeout
        replicationConnectiondelegatorContainer = new ReplicationConnectionDelegatorContainer(config);
    }

    /**
     * Destroys and unregisters the gateway delegator component.
     */
    @Override
    protected void destroyImpl() {
        if (replicationConnectiondelegatorContainer != null) {
            replicationConnectiondelegatorContainer.close();
            replicationConnectiondelegatorContainer = null;
        }
    }

    public ServiceDetails[] getServicesDetails() {
        return new ServiceDetails[]{new GatewayDelegatorServiceDetails(getLocalGatewayName(), getGatewayDelegations().toArray(new GatewayDelegation[getGatewayDelegations().size()]))};
    }

    @Override
    protected String getGatewayComponentTypeName() {
        return "Delegator";
    }

    @Override
    protected String dumpState() {
        return replicationConnectiondelegatorContainer != null ? replicationConnectiondelegatorContainer.dumpState() : "";
    }

}
