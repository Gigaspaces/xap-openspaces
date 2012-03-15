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

import com.gigaspaces.internal.cluster.node.impl.gateway.GatewayPolicy;
import com.j_spaces.core.cluster.RedoLogCapacityExceededPolicy;

/**
 * Holds gateway target configuration.
 * 
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewayTarget {

    private String name;
    private Integer bulkSize;
    private Long idleTimeThreshold;
    private Integer pendingOperationThreshold;
    private Long maxRedoLogCapacity;
    private RedoLogCapacityExceededPolicy onRedoLogCapacityExceeded;

    public GatewayTarget() {
        
    }
    public GatewayTarget(String name) {
        this.name = name;
    }

    /**
     * @return The gateway's target name used for identification.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the gateway's target name used for identification.
     * @param name The gateway name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the number of packets in each replication bulk sent to this gateway.
     * @param bulkSize number of packets in each replication bulk sent to this gateway.
     */
    public void setBulkSize(Integer bulkSize) {
        if (bulkSize != null && bulkSize < 0)
            throw new IllegalArgumentException("bulkSize can't be negative.");
        this.bulkSize = bulkSize;
    }
    
    /**
     * @return The number of packets in each replication bulk sent to this gateway
     */
    public Integer getBulkSize() {
        return bulkSize;
    }
    
    /**
     * Sets the maximum time (in milliseconds) pending replication packets should wait before being replicated if
     * {@link #setPendingOperationThreshold(Integer)} has not been breached.
     * @param idleTimeThreshold the maximum time (in milliseconds).
     */
    public void setIdleTimeThreshold(Long idleTimeThreshold) {
        if (idleTimeThreshold != null && idleTimeThreshold < 0)
            throw new IllegalArgumentException("idleTimeThreshold can't be negative.");
        this.idleTimeThreshold = idleTimeThreshold;
    }

    /**
     * @return The maximum time (in milliseconds) pending replication packets should wait before being replicated
     * if {@link #setPendingOperationThreshold(Integer) has not been breached.
     */
    public Long getIdleTimeThreshold() {
        return idleTimeThreshold;
    }
    
    /**
     * Sets the threshold count for pending replication packets that once reached, the packets will be replicated using the
     * {@link #setBulkSize(Integer)}.
     * @param pendingOperationThreshold the threshold count.
     * @see #setIdleTimeThreshold(long)
     */
    public void setPendingOperationThreshold(Integer pendingOperationThreshold) {
        if (pendingOperationThreshold != null && pendingOperationThreshold < 0)
            throw new IllegalArgumentException("pendingOperationThreshold can't be negative.");
        this.pendingOperationThreshold = pendingOperationThreshold;
    }
    
    /**
     * @return The threshold count for pending replication packets that once reached, the packets will be replicated using
     * the {@link #setBulkSize(Integer).
     */
    public Integer getPendingOperationThreshold() {
        return pendingOperationThreshold;
    }
    
    /**
     * Sets unlimited replication redo log capacity for this gateway.
     */
    public void setUnlimitedRedoLogCapacity() {
        this.maxRedoLogCapacity = -1L;
    }
    
    /**
     * Sets limited redo log capacity for this gateway
     * @param maxRedoLogCapacity redo log limit
     */
    public void setMaxRedoLogCapacity(Long maxRedoLogCapacity) {
        if (maxRedoLogCapacity != null && maxRedoLogCapacity < -1)
            throw new IllegalArgumentException("maxRedoLogCapacity must be larger than 0 or -1 for unlimited.");
        this.maxRedoLogCapacity = maxRedoLogCapacity;
    }

    /**
     * @return The limited redo log capacity for this gateway.
     */
    public Long getMaxRedoLogCapacity() {
        return maxRedoLogCapacity;
    }
    
    /**
     * Sets the behavior once the defined redo log capacity is exceeded, irrelevant if the capacity is unlimited.
     * @see #setMaxRedoLogCapacity(long)
     * @param onRedoLogCapacityExceeded
     */
    public void setOnRedoLogCapacityExceeded(RedoLogCapacityExceededPolicy onRedoLogCapacityExceeded) {
        this.onRedoLogCapacityExceeded = onRedoLogCapacityExceeded;
    }

    /**
     * Sets limited redo log capacity for this gateway
     * @param maxRedoLogCapacity redo log limit
     */
    public RedoLogCapacityExceededPolicy getOnRedoLogCapacityExceeded() {
        return onRedoLogCapacityExceeded;
    }
    /**
     * @return A new {@link GatewayPolicy} instance from the gateway target's properties.
     */
    public GatewayPolicy asGatewayPolicy() {
        return asGatewayPolicy(new GatewayPolicy());
    }
    
    /**
     * @return A new {@link GatewayPolicy} instance from the gateway target's properties, using provided default policy for properties which
     * are not explicitly set by this instance.
     */
    public GatewayPolicy asGatewayPolicy(GatewayPolicy defaultGatewayPolicy) {
        GatewayPolicy policy = new GatewayPolicy();
        policy.setGatewayName(name);
        if (bulkSize != null)
            policy.setBulkSize(bulkSize.intValue());
        else
            policy.setBulkSize(defaultGatewayPolicy.getBulkSize());
        if (idleTimeThreshold != null)
            policy.setIdleTimeThreshold(idleTimeThreshold.longValue());
        else
            policy.setIdleTimeThreshold(defaultGatewayPolicy.getIdleTimeThreshold());
        if (pendingOperationThreshold != null)
            policy.setPendingOperationThreshold(pendingOperationThreshold.intValue());
        else
            policy.setPendingOperationThreshold(defaultGatewayPolicy.getPendingOperationThreshold());
        if (maxRedoLogCapacity != null)
            policy.setMaxRedoLogCapacity(maxRedoLogCapacity.longValue());
        else
            policy.setMaxRedoLogCapacity(defaultGatewayPolicy.getMaxRedoLogCapacity());
        if (onRedoLogCapacityExceeded != null)
            policy.setOnRedoLogCapacityExceeded(onRedoLogCapacityExceeded);
        else
            policy.setOnRedoLogCapacityExceeded(defaultGatewayPolicy.getOnRedoLogCapacityExceeded());
        return policy;
    }
    
}
