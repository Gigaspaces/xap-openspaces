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
package org.openspaces.grid.gsm.machines.plugins.discovered;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningProgressChangedEventListener;
import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEventListener;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.config.DiscoveredMachineProvisioningConfig;
import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.openspaces.core.bean.Bean;
import org.openspaces.grid.gsm.ProcessingUnitAware;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.machines.FailedGridServiceAgent;
import org.openspaces.grid.gsm.machines.FutureCleanupCloudResources;
import org.openspaces.grid.gsm.machines.FutureGridServiceAgent;
import org.openspaces.grid.gsm.machines.FutureGridServiceAgents;
import org.openspaces.grid.gsm.machines.FutureStoppedMachine;
import org.openspaces.grid.gsm.machines.isolation.ElasticProcessingUnitMachineIsolation;
import org.openspaces.grid.gsm.machines.plugins.NonBlockingElasticMachineProvisioning;

public class DiscoveredMachineProvisioningBean implements NonBlockingElasticMachineProvisioning , Bean , ProcessingUnitAware {

    private Admin admin;
    private DiscoveredMachineProvisioningConfig config;

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public void setProperties(Map<String, String> properties) {
        this.config = new DiscoveredMachineProvisioningConfig(properties);
    }

    public Map<String, String> getProperties() {
        return config.getProperties();
    }

    public void afterPropertiesSet() throws Exception {
    }

    public void destroy() throws Exception {
    }

    public void setProcessingUnit(ProcessingUnit pu) {
    }

    public void setProcessingUnitSchema(ProcessingUnitSchemaConfig schemaConfig) {        
    }      

    public FutureGridServiceAgents getDiscoveredMachinesAsync(long duration, TimeUnit unit) {
        final Date timestamp = new Date();
        return new FutureGridServiceAgents() {
            
            public GridServiceAgent[] get() throws ExecutionException, IllegalStateException, TimeoutException {
                return admin.getGridServiceAgents().getAgents();
            }
   
            public boolean isDone() {
                return true;
            }
            
            public boolean isTimedOut() {
                return false;
            }
   
            public ExecutionException getException() {
                return null;
            }
   
            public Date getTimestamp() {
                return timestamp;
            }
        };
    }

    public ElasticMachineProvisioningConfig getConfig() {
        return config;
    }

    public boolean isStartMachineSupported() {
        return false;
    }

    public FutureStoppedMachine stopMachineAsync(final GridServiceAgent agent, long duration, TimeUnit unit) {
        
        final long start = System.currentTimeMillis();
        
        agent.shutdown();
        return new FutureStoppedMachine() {
            
            @Override
            public boolean isTimedOut() {
                return false;
            }
            
            @Override
            public boolean isDone() {
                return true;
            }
            
            @Override
            public Date getTimestamp() {
                return new Date(start);
            }
            
            @Override
            public ExecutionException getException() {
                return null;
            }
            
            @Override
            public Void get() throws ExecutionException, IllegalStateException, TimeoutException {
                return null;
            }

            @Override
            public GridServiceAgent getGridServiceAgent() {
                return agent;
            }
        };
    }

    @Override
    public void setElasticProcessingUnitMachineIsolation(ElasticProcessingUnitMachineIsolation isolation) {
        // not required
    }

    public FutureGridServiceAgent[] startMachinesAsync(
    		final CapacityRequirements capacityRequirements,
            final ExactZonesConfig zones,
            final FailedGridServiceAgent[] failedAgents,
            final long duration, final TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setElasticMachineProvisioningProgressChangedEventListener(
            ElasticMachineProvisioningProgressChangedEventListener machineEventListener) {
      //not used
    }

    @Override
    public void setElasticGridServiceAgentProvisioningProgressEventListener(
            ElasticGridServiceAgentProvisioningProgressChangedEventListener agentEventListener) {
        //not used
    }

	@Override
	public FutureCleanupCloudResources cleanupCloudResources(long duration,
			TimeUnit unit) {
	    //does nothing
		final long start = System.currentTimeMillis();
        
        return new FutureCleanupCloudResources() {
            
            private boolean mark;

			@Override
            public boolean isTimedOut() {
                return false;
            }
            
            @Override
            public boolean isDone() {
                return true;
            }
            
            @Override
            public Date getTimestamp() {
                return new Date(start);
            }
            
            @Override
            public ExecutionException getException() {
                return null;
            }
            
            @Override
            public Void get() throws ExecutionException, IllegalStateException, TimeoutException {
                return null;
            }

			@Override
			public boolean isMarked() {
				return mark;
			}

			@Override
			public void mark() {
				mark = true;
			}
        };
	}
}