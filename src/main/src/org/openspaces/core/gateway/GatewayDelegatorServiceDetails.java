package org.openspaces.core.gateway;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.openspaces.admin.gateway.IDelegation;

import com.gigaspaces.internal.io.IOUtils;

/**
 * Provide service details of {@link GatewayDelegatorFactoryBean}
 * @author eitany
 * @since 8.0.3
 */
public class GatewayDelegatorServiceDetails extends GatewayServiceDetails {

    private static final long serialVersionUID = 1L;
    private IDelegation[] _delegationTargets;
    
    public GatewayDelegatorServiceDetails(String localGatewayName, IDelegation[] delegationTargets) {
        super(localGatewayName + "-delegator", "gateway-delegator", "gateway delegator (" + localGatewayName + ")", "gateway delegator (" + localGatewayName + ")", localGatewayName);
        _delegationTargets = delegationTargets;
    }

    public IDelegation[] getDelegationTargets() {
        return _delegationTargets;
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        IOUtils.writeObject(out, _delegationTargets);
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        _delegationTargets = IOUtils.readObject(in);
    }

}
