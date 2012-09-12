/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
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
    
    public static final String SERVICE_SUB_TYPE = "gateway-delegator";
    
    /**
     * For {@link java.io.Externalizable}
     */
    public GatewayDelegatorServiceDetails() {
    }
    
    public GatewayDelegatorServiceDetails(String localGatewayName, GatewayDelegation[] gatewayDelegations) {
        super(localGatewayName + "-delegator", SERVICE_SUB_TYPE, "gateway delegator (" + localGatewayName + ")", "gateway delegator (" + localGatewayName + ")", localGatewayName);
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
