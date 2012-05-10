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
 * Holds gateway source configuration. 
 * 
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewaySource {

    private String name;
    
    public GatewaySource() {
    }
    public GatewaySource(String gatewaySourceName) {
        this.name = gatewaySourceName;
    }

    /**
     * @return Gateway's source name used for identification.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the gateway's source name used for identification.
     * @param name The gateway's source name.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public String toString() {
        return "GatewaySource [name=" + name + "]";
    }
}
