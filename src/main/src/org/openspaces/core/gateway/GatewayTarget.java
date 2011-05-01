package org.openspaces.core.gateway;

import com.j_spaces.core.cluster.gateway.GatewayPolicy;

/**
 * 
 * @author Idan Moyal
 * @since 8.0.2
 *
 */
public class GatewayTarget {

    private String name;

    public GatewayTarget(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public GatewayPolicy asGatewayPolicy() {
        GatewayPolicy policy = new GatewayPolicy();
        policy.setGatewayName(name);
        return policy;
    }
    
}
