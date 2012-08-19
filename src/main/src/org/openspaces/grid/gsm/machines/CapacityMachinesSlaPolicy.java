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
package org.openspaces.grid.gsm.machines;

import java.util.Set;

import org.openspaces.grid.gsm.capacity.CapacityRequirements;

public class CapacityMachinesSlaPolicy extends AbstractMachinesSlaPolicy {
 
    private CapacityRequirements capacityRequirements;
    private Set<String> zones;
    
    public CapacityRequirements getCapacityRequirements() {
        return capacityRequirements;
    }
    
    public void setCapacityRequirements(CapacityRequirements capacityRequirements) {
        this.capacityRequirements=capacityRequirements;
    }
    
    public boolean isStopMachineSupported() {
        return true;
    }

    @Override
    public String getScaleStrategyName() {
        return "Manual Capacity Scale Strategy";
    }

    public Set<String> getZones() {
        return zones;
    }

    public void setZones(Set<String> zones) {
        this.zones = zones;
    }

    @Override
    public void validate() throws IllegalArgumentException {
        
        super.validate();
        
        if (this.capacityRequirements == null) {
            throw new IllegalArgumentException ("capacityRequirements cannot be null");
        }
        
        if (this.zones == null) {
            throw new IllegalArgumentException("Zones cannot be null");
        }
    }

}
