package org.openspaces.core.gateway;

import java.util.List;

import com.gigaspaces.internal.cluster.node.impl.gateway.GatewayPolicy;
import com.gigaspaces.internal.cluster.node.impl.gateway.GatewaysPolicy;

/**
 * A factory bean for creating a {@link GatewaysPolicy} instance.
 *  
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewayTargetsFactoryBean {
    
    private String localGatewayName;
    private List<GatewayTarget> gatewayTargets;
    
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
    
}
