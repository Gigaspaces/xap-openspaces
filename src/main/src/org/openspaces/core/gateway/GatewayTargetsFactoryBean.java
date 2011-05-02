package org.openspaces.core.gateway;

import com.j_spaces.core.cluster.gateway.GatewayPolicy;
import com.j_spaces.core.cluster.gateway.GatewaysPolicy;

/**
 * A factory bean for creating a {@link GatewaysPolicy} instance.
 *  
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewayTargetsFactoryBean {
    
    private String localGatewayName;
    private GatewayTarget[] gatewayTargets;
    
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
    public GatewayTarget[] getGatewayTargets() {
        return gatewayTargets;
    }
    
    /**
     * Sets the gateway targets configuration.
     * @param gatewayTargets The gateway targets.
     */
    public void setGatewayTargets(GatewayTarget[] gatewayTargets) {
        this.gatewayTargets = gatewayTargets;
    }

    /**
     * @return A new {@link GatewaysPolicy} instance using the bean's properties.
     */
    public GatewaysPolicy asGatewaysPolicy() {
        GatewaysPolicy gatewaysPolicy = new GatewaysPolicy();
        gatewaysPolicy.setLocalSiteName(getLocalGatewayName());
        if (gatewayTargets != null) {
            GatewayPolicy[] policies = new GatewayPolicy[gatewayTargets.length];
            for (int i = 0; i < gatewayTargets.length; i++) {
                policies[i] = gatewayTargets[i].asGatewayPolicy();
            }
            gatewaysPolicy.setGatewayPolicies(policies);
        }
        return gatewaysPolicy;
    }
    
}
