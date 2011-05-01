package org.openspaces.core.gateway;

import com.j_spaces.core.cluster.gateway.GatewayPolicy;
import com.j_spaces.core.cluster.gateway.GatewaysPolicy;

/**
 * 
 * @author Idan Moyal
 * @since 8.0.2
 *
 */
public class GatewayTargetsFactoryBean {
    
    private String localGatewayName;
    private GatewayTarget[] gatewayTargets;
    
    public GatewayTargetsFactoryBean() {
    }

    public void setLocalGatewayName(String localGatewayName) {
        this.localGatewayName = localGatewayName;
    }
    public String getLocalGatewayName() {
        return localGatewayName;
    }
    
    public GatewayTarget[] getGatewayTargets() {
        return gatewayTargets;
    }
    public void setGatewayTargets(GatewayTarget[] gatewayTargets) {
        this.gatewayTargets = gatewayTargets;
    }

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
