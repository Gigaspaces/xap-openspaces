package org.openspaces.core.gateway;

/**
 * 
 * @author Idan Moyal
 * @since 8.0.2
 *
 */
public class GatewaySource {

    private String name;
    
    public GatewaySource(String gatewaySourceName) {
        this.name = gatewaySourceName;
    }

    public String getName() {
        return name;
    }
    
}
