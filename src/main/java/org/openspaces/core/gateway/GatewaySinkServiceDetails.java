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
 * Provide service details of a {@link GatewaySinkFactoryBean}
 * @author eitany
 * @since 8.0.3
 */
public class GatewaySinkServiceDetails extends GatewayServiceDetails {

    private static final long serialVersionUID = 1L;
    private String[] _gatewaySourceNames;
    private boolean _requiresBootstrap;
    private String _localSpaceUrl;
    
    public static final String SERVICE_SUB_TYPE = "gateway-sink";
    
    public GatewaySinkServiceDetails() {
        super();
    }
    
    public GatewaySinkServiceDetails(String localGatewayName, String[] gatewaySourceNames, boolean requiresBootstrap, String localSpaceUrl) {
        super(localGatewayName + "-sink", SERVICE_SUB_TYPE, "gateway sink (" + localGatewayName + ")", "gateway sink (" + localGatewayName + ")", localGatewayName);
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
