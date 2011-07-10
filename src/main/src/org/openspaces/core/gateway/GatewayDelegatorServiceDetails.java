package org.openspaces.core.gateway;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.gigaspaces.internal.io.IOUtils;

/**
 * Provide service details of {@link GatewayDelegatorFactoryBean}
 * @author eitany
 * @since 8.0.4
 */
public class GatewayDelegatorServiceDetails extends GatewayServiceDetails {

    private static final long serialVersionUID = 1L;
    private GatewayDelegation[] _delegationTargets;
    
    public GatewayDelegatorServiceDetails(String localGatewayName, GatewayDelegation[] gatewayDelegations) {
        super(localGatewayName + "-delegator", "gateway-delegator", "gateway delegator (" + localGatewayName + ")", "gateway delegator (" + localGatewayName + ")", localGatewayName);
        _delegationTargets = gatewayDelegations;
    }

    public GatewayDelegation[] getDelegationTargets() {
        return _delegationTargets;
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        //GatewayDelegation is not serializable in 8.0.3, hence we must write custom external serialization code
        if (_delegationTargets == null)
            out.writeInt(-1);
        else        
        {
            out.writeInt(_delegationTargets.length);
            for (int i = 0; i < _delegationTargets.length; i++) {
                IOUtils.writeRepetitiveString(out, _delegationTargets[i].getTarget());
                IOUtils.writeRepetitiveString(out, _delegationTargets[i].getDelegateThrough());
            }
        }
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        final int length = in.readInt();
        if (length == -1)
            _delegationTargets = null;
        else
        {
            _delegationTargets = new GatewayDelegation[length];
            for (int i = 0; i < _delegationTargets.length; i++) {
                final String target = IOUtils.readRepetitiveString(in);
                final String delegateThrough = IOUtils.readRepetitiveString(in);
                _delegationTargets[i] = new GatewayDelegation(target, delegateThrough);
            }
        }
    }

}
