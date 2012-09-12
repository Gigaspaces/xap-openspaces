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
 * Holds gateway delegation settings.
 * {@link GatewayDelegation#getTarget()} specifies the delegation target name.
 * {@link GatewayDelegation#getDelegateThrough()} specifies the component name to delegate through.
 * 
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewayDelegation {
    
    private String target;
    private String delegateThrough;

    public GatewayDelegation() {
    }
    public GatewayDelegation(String target, String delegateThrough) {
        this.target = target;
        this.delegateThrough = delegateThrough;
    }

    /**
     * @return The delegation target name.
     */
    public String getTarget() {
        return target;
    }

    /**
     * @return The name of the component the delegation will be made through.
     */
    public String getDelegateThrough() {
        return delegateThrough;
    }
    
    /**
     * Sets the delegation target name.
     * @param target The delegation target name.
     */
    public void setTarget(String target) {
        this.target = target;
    }
    
    /**
     * Sets the name of the component the delegation will be made through.
     * @param delegateThrough The component name to delegate through.
     */
    public void setDelegateThrough(String delegateThrough) {
        this.delegateThrough = delegateThrough;
    }
    
    
    
}
