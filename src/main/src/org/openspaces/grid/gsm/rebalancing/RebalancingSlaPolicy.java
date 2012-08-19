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
package org.openspaces.grid.gsm.rebalancing;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerAgent;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementPolicy;

public class RebalancingSlaPolicy extends ServiceLevelAgreementPolicy {
    
    private GridServiceContainer[] containers;
    private int maxNumberOfConcurrentRelocationsPerMachine;
    private ProcessingUnitSchemaConfig schema;
    private CapacityRequirementsPerAgent allocatedCapacity;
    private int minimumNumberOfInstancesPerPartition;
    
    public void setContainers(GridServiceContainer[] containers) {
        this.containers = containers;
    }
    
    public GridServiceContainer[] getContainers() {
        return containers;
    }
    
    public void setMaximumNumberOfConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        this.maxNumberOfConcurrentRelocationsPerMachine = maxNumberOfConcurrentRelocationsPerMachine;
    }
    
    public int getMaximumNumberOfConcurrentRelocationsPerMachine() {
        return maxNumberOfConcurrentRelocationsPerMachine;
    }

    public ProcessingUnitSchemaConfig getSchemaConfig() {
        return schema;
    }
    
    public void setSchemaConfig(ProcessingUnitSchemaConfig schemaConfig) {
        this.schema = schemaConfig;
    }
    
    public CapacityRequirementsPerAgent getAllocatedCapacity() {
        return allocatedCapacity;
    }
    
    public void setAllocatedCapacity(CapacityRequirementsPerAgent allocatedCapacity) {
        this.allocatedCapacity = allocatedCapacity;
    }

    @Override
    public void validate() throws IllegalArgumentException {

        if (containers == null) {
            throw new IllegalArgumentException ("containers cannot be null");
        }
        
        if (maxNumberOfConcurrentRelocationsPerMachine <= 0) {
            throw new IllegalArgumentException("maxNumberOfConcurrentRelocationsPerMachine must be positive");
        }
        
        if (schema == null) {
            throw new IllegalArgumentException("PU schema cannot be null");
        }
        
        if (allocatedCapacity == null) {
            throw new IllegalArgumentException("allocatedCapacity cannot be null");
        }
        
        if (minimumNumberOfInstancesPerPartition < 0) {
            throw new IllegalArgumentException("minimumNumberOfInstancesPerPartition must be zero or positive");
        }
    }

    public int getMinimumNumberOfInstancesPerPartition() {
        return minimumNumberOfInstancesPerPartition;
    }

    public void setMinimumNumberOfInstancesPerPartition(int minimumNumberOfInstancesPerPartition) {
        this.minimumNumberOfInstancesPerPartition = minimumNumberOfInstancesPerPartition;
    }
}
