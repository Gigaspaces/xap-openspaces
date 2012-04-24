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

import org.springframework.beans.factory.InitializingBean;

import com.gigaspaces.internal.cluster.node.impl.gateway.GatewayPolicy;
import com.gigaspaces.internal.cluster.node.impl.gateway.GatewaysPolicy;
import com.j_spaces.core.cluster.RedoLogCapacityExceededPolicy;

/**
 * A factory bean for creating a {@link GatewaysPolicy} instance.
 *  
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewayTargetsFactoryBean implements InitializingBean {
    
    private String localGatewayName;
    private List<GatewayTarget> gatewayTargets;
    private Integer bulkSize;
    private Long idleTimeThreshold;
    private Integer pendingOperationThreshold;
    private Long maxRedoLogCapacity;
    private RedoLogCapacityExceededPolicy onRedoLogCapacityExceeded;
    
    public GatewayTargetsFactoryBean() {
    }

    /**
     * Sets the local gateway name used for identification.
     * @param localGatewayName The local gateway name.
     */
    public void setLocalGatewayName(String localGatewayName) {
        this.localGatewayName = localGatewayName;
    }
    
    /**
     * @return The local gateway name used for identification.
     */
    public String getLocalGatewayName() {
        return localGatewayName;
    }
    
    /**
     * @return {@link GatewayTarget}s configuration as array.
     */
    public List<GatewayTarget> getGatewayTargets() {
        return gatewayTargets;
    }
    
    /**
     * Sets the gateway targets configuration.
     * @param gatewayTargets The gateway targets.
     */
    public void setGatewayTargets(List<GatewayTarget> gatewayTargets) {
        this.gatewayTargets = gatewayTargets;
    }

    /**
     * Sets the number of packets in each replication bulk sent to this gateway.
     * @param bulkSize number of packets in each replication bulk sent to this gateway.
     */
    public void setBulkSize(Integer bulkSize) {
        this.bulkSize = bulkSize;
    }
    
    /**
     * Sets the maximum time (in milliseconds) pending replication packets should wait before being replicated if
     * {@link #setPendingOperationThreshold(Integer)} has not been breached.
     * @param idleTimeThreshold the maximum time (in milliseconds).
     */
    public void setIdleTimeThreshold(Long idleTimeThreshold) {
        this.idleTimeThreshold = idleTimeThreshold;
    }
    
    /**
     * Sets the threshold count for pending replication packets that once reached, the packets will be replicated using the
     * {@link #setBulkSize(Integer)}.
     * @param pendingOperationThreshold the threshold count.
     * @see #setIdleTimeThreshold(Long)
     */
    public void setPendingOperationThreshold(Integer pendingOperationThreshold) {
        this.pendingOperationThreshold = pendingOperationThreshold;
    }
    
    /**
     * Sets limited redo log capacity for this gateway
     * @param maxRedoLogCapacity redo log limit
     */
    public void setMaxRedoLogCapacity(Long maxRedoLogCapacity) {
        this.maxRedoLogCapacity = maxRedoLogCapacity;
    }
 
    /**
     * Sets the behavior once the defined redo log capacity is exceeded, irrelevant if the capacity is unlimited.
     * @see #setMaxRedoLogCapacity(Long)
     * @param onRedoLogCapacityExceeded
     */
    public void setOnRedoLogCapacityExceeded(RedoLogCapacityExceededPolicy onRedoLogCapacityExceeded) {
        this.onRedoLogCapacityExceeded = onRedoLogCapacityExceeded;
    }
    
    /**
     * @return A new {@link GatewaysPolicy} instance using the bean's properties.
     */
    public GatewaysPolicy asGatewaysPolicy() {
        GatewaysPolicy gatewaysPolicy = new GatewaysPolicy();
        gatewaysPolicy.setLocalSiteName(getLocalGatewayName());
        if (gatewayTargets != null) {
            GatewayPolicy[] policies = new GatewayPolicy[gatewayTargets.size()];
            for (int i = 0; i < gatewayTargets.size(); i++) {
                policies[i] = gatewayTargets.get(i).asGatewayPolicy();
            }
            gatewaysPolicy.setGatewayPolicies(policies);
            gatewaysPolicy.setDefaultGatewayPolicy(createDefaultGatewayPolicy());
        }
        return gatewaysPolicy;
    }

    private GatewayPolicy createDefaultGatewayPolicy() {
        GatewayPolicy policy = new GatewayPolicy();
        policy.setGatewayName("default");
        if (bulkSize != null)
            policy.setBulkSize(bulkSize.intValue());
        if (idleTimeThreshold != null)
            policy.setIdleTimeThreshold(idleTimeThreshold.longValue());
        if (pendingOperationThreshold != null)
            policy.setPendingOperationThreshold(pendingOperationThreshold.intValue());
        if (maxRedoLogCapacity != null)
            policy.setMaxRedoLogCapacity(maxRedoLogCapacity.longValue());
        if (onRedoLogCapacityExceeded != null)
            policy.setOnRedoLogCapacityExceeded(onRedoLogCapacityExceeded);
        return policy;
    }

    public void afterPropertiesSet() throws Exception {
        if (gatewayTargets != null) {
            // Set gateway targets properties (override if doesn't exist)
            for (GatewayTarget gatewayTarget : gatewayTargets) {
                if (gatewayTarget.getBulkSize() == null)
                    gatewayTarget.setBulkSize(bulkSize);
                if (gatewayTarget.getIdleTimeThreshold() == null)
                    gatewayTarget.setIdleTimeThreshold(idleTimeThreshold);
                if (gatewayTarget.getMaxRedoLogCapacity() == null)
                    gatewayTarget.setMaxRedoLogCapacity(maxRedoLogCapacity);
                if (gatewayTarget.getPendingOperationThreshold() == null)
                    gatewayTarget.setPendingOperationThreshold(pendingOperationThreshold);
                if (gatewayTarget.getOnRedoLogCapacityExceeded() == null)
                    gatewayTarget.setOnRedoLogCapacityExceeded(onRedoLogCapacityExceeded);
            }
        }
    }
    
}
