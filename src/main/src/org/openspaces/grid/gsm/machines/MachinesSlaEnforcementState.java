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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.openspaces.admin.zone.config.ZonesConfig;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerAgent;
import org.openspaces.grid.gsm.containers.ContainersSlaUtils;
import org.openspaces.grid.gsm.machines.isolation.ElasticProcessingUnitMachineIsolation;
import org.openspaces.grid.gsm.machines.isolation.PublicMachineIsolation;

public class MachinesSlaEnforcementState {
    
    public static class StateKey {
        
        ProcessingUnit pu;
        ZonesConfig gridServiceAgentZones;
        
        public StateKey (ProcessingUnit pu, ZonesConfig gridServiceAgentZones) {
            this.pu = pu;
            this.gridServiceAgentZones = gridServiceAgentZones;
        }
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((gridServiceAgentZones == null) ? 0 : gridServiceAgentZones.hashCode());
            result = prime * result + ((pu == null) ? 0 : pu.hashCode());
            return result;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            StateKey other = (StateKey) obj;
            if (gridServiceAgentZones == null) {
                if (other.gridServiceAgentZones != null)
                    return false;
            } else if (!gridServiceAgentZones.equals(other.gridServiceAgentZones))
                return false;
            if (pu == null) {
                if (other.pu != null)
                    return false;
            } else if (!pu.equals(other.pu))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "StateKey ["
                    + (pu != null ? "pu=" + pu.getName() + ", " : "")
                    + (gridServiceAgentZones != null ? "agentZones=" + gridServiceAgentZones : "")
                    + "]";
        }
    }
    
    class StateValue {
        
        private CapacityRequirementsPerAgent allocatedCapacity = new CapacityRequirementsPerAgent();
        private final List<GridServiceAgentFutures> futureAgents = new ArrayList<GridServiceAgentFutures>();
        private CapacityRequirementsPerAgent markedForDeallocationCapacity = new CapacityRequirementsPerAgent();
        private ElasticProcessingUnitMachineIsolation machineIsolation;
        private Map<String,Long> timeoutTimestampPerAgentUidGoingDown = new HashMap<String,Long>();
        private boolean completedStateRecoveryAfterRestart;
        
        public void addFutureAgents(FutureGridServiceAgent[] newFutureAgents, CapacityRequirements capacityRequirements) {
            futureAgents.add(new GridServiceAgentFutures(newFutureAgents,capacityRequirements));
        }

        public void allocateCapacity(String agentUid, CapacityRequirements capacity) {
            if (machineIsolation == null) {
                throw new IllegalStateException(this + " should have set machine isolation before allocating capacity");
            }
            allocatedCapacity = allocatedCapacity.add(agentUid,capacity);
        }

        public void markCapacityForDeallocation(String agentUid, CapacityRequirements capacity) {
            if (machineIsolation == null) {
                throw new IllegalStateException(this + " should have set machine isolation before marking capacity for deallocation");
            }
            allocatedCapacity = allocatedCapacity.subtract(agentUid,capacity);
            markedForDeallocationCapacity = markedForDeallocationCapacity.add(agentUid, capacity);
        }

        public void unmarkCapacityForDeallocation(String agentUid, CapacityRequirements capacity) {
            if (machineIsolation == null) {
                throw new IllegalStateException(this + " should have set machine isolation before unmarking capacity for deallocation");
            }
            markedForDeallocationCapacity = markedForDeallocationCapacity.subtract(agentUid, capacity);
            allocatedCapacity = allocatedCapacity.add(agentUid,capacity);
        }

        public void deallocateCapacity(String agentUid, CapacityRequirements capacity) {
            if (machineIsolation == null) {
                throw new IllegalStateException(this + " should have set machine isolation before deallocating capacity");
            }
            markedForDeallocationCapacity = markedForDeallocationCapacity.subtract(agentUid, capacity);
        }

        public Collection<GridServiceAgentFutures> getAllDoneFutureAgents() {
            final List<GridServiceAgentFutures> doneFutures = new ArrayList<GridServiceAgentFutures>();
            
            for (GridServiceAgentFutures future : futureAgents) {
                
                if (future.isDone()) {
                    doneFutures.add(future);
                }
            }
            
            return doneFutures;
        }

        public void agentGoingDown(String agentUid, long timeout, TimeUnit unit) {
            if (machineIsolation == null) {
                throw new IllegalStateException(this + " should have set machine isolation before agentGoingDown");
            }
            timeoutTimestampPerAgentUidGoingDown.put(agentUid, System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(timeout, unit));
        }

        public Collection<String> getAgentUidsGoingDown() {
            return Collections.unmodifiableCollection(new ArrayList<String>(this.timeoutTimestampPerAgentUidGoingDown.keySet()));
        }

        public void removeFutureAgents(GridServiceAgentFutures futureAgentsToRemove) {
            if (machineIsolation == null) {
                throw new IllegalStateException(this + " should have set machine isolation before removing future agent");
            }
            futureAgents.remove(futureAgentsToRemove);
        }

        public void completedStateRecoveryAfterRestart() {
            completedStateRecoveryAfterRestart = true;
        }

        @Override
        public String toString() {
            return "StateValue ["
                    + (allocatedCapacity != null ? "allocatedCapacity=" + allocatedCapacity + ", " : "")
                    + (futureAgents != null ? "futureAgents=" + futureAgents + ", " : "")
                    + (markedForDeallocationCapacity != null ? "markedForDeallocationCapacity="
                            + markedForDeallocationCapacity + ", " : "")
                    + (machineIsolation != null ? "machineIsolation=" + machineIsolation + ", " : "")
                    + (timeoutTimestampPerAgentUidGoingDown != null ? "timeoutTimestampPerAgentUidGoingDown="
                            + timeoutTimestampPerAgentUidGoingDown + ", " : "") + "completedStateRecoveryAfterRestart="
                    + completedStateRecoveryAfterRestart + "]";
        }

        public boolean equalsZero() {
            return allocatedCapacity.equalsZero() 
                   && markedForDeallocationCapacity.equalsZero() 
                   && futureAgents.isEmpty() 
                   && timeoutTimestampPerAgentUidGoingDown.isEmpty();
        }
    }
    
    private final Log logger;
    
    private final Map<StateKey,StateValue> state;

    private final Set<ProcessingUnit> recoveredStatePerProcessingUnit;
    
    public MachinesSlaEnforcementState() {
        this.logger = 
                new SingleThreadedPollingLog( 
                        LogFactory.getLog(DefaultMachinesSlaEnforcementEndpoint.class));
        
        state = new HashMap<StateKey,StateValue>();
        recoveredStatePerProcessingUnit = new HashSet<ProcessingUnit>();
    }

    public boolean isHoldingStateForProcessingUnit(ProcessingUnit pu) {
        return !getGridServiceAgentsZones(pu).isEmpty();
    }

    private StateValue getState(StateKey key) {
        if (!state.containsKey(key)) {
            state.put(key, new StateValue());
        }
        return state.get(key);
    }
    
    public void addFutureAgents(StateKey key, FutureGridServiceAgent[] futureAgents, CapacityRequirements capacityRequirements) {
        getState(key).addFutureAgents(futureAgents, capacityRequirements);
    }
    

    public void allocateCapacity(StateKey key, String agentUid, CapacityRequirements capacity) {
        getState(key).allocateCapacity(agentUid, capacity);        
    }

    public void markCapacityForDeallocation(StateKey key, String agentUid, CapacityRequirements capacity) {
        getState(key).markCapacityForDeallocation(agentUid, capacity);
    }
    
    public void unmarkCapacityForDeallocation(StateKey key, String agentUid, CapacityRequirements capacity) {
        getState(key).unmarkCapacityForDeallocation(agentUid, capacity);        
    }

    
    public void deallocateCapacity(StateKey key, String agentUid, CapacityRequirements capacity) {
        getState(key).deallocateCapacity(agentUid, capacity);
    }

    public CapacityRequirementsPerAgent getCapacityMarkedForDeallocation(StateKey key) {
       return getState(key).markedForDeallocationCapacity;
    }

    public CapacityRequirementsPerAgent getAllocatedCapacity(StateKey key) {
        return getState(key).allocatedCapacity;
    }
    
    public CapacityRequirementsPerAgent getAllocatedCapacityOfOtherKeysFromSamePu(StateKey key) {
        CapacityRequirementsPerAgent capacityRequirementsPerAgent =  new CapacityRequirementsPerAgent();
        for (Entry<StateKey, StateValue> pair : state.entrySet()) {
            StateKey otherKey = pair.getKey();
            if (otherKey.pu.equals(key.pu) && 
                !otherKey.gridServiceAgentZones.equals(key.gridServiceAgentZones)) {
                //same pu, different agent zone
                CapacityRequirementsPerAgent otherAllocatedCapacity = getAllocatedCapacity(otherKey);
                capacityRequirementsPerAgent = capacityRequirementsPerAgent.add(otherAllocatedCapacity);
            }
        }
        return capacityRequirementsPerAgent;
    }
    
    public int getNumberOfFutureAgents(StateKey key) {
        return getState(key).futureAgents.size();
    }

    public Collection<GridServiceAgentFutures> getFutureAgents(StateKey key) {
        return Collections.unmodifiableCollection(getState(key).futureAgents);
    }

    public Collection<GridServiceAgentFutures> getAllDoneFutureAgents(StateKey key) {
        return getState(key).getAllDoneFutureAgents();
    }
    
    /**
     * Lists all grid service agents from all processing units including those that are marked for deallocation.
     */
    public Collection<String> getAllUsedAgentUids() {
        
        return getAllUsedCapacity().getAgentUids();
    }
    
    /**
     * Lists all capacity from all processing units including those that are marked for deallocation.
     */
    public CapacityRequirementsPerAgent getAllUsedCapacity() {
        
        CapacityRequirementsPerAgent allUsedCapacity = new CapacityRequirementsPerAgent();
        
        for (StateValue value : state.values()) {
            allUsedCapacity = allUsedCapacity.add(value.allocatedCapacity).add(value.markedForDeallocationCapacity);
        }
        
        return allUsedCapacity;
    }

    /**
     * @return true if processing units other than the specified PU, also use the specified agent. false otherwise.
     */
    public boolean isAgentSharedWithOtherProcessingUnits(ProcessingUnit pu, String agentUid) {
        
        for (Entry<StateKey, StateValue>  pair : state.entrySet()) {
            if (pair.getKey().pu.equals(pu)) {
                continue;
            }

            StateValue value = pair.getValue();
            if (!value.allocatedCapacity.getAgentCapacityOrZero(agentUid).equalsZero() ||
                !value.markedForDeallocationCapacity.getAgentCapacityOrZero(agentUid).equalsZero()) {
                return true;
            }
        }
        return false;
    }

    public void agentGoingDown(StateKey key, String agentUid, long timeout, TimeUnit unit) {
        getState(key).agentGoingDown(agentUid, timeout, unit);
    }

    public void agentShutdownComplete(String agentUid) {
        for (StateValue value : state.values()) {
            if (value.timeoutTimestampPerAgentUidGoingDown.containsKey(agentUid)) {
                value.timeoutTimestampPerAgentUidGoingDown.remove(agentUid);
                return;
            }
        }
        throw new IllegalArgumentException("agentUid");
    }

    public Collection<String> getAgentUidsGoingDown(StateKey key) {
        return getState(key).getAgentUidsGoingDown();
    }
    
    public boolean isAgentUidGoingDownTimedOut(String agentUid) {
        
        for (StateValue value : state.values()) {
            if (value.timeoutTimestampPerAgentUidGoingDown.containsKey(agentUid)) {
                return value.timeoutTimestampPerAgentUidGoingDown.get(agentUid) < System.currentTimeMillis();
            }
        }
        
        throw new IllegalArgumentException("agentUid");
    }

    public void markAgentCapacityForDeallocation(StateKey key, String uid) {
        CapacityRequirements agentCapacity = getAllocatedCapacity(key).getAgentCapacity(uid);
        markCapacityForDeallocation(key, uid,agentCapacity);
    }

    public void deallocateAgentCapacity(StateKey key, String agentUid) {
        CapacityRequirements agentCapacity = getCapacityMarkedForDeallocation(key).getAgentCapacity(agentUid);
        deallocateCapacity(key, agentUid , agentCapacity);
    }

    /**
     * @param exactZones - the exact zones that the grid service agent should have
     * @return all Grid Service Agent UIDs that the specified PU cannot be deploy on due to machine isolation restrictions
     * or due to the fact that the machine is about to be deployed by another PU that started it.
     * The map keys contain the agent UIDs, and the map values contains the reasons for the restriction.
     */
    public Map<String,List<String>> getRestrictedAgentUids(StateKey key) {
        
        Admin admin = key.pu.getAdmin();
        ElasticProcessingUnitMachineIsolation puIsolation = getState(key).machineIsolation;
        Map<String,List<String>> restrictedAgentUidsWithReason = new HashMap<String,List<String>>();
         if (!(puIsolation instanceof PublicMachineIsolation)) {
             //find all PUs with different machine isolation, and same machine isolation
             Collection<StateKey> keysWithDifferentIsolation = new HashSet<StateKey>();
             Collection<StateKey> keysWithSameIsolation = new HashSet<StateKey>();

             for (Entry<StateKey, StateValue> pair : state.entrySet()) {
                 ElasticProcessingUnitMachineIsolation otherPuIsolation = pair.getValue().machineIsolation;
                 if (otherPuIsolation == null) {
                     throw new IllegalStateException(pair.getKey() + " should have set machine isolation");
                 }
                 if (otherPuIsolation.equals(puIsolation)) {
                     keysWithSameIsolation.add(pair.getKey());
                 }
                 else {
                     keysWithDifferentIsolation.add(pair.getKey());
                 }
             }

             // add all agent uids used by conflicting pus
             if (logger.isDebugEnabled()) {
                 logger.debug("PUs with different isolation than pu " + key +" are: "+ keysWithDifferentIsolation);
                 logger.debug("PUs with same isolation of " + key + " are: " + keysWithSameIsolation);
             }

             for (StateKey otherKey: keysWithDifferentIsolation) {

                 StateValue otherValue = getState(otherKey);

                 for (String agentUid : otherValue.allocatedCapacity.getAgentUids()) {
                     initValue(restrictedAgentUidsWithReason, agentUid);
                     restrictedAgentUidsWithReason.get(agentUid).add(otherKey.pu + "machineIsolation=" + getState(otherKey).machineIsolation + " allocated on machine which restricts  " + key.pu + " machineIsolation="+getState(key).machineIsolation);
                 }

                 for (String agentUid : otherValue.markedForDeallocationCapacity.getAgentUids()) {
                     initValue(restrictedAgentUidsWithReason, agentUid);
                     restrictedAgentUidsWithReason.get(agentUid).add(otherKey.pu + "machineIsolation=" + getState(otherKey).machineIsolation + " marked for deallocation on machine which restricts  " + key.pu + " machineIsolation="+getState(key).machineIsolation);
                 }

                 for (String agentUid : otherValue.timeoutTimestampPerAgentUidGoingDown.keySet()) {
                     initValue(restrictedAgentUidsWithReason, agentUid);
                     restrictedAgentUidsWithReason.get(agentUid).add(otherKey.pu + "machineIsolation=" + getState(otherKey).machineIsolation + " is shutting down the agent which restricts  " + key.pu + " machineIsolation="+getState(key).machineIsolation);
                 }
             }

             // add all agents that started containers that are not with the same isolation
             Set<ZonesConfig> allowedContainerZoness = new HashSet<ZonesConfig>();
             for (StateKey otherKey : keysWithSameIsolation) {
                 allowedContainerZoness.add(otherKey.pu.getRequiredContainerZones());
             }
             for (GridServiceContainer container : admin.getGridServiceContainers()) {
                 if (container.getGridServiceAgent() == null) {
                     // ignore manually started containers using gsc.bat
                     continue;
                 }
                 boolean allowed = false;
                 for (ZonesConfig allowedContainerZones : allowedContainerZoness) {
                     if (container.getExactZones().isStasfies(allowedContainerZones)) {
                         allowed = true;
                         break;
                     }
                 }
                 if (!allowed) {
                     String agentUid = container.getGridServiceAgent().getUid();
                     initValue(restrictedAgentUidsWithReason, agentUid);
                     restrictedAgentUidsWithReason.get(agentUid).add("Machine has a container with restricted zones " + ContainersSlaUtils.gscToString(container));
                 }
             }
        }
        
        // add all future grid service agents that have been started but not allocated yet
        for (Entry<StateKey, StateValue> pair : state.entrySet()) {
            for (GridServiceAgentFutures futureAgents: pair.getValue().futureAgents) {
                for (GridServiceAgent agent : futureAgents.getGridServiceAgents()) {
                    String agentUid = agent.getUid();
                    initValue(restrictedAgentUidsWithReason, agentUid);
                    restrictedAgentUidsWithReason.get(agentUid).add("Agent has been started by " + pair.getKey() +" but not allocated yet");
                }
            }        
        }
        
        if (key.gridServiceAgentZones != null) {
            //add all agents that do not have this specific zone
            //notice that unlike MachinesSlaUtils#zoneFilter that only validates MachineProvisioning.getGSAZones()
            //this check is more restrictive. The machine is ok, just we cannot deploy on it with the given key
            for (GridServiceAgent agent : admin.getGridServiceAgents()) {
                if (!agent.getExactZones().isStasfies(key.gridServiceAgentZones)) {
                    String agentUid = agent.getUid();
                    initValue(restrictedAgentUidsWithReason, agentUid);
                    restrictedAgentUidsWithReason.get(agentUid).add("Agent zones=" + agent.getExactZones().getZones() +" does not match " + key.gridServiceAgentZones);
                }
            }
        }
        
        return restrictedAgentUidsWithReason;
   }

    private void initValue(Map<String, List<String>> mapOfLists, String key) {
        if (!mapOfLists.containsKey(key)) {
            mapOfLists.put(key, new LinkedList<String>());
        }
    }

    /**
     * @return all processing units that have a share of the specified future machine
     * not implemented yet. See GS-9484
     */
    public String[] getProcessingUnitsOfFutureMachine(ProcessingUnit pu, FutureGridServiceAgent futureAgent) {
        
        return new String[] {pu.getName()};
    }
    
    public void removeFutureAgents(StateKey key, GridServiceAgentFutures futureAgents) {
        getState(key).removeFutureAgents(futureAgents);
    }
    
    public Collection<String> getUsedAgentUids(StateKey key) {
        StateValue stateValue = getState(key);
        return stateValue.allocatedCapacity.add(stateValue.markedForDeallocationCapacity)
               .getAgentUids();
    }
    
    public void setMachineIsolation(StateKey key, ElasticProcessingUnitMachineIsolation isolation) {
        
        if (isolation == null) {
            throw new IllegalArgumentException("machine isolation cannot be null");
        }
        if (logger.isDebugEnabled()) {
            logger.debug(key + " machine isolation is " + isolation);
        }
        getState(key).machineIsolation = isolation;
    }
    
    public ElasticProcessingUnitMachineIsolation getMachineIsolation(StateKey key) {
        ElasticProcessingUnitMachineIsolation machineIsolation = getState(key).machineIsolation;
        if (machineIsolation == null) {
            throw new IllegalStateException(key + " machine isolation has not been defined");
        }
        return machineIsolation;
    }

    public boolean isCompletedStateRecovery(StateKey key) {
        return getState(key).completedStateRecoveryAfterRestart;
    }

    public void completedStateRecovery(StateKey key) {
        getState(key).completedStateRecoveryAfterRestart();      
    }

    public void recoveredStateOnEsmStart(ProcessingUnit otherPu) {
        recoveredStatePerProcessingUnit.add(otherPu);
    }

    public boolean isRecoveredStateOnEsmStart(ProcessingUnit pu) {
        return recoveredStatePerProcessingUnit.contains(pu);
    }

    public Set<ZonesConfig> getGridServiceAgentsZones(ProcessingUnit pu) {
        Set<ZonesConfig> zones = new HashSet<ZonesConfig>();
        for (StateKey key : state.keySet()) {
            if (key.pu.equals(pu)) {
                zones.add(key.gridServiceAgentZones);
            }
        }
        return zones;
    }
    
    public Map<GridServiceAgent, Map<ProcessingUnit, CapacityRequirements>> groupCapacityPerProcessingUnitPerAgent(StateKey key) {
        
        // create a report for each relevant agent - which pus are installed on it and how much capacity they are using
        final Map<GridServiceAgent,Map<ProcessingUnit,CapacityRequirements>> capacityPerPuPerAgent = new HashMap<GridServiceAgent,Map<ProcessingUnit,CapacityRequirements>>();
        Admin admin = key.pu.getAdmin();
        Collection<String> restrictedAgentUids = getRestrictedAgentUids(key).keySet();
        for (Entry<StateKey, StateValue> pair : state.entrySet()) {
            ProcessingUnit otherPu = pair.getKey().pu;
            CapacityRequirementsPerAgent otherPuCapacityPerAgents = pair.getValue().allocatedCapacity;
            for (String agentUid : otherPuCapacityPerAgents.getAgentUids()) {
                GridServiceAgent agent = admin.getGridServiceAgents().getAgentByUID(agentUid);
                if (!restrictedAgentUids.contains(agentUid) && agent != null) {
                    
                    if (!capacityPerPuPerAgent.containsKey(agent)) {
                        //lazy init
                        capacityPerPuPerAgent.put(agent, new HashMap<ProcessingUnit, CapacityRequirements>());
                    }
                    
                    if (!capacityPerPuPerAgent.get(agent).containsKey(otherPu)) {
                        capacityPerPuPerAgent.get(agent).put(otherPu, new CapacityRequirements());
                    }
                     
                    CapacityRequirements otherPuCapacityOnAgent = capacityPerPuPerAgent.get(agent).get(otherPu);
                    CapacityRequirements otherPuCapacityOnAgentIncrease = otherPuCapacityPerAgents.getAgentCapacity(agentUid);
                    otherPuCapacityOnAgent = otherPuCapacityOnAgent.add(otherPuCapacityOnAgentIncrease);
                    capacityPerPuPerAgent.get(agent).put(otherPu, otherPuCapacityOnAgent);
                }
            }
        }
        return capacityPerPuPerAgent;
    }
    
    public CapacityRequirementsPerAgent getAllocatedCapacity(ProcessingUnit otherPu) {
        CapacityRequirementsPerAgent capacityRequirementsPerAgent =  new CapacityRequirementsPerAgent();
        for (ZonesConfig  zones : getGridServiceAgentsZones(otherPu)) {
            capacityRequirementsPerAgent = capacityRequirementsPerAgent.add(getAllocatedCapacity(new StateKey(otherPu,zones)));
        }
        return capacityRequirementsPerAgent;
    }
    
    
    
    

    /**
     * Changes the key.zone of the allocated capacity to match the exact zone of the agent
     * @return false if nothing changed, true if replace occurred
     */
    public boolean replaceAllocatedCapacity(StateKey key, Admin admin) {
        boolean changed = false;
        final CapacityRequirementsPerAgent allocatedCapacityPerAgent  = getState(key).allocatedCapacity;
        Collection<String> agentUids = new ArrayList<String>(allocatedCapacityPerAgent.getAgentUids()); //copy before iteration
        for (String agentUid : agentUids) {
            final ExactZonesConfig agentZones = admin.getGridServiceAgents().getAgentByUID(agentUid).getExactZones();
            if (!key.gridServiceAgentZones.equals(agentZones)) {
                // the key.agentZones is different than agentZones
                // move allocation from key.agentZones to agentZones
                final CapacityRequirements capacity = allocatedCapacityPerAgent.getAgentCapacity(agentUid);
                markCapacityForDeallocation(key, agentUid, capacity);
                deallocateCapacity(key, agentUid, capacity);
                final StateKey newKey = new StateKey(key.pu, agentZones);
                setMachineIsolation(newKey, getMachineIsolation(key));
                allocateCapacity(newKey, agentUid, capacity);
                changed = true;
            }
        }
        if (getState(key).allocatedCapacity.equalsZero()) {
            removeKey(key);
        }
        return changed;
    }

    private void removeKey(StateKey key) {
        if (!getState(key).equalsZero()) {
            throw new IllegalStateException("Cannot remove " + key + " since it does not equal zero " + getState(key));
        }
        state.remove(key);
    }

    /**
     * Removes all state related to the specified processing unit
     * Call this method only if you are not going to call any other state method on this pu
     */
    public void removeAllocatedCapacity(ProcessingUnit pu) {
        Iterator<StateKey> stateKeyIterator = state.keySet().iterator();
        while(stateKeyIterator.hasNext()) {
            ProcessingUnit statePu = stateKeyIterator.next().pu; 
            if (statePu.equals(pu)) {
                stateKeyIterator.remove();
            }
        }
    }

    public boolean isAllocatedCapacityRemoved(ProcessingUnit pu) {
        boolean removed = true;
        for (StateKey key : state.keySet()) {
            if (key.pu.equals(pu)) {
                removed = false;
                break;
            }
        }
        return removed;
    }
}
