package org.openspaces.core.gateway;

/**
 * Holds gateway source configuration. 
 * 
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewaySource {

    private String name;
    
    public GatewaySource(String gatewaySourceName) {
        this.name = gatewaySourceName;
    }

    /**
     * @return Gateway's source name used for identification.
     */
    public String getName() {
        return name;
    }
    
}
