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

import org.openspaces.pu.service.PlainServiceDetails;

import com.gigaspaces.internal.io.IOUtils;
import com.gigaspaces.internal.version.PlatformLogicalVersion;
import com.gigaspaces.lrmi.LRMIInvocationContext;

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
    private int _discoveryPort;
    private int _communicationPort;
    private boolean _embeddedLus;

    public GatewayServiceDetails() {
        super();
    }
    
    public GatewayServiceDetails(String id, String subserviceType,
            String description, String longDescription,
            String localGatewayName, int discoveryPort, int communicationPort,
            boolean embeddedLus)
    {
        super(id, SERVICE_TYPE, subserviceType, description, longDescription);
        _localGatewayName = localGatewayName;
        _discoveryPort = discoveryPort;
        _communicationPort = communicationPort;
        _embeddedLus = embeddedLus;
    }

    public String getLocalGatewayName() {
        return _localGatewayName;
    }
    
    public int getCommunicationPort() {
        return _communicationPort;
    }
    
    public int getDiscoveryPort() {
        return _discoveryPort;
    }
    
    public boolean isStartEmbeddedLus() {
        return _embeddedLus;
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        IOUtils.writeRepetitiveString(out, _localGatewayName);
        if (LRMIInvocationContext.getEndpointLogicalVersion().greaterOrEquals(PlatformLogicalVersion.v9_5_0)){
            out.writeInt(_communicationPort);
            out.writeInt(_discoveryPort);
            out.writeBoolean(_embeddedLus);
        }
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        _localGatewayName = IOUtils.readRepetitiveString(in);
        if (LRMIInvocationContext.getEndpointLogicalVersion().greaterOrEquals(PlatformLogicalVersion.v9_5_0)){
            _communicationPort = in.readInt();
            _discoveryPort = in.readInt();
            _embeddedLus = in.readBoolean();
        }
    }
}
