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
package org.openspaces.grid.gsm.strategy;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.events.GridServiceAgentAddedEventListener;
import org.openspaces.admin.gsa.events.GridServiceAgentRemovedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.LogPerProcessingUnit;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
import org.openspaces.grid.gsm.machines.FutureGridServiceAgents;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;
import org.openspaces.grid.gsm.machines.exceptions.FailedToDiscoverMachinesException;
import org.openspaces.grid.gsm.machines.exceptions.WaitingForDiscoveredMachinesException;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioning;
import org.openspaces.grid.gsm.machines.plugins.exceptions.ElasticGridServiceAgentProvisioningException;
import org.openspaces.grid.gsm.machines.plugins.exceptions.ElasticMachineProvisioningException;


public class ElasticMachineProvisioningDiscoveredMachinesCache implements 
    DiscoveredMachinesCache,
    GridServiceAgentAddedEventListener, 
    GridServiceAgentRemovedEventListener , 
    Runnable{
    

    private static final long GET_DISCOVERED_MACHINES_TIMEOUT_SECONDS = 60;
    private static final long GET_DISCOVERED_MACHINES_RETRY_SECONDS = 60;
    
    
    private final ProcessingUnit pu;
    private final InternalAdmin admin;
    private final NonBlockingElasticMachineProvisioning machineProvisioning;
    private final boolean quiteMode;
    
    // created by afterPropertiesSet()
    private Log logger;
    private ScheduledFuture<?> scheduledTask;
    
    // state
    private boolean syncAgents;
    private FutureGridServiceAgents futureAgents;
    
    
    /**
     * Quite mode fall backs to the admin API (lookup discovery) in case of exceptions from the machine provisioning
     */
    public ElasticMachineProvisioningDiscoveredMachinesCache(
            ProcessingUnit pu, 
            NonBlockingElasticMachineProvisioning machineProvisioning,
            boolean quiteMode,
            long pollingIntervalSeconds) {
        
        this.pu = pu;
        
        this.admin = (InternalAdmin) pu.getAdmin();
        
        this.machineProvisioning = machineProvisioning;
        this.quiteMode = quiteMode;
        
        logger = new LogPerProcessingUnit(
                    new SingleThreadedPollingLog(
                            LogFactory.getLog(this.getClass())),
                    pu);
        
        admin.getGridServiceAgents().getGridServiceAgentAdded().add(this);
        admin.getGridServiceAgents().getGridServiceAgentRemoved().add(this);

        syncAgents = true;
        
        scheduledTask = 
            admin.scheduleWithFixedDelayNonBlockingStateChange(
                    this, 
                    0L, 
                    pollingIntervalSeconds, 
                    TimeUnit.SECONDS);
                
    }

    public void destroy() {
    
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
        
        admin.getGridServiceAgents().getGridServiceAgentAdded().remove(this);
        admin.getGridServiceAgents().getGridServiceAgentRemoved().remove(this);
       
    }
    
    public Collection<GridServiceAgent> getDiscoveredAgents() throws WaitingForDiscoveredMachinesException, FailedToDiscoverMachinesException {
        
        if (futureAgents == null || !futureAgents.isDone()) {
            throw new WaitingForDiscoveredMachinesException(pu,
                    "Need to wait until retrieved list of machines.");
        }
        
         
        GridServiceAgent[] agents = admin.getGridServiceAgents().getAgents(); // default value
        try {
            agents = futureAgents.get();

        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Error) {
                throw (Error)cause;
            }
            
            if (cause instanceof ElasticMachineProvisioningException || 
                cause instanceof ElasticGridServiceAgentProvisioningException || 
                cause instanceof AdminException ) {
                if (!quiteMode) {
                    throw new FailedToDiscoverMachinesException(pu, e);
                }
                logger.info("Failed to discover machines",e);
            }
            
            else {
                throw new IllegalStateException("Unexpected exception type",e);
            }
            
        } catch (TimeoutException e) {
            if (!quiteMode) {
                throw new FailedToDiscoverMachinesException(pu, e);
            }
            logger.info("Failed to discover machines",e);
        }
        return MachinesSlaUtils.sortAndFilterAgents(agents, machineProvisioning.getConfig(), logger);
    }

    public void gridServiceAgentRemoved(GridServiceAgent gridServiceAgent) {
        syncAgents = true;
    }

    public void gridServiceAgentAdded(GridServiceAgent gridServiceAgent) {
        syncAgents = true;
    }

    /**
     * Synchronizes the list of agents with the machine provisioning bean
     * We use the syncAgents flag to make sure there is no more than one concurrent call to machineProvisioning
     */
    public void run() {
        
        if (syncAgents) {
            syncAgents = false;
            if (logger.isDebugEnabled()) {
                logger.debug("Retrieving list of provisioned machines");
            }
            futureAgents = machineProvisioning.getDiscoveredMachinesAsync(GET_DISCOVERED_MACHINES_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        }
        
        if (futureAgents != null &&  futureAgents.getException() != null) {
            
            if (logger.isDebugEnabled()) {
                logger.debug("Failed retrieving list of machines. " +
                             "Retrying in " + GET_DISCOVERED_MACHINES_RETRY_SECONDS + " seconds.", futureAgents.getException());
            }
            
            admin.scheduleOneTimeWithDelayNonBlockingStateChange(new Runnable() {
    
                public void run() {
                   syncAgents = true;
                }},
                GET_DISCOVERED_MACHINES_RETRY_SECONDS, TimeUnit.SECONDS);
        }
    }
}
