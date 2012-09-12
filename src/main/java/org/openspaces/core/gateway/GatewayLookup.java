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

/**
 * Holds information used for gateway lookup. 
 * 
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewayLookup {

    private String gatewayName;
    private String host;
    private String discoveryPort;
    private String communicationPort;
    
    public GatewayLookup() {
    }

    /**
     * @return The gateway's name used for identification.
     */
    public String getGatewayName() {
        return gatewayName;
    }
    
    /**
     * Sets the gateway's name used for identification.
     * @param siteName
     */
    public void setGatewayName(String siteName) {
        this.gatewayName = siteName;
    }
    
    /**
     * @return The gateway's lookup host address (locator).
     */
    public String getHost() {
        return host;
    }
    
    /**
     * Sets the gateway's lookup host address (locator).
     * @param host The host address.
     */
    public void setHost(String host) {
        this.host = host;
    }
    
    /**
     * @return The gateway's lookup LUS port (Component's discovery port).
     */
    public String getDiscoveryPort() {
        return discoveryPort;
    }
    
    /**
     * Sets the gateway's lookup LUS port (Component's discovery port).
     * @param discoveryPort
     */
    public void setDiscoveryPort(String discoveryPort) {
        this.discoveryPort = discoveryPort;
    }
    
    /**
     * @return The gateway's lookup communication port (Component's communication port).
     */
    public String getCommunicationPort() {
        return communicationPort;
    }
    
    /**
     * Sets the gateway's lookup communication port (Component's communication port).
     * @param lrmiPort The LRMI port.
     */
    public void setCommunicationPort(String lrmiPort) {
        this.communicationPort = lrmiPort;
    }
    
}
