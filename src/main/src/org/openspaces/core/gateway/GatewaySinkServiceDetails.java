package org.openspaces.core.gateway;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.gigaspaces.internal.io.IOUtils;

/**
 * Provide service details of a {@link GatewaySinkFactoryBean}
 * @author eitany
 * @since 8.0.3
 */
public class GatewaySinkServiceDetails extends GatewayServiceDetails {

    private static final long serialVersionUID = 1L;
    private String[] _gatewaySourceNames;
    private boolean _requiresBootstrap;
    private String _localSpaceUrl;
    
    public GatewaySinkServiceDetails(String localGatewayName, String[] gatewaySourceNames, boolean requiresBootstrap, String localSpaceUrl) {
        super(localGatewayName + "-sink", "gateway-sink", "gateway sink (" + localGatewayName + ")", "gateway sink (" + localGatewayName + ")", localGatewayName);
        _gatewaySourceNames = gatewaySourceNames;
        _requiresBootstrap = requiresBootstrap;
        _localSpaceUrl = localSpaceUrl;
    }
   
    public String[] getGatewaySourceNames() {
        return _gatewaySourceNames;
    }
    
    public boolean requiresBootstrap() {
        return _requiresBootstrap;
    }
    
    public String getLocalSpaceUrl() {
        return _localSpaceUrl;
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        IOUtils.writeStringArray(out, _gatewaySourceNames);
        out.writeBoolean(_requiresBootstrap);
        IOUtils.writeString(out, _localSpaceUrl);
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        _gatewaySourceNames = IOUtils.readStringArray(in);
        _requiresBootstrap = in.readBoolean();
        _localSpaceUrl = IOUtils.readString(in);
    }

    

}
