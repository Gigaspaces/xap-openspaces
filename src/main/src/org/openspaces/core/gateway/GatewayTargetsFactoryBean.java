package org.openspaces.core.gateway;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;

import com.gigaspaces.internal.cluster.node.impl.gateway.GatewayPolicy;
import com.gigaspaces.internal.cluster.node.impl.gateway.GatewaysPolicy;

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
        }
        return gatewaysPolicy;
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
            }
        }
    }
    
}
