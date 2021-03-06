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
package org.openspaces.grid.esm;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.jini.rio.monitor.event.Events;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;

/**
 * The Elastic Service Manager (ESM) provides the support to deploy, manage and
 * monitor elastic services in the Grid/Cloud.
 */
public interface ESM extends com.gigaspaces.grid.esm.ESM {

    void setProcessingUnitElasticProperties(String processingUnitName, Map<String, String> properties) throws RemoteException;

    void setProcessingUnitScaleStrategy(String puName, ScaleStrategyConfig scaleStrategyConfig) throws RemoteException;

    ScaleStrategyConfig getProcessingUnitScaleStrategyConfig(String processingUnitName) throws RemoteException;
    
    Events getScaleStrategyEvents(final long cursor, final int maxNumberOfEvents) throws RemoteException;
    
    Remote getRemoteApi(final String processingUnitName, final String apiName) throws RemoteException;
	
    /**
     * Disables failure detection for the agent on the machine that called this method.
     * This suppresses the creation of a new machine instead of the specified GSA if it is no longer running.
     * It does not suppress the healing of GSCs on that GSA.
     * 
     * Once the specified timeout expires, the failure detection is enabled automatically.
     * 
     * @since 9.7
     * @author itaif
     */
    void disableAgentFailureDetection(final String processingUnitName, final long timeout, final TimeUnit timeunit) throws RemoteException;
    
    /**
     * Enables failure detection for the agent on the machine that called this method.
     * 
     * @since 9.7
     * @author itaif
     */
    void enableAgentFailureDetection(final String processingUnitName) throws RemoteException;
}
