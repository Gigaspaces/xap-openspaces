/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.core.gateway;

import com.gigaspaces.sync.SyncEndPointInterceptor;

/**
 * A factory bean used for configuring the Sink component synchronization endpoint interceptor
 * configuration.
 * 
 * @author eitany
 * @since 9.0.1
 */
public class GatewaySinkSyncEndpointInterceptorFactoryBean {
    
    private SyncEndPointInterceptor interceptor;
    
    /**
     * Sets a custom synchronization endpoint interceptor that can be used provide custom behavior upon incoming replication events.
     */
    public void setInterceptor(SyncEndPointInterceptor interceptor) {
        this.interceptor = interceptor;
    }
    
    /**
     * Gets the custom synchronization endpoint interceptor that can be used provide custom behavior upon incoming replication events.
     */
    public SyncEndPointInterceptor getInterceptor() {
        return interceptor;
    }
}
