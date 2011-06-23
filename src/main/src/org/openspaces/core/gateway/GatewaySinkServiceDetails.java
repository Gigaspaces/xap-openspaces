package org.openspaces.core.gateway;

public class GatewaySinkServiceDetails extends GatewayServiceDetails {

    private static final long serialVersionUID = 1L;
    
    public GatewaySinkServiceDetails(String localGatewayName) {
        super(localGatewayName + "-sink", "gateway-sink", "gateway sink (" + localGatewayName + ")", "gateway sink (" + localGatewayName + ")", localGatewayName);
    }

}
