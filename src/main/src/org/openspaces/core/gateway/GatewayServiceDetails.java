package org.openspaces.core.gateway;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.openspaces.pu.service.PlainServiceDetails;

import com.gigaspaces.internal.io.IOUtils;

/**
 * Provide service details of a gateway
 * {@link AbstractGatewayComponentFactoryBean}
 * 
 * @author eitany
 * @since 8.0.3
 */
public class GatewayServiceDetails extends PlainServiceDetails {
    
    public static final String SERVICE_TYPE = "gateway";

    private static final long serialVersionUID = 1L;
    
    private String _localGatewayName;

    public GatewayServiceDetails(String id, String subserviceType, String description, String longDescription, String localGatewayName) {
        super(id, SERVICE_TYPE, subserviceType, description, longDescription);
        _localGatewayName = localGatewayName;
    }

    public String getLocalGatewayName() {
        return _localGatewayName;
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        IOUtils.writeRepetitiveString(out, _localGatewayName);
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        _localGatewayName = IOUtils.readRepetitiveString(in);
    }
}
