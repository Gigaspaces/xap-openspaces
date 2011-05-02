package org.openspaces.core.gateway;

import com.j_spaces.core.cluster.gateway.GatewayPolicy;

/**
 * Holds gateway target configuration.
 * 
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewayTarget {

    private String name;

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
     * @return A new {@link GatewayPolicy} instance from the gateway target's properties.
     */
    public GatewayPolicy asGatewayPolicy() {
        GatewayPolicy policy = new GatewayPolicy();
        policy.setGatewayName(name);
        return policy;
    }
    
}
