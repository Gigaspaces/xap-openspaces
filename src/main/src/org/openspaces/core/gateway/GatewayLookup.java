package org.openspaces.core.gateway;

/**
 * Holds information used for gateway lookup. 
 * 
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewayLookup {

    private String gatewayName;
    private String host;
    private int lusPort;
    private int lrmiPort;
    
    public GatewayLookup() {
    }

    /**
     * @return The gateway's name used for identification.
     */
    public String getGatewayName() {
        return gatewayName;
    }
    
    /**
     * Sets the gateway's name used for identification.
     * @param siteName
     */
    public void setGatewayName(String siteName) {
        this.gatewayName = siteName;
    }
    
    /**
     * @return The gateway's lookup host address (locator).
     */
    public String getHost() {
        return host;
    }
    
    /**
     * Sets the gateway's lookup host address (locator).
     * @param host The host address.
     */
    public void setHost(String host) {
        this.host = host;
    }
    
    /**
     * @return The gateway's lookup LUS port (Component's discovery port).
     */
    //TODO WAN: rename to discovery port
    public int getLusPort() {
        return lusPort;
    }
    
    /**
     * Sets the gateway's lookup LUS port (Component's discovery port).
     * @param lusPort
     */
    public void setLusPort(int lusPort) {
        this.lusPort = lusPort;
    }
    
    /**
     * @return The gateway's lookup LRMI port (Component's communication port).
     */
    public int getLrmiPort() {
        return lrmiPort;
    }
    
    /**
     * Sets the gateway's lookup LRMI port (Component's communication port).
     * @param lrmiPort The LRMI port.
     */
    public void setLrmiPort(int lrmiPort) {
        this.lrmiPort = lrmiPort;
    }
    
}
