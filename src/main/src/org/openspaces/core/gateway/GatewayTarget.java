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
    public void setBulkSize(int bulkSize) {
        this.bulkSize = bulkSize;
    }
    
    /**
     * Sets the maximum time (in mili-seconds) pending replication packets should wait before being replicated if
     * {@link #setPendingOperationThreshold(int)} has not been breached.
     * @param idleTimeThreshold the maximum time (in mili-seconds).
     */
    public void setIdleTimeThreshold(long idleTimeThreshold) {
        this.idleTimeThreshold = idleTimeThreshold;
    }
    
    /**
     * Sets the threshold count for pending replication packets that once reached, the packets will be replicated using the
     * {@link #setBulkSize(int)}.
     * @param pendingOperationThreshold the threshold count.
     * @see #setIdleTimeThreshold(long)
     */
    public void setPendingOperationThreshold(int pendingOperationThreshold) {
        this.pendingOperationThreshold = pendingOperationThreshold;
    }
    
    /**
     * Sets unlimited replication redo log capacity for this gateway.
     */
    public void setUnlimitedRedoLogCapacity(){
        this.maxRedoLogCapacity = -1L;
    }
    
    /**
     * Sets limited redo log capacity for this gateway
     * @param maxRedoLogCapacity redo log limit
     */
    public void setMaxRedoLogCapacity(long maxRedoLogCapacity) {
        if (maxRedoLogCapacity <=0)
            throw new IllegalArgumentException("maxRedoLogCapacity must be larger than 0");
        this.maxRedoLogCapacity = maxRedoLogCapacity;
    }
    
    /**
     * Sets the behavior once the defined redo log capacity is exceeded, irrelevent if the capacity is unlimited.
     * @see #setMaxRedoLogCapacity(long)
     * @param onRedoLogCapacityExceeded
     */
    public void setOnRedoLogCapacityExceeded(RedoLogCapacityExceededPolicy onRedoLogCapacityExceeded) {
        this.onRedoLogCapacityExceeded = onRedoLogCapacityExceeded;
    }
    
    /**
     * @return A new {@link GatewayPolicy} instance from the gateway target's properties.
     */
    public GatewayPolicy asGatewayPolicy() {
        GatewayPolicy policy = new GatewayPolicy();
        policy.setGatewayName(name);
        if (bulkSize != null)
            policy.setBulkSize(bulkSize.intValue());
        if (idleTimeThreshold != null)
            policy.setIntervalMilis(idleTimeThreshold.longValue());
        if (pendingOperationThreshold != null)
            policy.setIntervalOperations(pendingOperationThreshold.intValue());
        if (maxRedoLogCapacity != null)
            policy.setMaxRedoLogCapacity(maxRedoLogCapacity.longValue());
        if (onRedoLogCapacityExceeded != null)
            policy.setOnRedoLogCapacityExceeded(onRedoLogCapacityExceeded);
        return policy;
    }
    
}
