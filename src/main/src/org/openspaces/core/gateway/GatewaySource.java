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
    
    public GatewaySource() {
    }
    public GatewaySource(String gatewaySourceName) {
        this.name = gatewaySourceName;
    }

    /**
     * @return Gateway's source name used for identification.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the gateway's source name used for identification.
     * @param name The gateway's source name.
     */
    public void setName(String name) {
        this.name = name;
    }
    
}
