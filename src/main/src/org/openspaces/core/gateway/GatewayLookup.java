package org.openspaces.core.gateway;

/**
 * 
 * @author Idan Moyal
 * @since 8.0.2
 *
 */
public class GatewayLookup {

    private String gatewayName;
    private String host;
    private int lusPort;
    private int lrmiPort;
    
    public GatewayLookup() {
    }

    public String getGatewayName() {
        return gatewayName;
    }
    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }
    public String getHost() {
        return host;
    }
    public void setHost(String ipAddress) {
        this.host = ipAddress;
    }
    //TODO WAN: rename to discovery port
    public int getLusPort() {
        return lusPort;
    }
    public void setLusPort(int lusPort) {
        this.lusPort = lusPort;
    }
    public int getLrmiPort() {
        return lrmiPort;
    }
    public void setLrmiPort(int lrmiPort) {
        this.lrmiPort = lrmiPort;
    }
    
}
