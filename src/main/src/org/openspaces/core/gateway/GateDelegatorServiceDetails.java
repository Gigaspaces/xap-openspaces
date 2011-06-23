package org.openspaces.core.gateway;

public class GateDelegatorServiceDetails extends GatewayServiceDetails {

    private static final long serialVersionUID = 1L;
    
    public GateDelegatorServiceDetails(String localGatewayName) {
        super(localGatewayName + "-delegator", "gateway-delegator", "gateway delegator (" + localGatewayName + ")", "gateway delegator (" + localGatewayName + ")", localGatewayName);
    }

}
