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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.InternalProcessingUnit;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.grid.gsm.LogPerProcessingUnit;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CpuCapacityRequirement;
import org.openspaces.grid.gsm.rebalancing.exceptions.FutureProcessingUnitInstanceDeploymentException;
import org.openspaces.grid.gsm.rebalancing.exceptions.ProcessingUnitIsNotEvenlyDistributedAccrossMachinesException;
import org.openspaces.grid.gsm.rebalancing.exceptions.ProcessingUnitIsNotEvenlyDistributedAcrossContainersException;
import org.openspaces.grid.gsm.rebalancing.exceptions.ProcessingUnitIsNotInTactException;
import org.openspaces.grid.gsm.rebalancing.exceptions.RebalancingSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.rebalancing.exceptions.WrongContainerProcessingUnitRelocationException;

import com.gigaspaces.cluster.activeelection.SpaceMode;

class DefaultRebalancingSlaEnforcementEndpoint implements RebalancingSlaEnforcementEndpoint {

    //0.01 minimum cpu cores per machine
    private static final Fraction MIN_CPU_CORES_PER_MACHINE_FOR_REBALANCING = new Fraction(1,100); 
    private static final int DEPLOYMENT_TIMEOUT_FAILURE_SECONDS = 3600; // one hour
    private static final int DEPLOYMENT_TIMEOUT_FAILURE_FORGET_SECONDS = 3600; // one hour

    private final ProcessingUnit pu;
    private final RebalancingSlaEnforcementState state;
    
    // restart a primary as a last resort continuation state
    // when primary rebalancing algorithm fails, we use this state to restart primaries by partition number (hueristics)
    private int lastResortPartitionRestart = 0;
    private int lastResortPartitionRelocate = 0;

    private final Log logger;

    DefaultRebalancingSlaEnforcementEndpoint(ProcessingUnit pu, RebalancingSlaEnforcementState state) {
        if (pu == null) {
            throw new IllegalArgumentException("pu cannot be null");
        }
        
        this.pu = pu;
        this.state = state;
        this.logger = 
            new LogPerProcessingUnit(
                    new SingleThreadedPollingLog(
                            LogFactory.getLog(DefaultRebalancingSlaEnforcementEndpoint.class)),
                    pu);
    }

    public ProcessingUnit getProcessingUnit() {
        return pu;
    }

    public void enforceSla(RebalancingSlaPolicy sla)
            throws RebalancingSlaEnforcementInProgressException {
        
        if (state.isDestroyedProcessingUnit(pu)) {
            throw new IllegalStateException("endpoint destroyed");
        }

        if (sla == null) {
            throw new IllegalArgumentException("sla cannot be null");
        }
        
        sla.validate();
        
        for (GridServiceContainer container : sla.getContainers()) {
            if (container.getGridServiceAgent() == null) {
                throw new IllegalStateException("container " + RebalancingUtils.gscToString(container) + " has no agent.");
            }
            
            String agentUid = container.getGridServiceAgent().getUid();
            if (!sla.getAllocatedCapacity().getAgentUids().contains(agentUid)) {
                throw new IllegalArgumentException(
                        "List of agents must be a superset of agents that started the containers, "+
                        "agentUids="+sla.getAllocatedCapacity().getAgentUids().toString()+" "+
                        "does not include agent " + agentUid);
            }
            
            if (sla.getAllocatedCapacity().getAgentCapacity(agentUid).getRequirement(new CpuCapacityRequirement().getType()).equalsZero()) {
                // number of cpu cores per machine cannot be zero (requirement of the primary rebalancing algorithm)
                sla.setAllocatedCapacity(sla.getAllocatedCapacity().add(agentUid, new CapacityRequirements(new CpuCapacityRequirement(MIN_CPU_CORES_PER_MACHINE_FOR_REBALANCING))));
            }
        }
        
        String zone = pu.getRequiredZones()[0];

        for (GridServiceContainer container : sla.getContainers()) {
            Set<String> zones = container.getZones().keySet();

            if (zones.size() != 1) {
                throw new IllegalArgumentException("Container " + RebalancingUtils.gscToString(container)
                        + " must have exactly one zone.");
            }

            if (!zones.contains(zone)) {
                throw new IllegalArgumentException("Container " + RebalancingUtils.gscToString(container)
                        + " must have the zone " + zone);
            }
        }

        enforceSlaInternal(sla);
    }

    private void enforceSlaInternal(RebalancingSlaPolicy sla) throws RebalancingSlaEnforcementInProgressException {

        cleanFutureStatefulDeployments();
        cleanFutureStatelessDeployments();
        cleanRemovedStatelessProcessingUnitInstances();
        
        if (sla.getSchemaConfig().isPartitionedSync2BackupSchema()) {
            enfroceSlaStatefulProcessingUnit(sla);
        }
        else if (sla.getSchemaConfig().isDefaultSchema()) {
            enforceSlaStatelessProcessingUnit(sla);
        }
        else {
            throw new IllegalStateException(pu.getName() + " schema " + sla.getSchemaConfig().getSchema() + " is not supported." );
        }
        
    }

    private void enforceSlaStatelessProcessingUnit(RebalancingSlaPolicy sla) throws RebalancingSlaEnforcementInProgressException {
        
        GridServiceContainer[] containers = sla.getContainers();
        if (state.getNumberOfFutureDeployments(pu) > 0) {
            // incrementNumberOfStatelessInstancesAsync can be called only one at a time
            // if called concurrently they won't share state and it causes too many increment instance calls to the GSM.
            throw new ProcessingUnitIsNotEvenlyDistributedAcrossContainersException(pu, containers);
        }
            
        Collection<FutureStatelessProcessingUnitInstance> futureInstances = 
            RebalancingUtils.incrementNumberOfStatelessInstancesAsync(
                    pu, 
                    sla.getContainers(),
                    logger, 
                    DEPLOYMENT_TIMEOUT_FAILURE_SECONDS , TimeUnit.SECONDS);
        
        state.addFutureStatelessDeployments(futureInstances);
        
        if (state.getNumberOfFutureDeployments(pu) > 0) {
            throw new ProcessingUnitIsNotEvenlyDistributedAcrossContainersException(pu, containers);
        }
        
        // find all containers with instances that are not in the approved containers
        Set<GridServiceContainer> approvedContainers = new HashSet<GridServiceContainer>(Arrays.asList(containers));
        List<ProcessingUnitInstance> instancesToRemove = new ArrayList<ProcessingUnitInstance>();
       
        for (GridServiceContainer container : pu.getAdmin().getGridServiceContainers()) {
            if (!approvedContainers.contains(container)) {
                
                for (ProcessingUnitInstance instance : container.getProcessingUnitInstances(pu.getName())) {
                    instancesToRemove.add(instance);
                }
            }
        }
        
        if (instancesToRemove.size() > 0) {

            for (ProcessingUnitInstance instanceToRemove : instancesToRemove) {
                logger.info(
                        "removing pu instance " + RebalancingUtils.puInstanceToString(instanceToRemove) + " "+
                        "since not deployed on approved container");
                removeInstance(instanceToRemove);
            }
            
            throw new ProcessingUnitIsNotEvenlyDistributedAcrossContainersException(pu, containers);
        }
        
        if (state.getRemovedStatelessProcessingUnitInstances(pu).iterator().hasNext()) {
            throw new ProcessingUnitIsNotEvenlyDistributedAcrossContainersException(pu, containers);
        }
        
        if (containers.length < pu.getNumberOfInstances() &&
            pu.getInstances().length > 1) {
            // the number of instances is more than the sla.
            // there has been an sla changed that leaved us with too many instances.
            int numberOfInstancesBeforeDecrement = pu.getNumberOfInstances();
            boolean decremented = ((InternalProcessingUnit)pu).decrementPlannedInstances();
            if (decremented) {
                logger.info(
                        "Number of instances is " + numberOfInstancesBeforeDecrement + " "+
                        "instead of " + containers.length +". "+
                        "Removed one pu instance of " + pu.getName());
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Number of instances is " + numberOfInstancesBeforeDecrement + " "+
                            "instead of " + containers.length +". "+
                            "Retry to remove one pu instance of " + pu.getName() + " next time.");
                }
            }
            throw new ProcessingUnitIsNotInTactException(pu);
        }
        
        if (!RebalancingUtils.isProcessingUnitIntact(pu,containers)) {
            throw new ProcessingUnitIsNotInTactException(pu);
        }
    }
    
    private void removeInstance(final ProcessingUnitInstance instance) {
        
        if (instance.isDiscovered() &&
            !state.isStatelessProcessingUnitInstanceBeingRemoved(instance)) {
            
            // this makes sure we try to decrement it only once
            state.addRemovedStatelessProcessingUnitInstance(instance);
            
            ((InternalAdmin)pu.getAdmin()).scheduleAdminOperation(new Runnable() {

                public void run() {
                    try {
                        if (instance.isDiscovered()) {
                            instance.decrement();
                        }
                    }
                    catch (AdminException e) {
                        logger.info(
                                "Failed to remove instance " + RebalancingUtils.puInstanceToString(instance),e);
                    }
                    catch (Exception e) {
                        logger.warn("Unexpected exception when removing "+ RebalancingUtils.puInstanceToString(instance));
                    }
                }});
        }
    }
    
    private void enfroceSlaStatefulProcessingUnit(RebalancingSlaPolicy sla)
            throws RebalancingSlaEnforcementInProgressException {
        
        if (!RebalancingUtils.isProcessingUnitIntact(pu)) {
            throw new ProcessingUnitIsNotInTactException(pu);
        }
        
        GridServiceContainer[] containers = sla.getContainers();
        if (pu.getNumberOfBackups() == 1) {
            // stage 1 : relocate backups so number of instances per container is balanced
            boolean relocateOnlyBackups = true;
            rebalanceNumberOfInstancesPerContainer(containers, sla, relocateOnlyBackups);

            if (state.getNumberOfFutureDeployments(pu) > 0) {
                logger.debug(
                        "Rebalancing of backup instances is in progress after Stage 1. "+
                        "Number of deployments in progress is " + state.getNumberOfFutureDeployments(pu));
                throw new ProcessingUnitIsNotEvenlyDistributedAcrossContainersException(pu, containers);
            }
    
            // if not all of pu instances are in the approved containers...
            // then skip directly to stage 3
            if (RebalancingUtils.isProcessingUnitIntact(pu, containers)) {

                // stage 2: restart primaries so number of cpu cores per primary is balanced
                rebalanceNumberOfPrimaryInstancesPerMachine(containers, sla);

                if (state.getNumberOfFutureDeployments(pu) > 0) {
                    logger.debug(
                            "Restarting of primary instances is in progress after Stage 2. "+
                            "Number of deployments in progress is " + state.getNumberOfFutureDeployments(pu));
                    throw new ProcessingUnitIsNotEvenlyDistributedAccrossMachinesException(pu);
                }
            }
        }

        // stage 3: relocate backups or primaries so number of instances per container is
        // balanced
        boolean relocateOnlyBackups = false;
        rebalanceNumberOfInstancesPerContainer(containers, sla, relocateOnlyBackups);
        
        if (state.getNumberOfFutureDeployments(pu) > 0) {
            logger.debug(
                    "Rebalancing of primary or backup instances is in progress after Stage 3. "+
                    "Number of deployments in progress is " + state.getNumberOfFutureDeployments(pu));
            throw new ProcessingUnitIsNotEvenlyDistributedAcrossContainersException(pu, containers);
        }
        
        if (!RebalancingUtils.isProcessingUnitIntact(pu, containers)) {
            throw new ProcessingUnitIsNotInTactException(pu);
        }
    }

    /**
     * Invokes multiple relocation operations to balance number of pu instances per container.
     * 
     * @param containers
     * @param onlyBackups
     *            - perform only backup relocations.
     * 
     * @throws RebalancingSlaEnforcementInProgressException
     *             - cannot determine what next to relocate since another conflicting operation
     *             is in progress.
     */
    private void rebalanceNumberOfInstancesPerContainer(GridServiceContainer[] containers,
            RebalancingSlaPolicy sla, boolean relocateOnlyBackups) throws RebalancingSlaEnforcementInProgressException {

        while (true) {
            final FutureStatefulProcessingUnitInstance futureInstance = 
                rebalanceNumberOfInstancesPerContainerStep(
                    containers, relocateOnlyBackups, sla.getMaximumNumberOfConcurrentRelocationsPerMachine());

            if (futureInstance == null) {
                break;
            }

            state.addFutureStatefulDeployment(futureInstance);
        }
    }

    /**
     * Invokes one relocation operations to balance number of instances per container
     * 
     * @param pu
     * @param containers
     * @param onlyBackups
     *            - perform only backup relocations.
     * 
     * @return future if performed relocation. null if no action needs to be performed.
     * 
     * @throws RebalancingSlaEnforcementInProgressException
     *             - cannot determine what to relocate since another conflicting operation is in
     *             progress.
     */
    private FutureStatefulProcessingUnitInstance rebalanceNumberOfInstancesPerContainerStep(
            final GridServiceContainer[] containers, boolean onlyBackups, int maximumNumberOfRelocationsPerMachine)
            throws RebalancingSlaEnforcementInProgressException {

        // sort all containers (including those not in the specified containers
        // by (numberOfInstancesPerContainer - minNumberOfInstances)
        final List<GridServiceContainer> sortedContainers = RebalancingUtils.sortAllContainersByNumberOfInstancesAboveMinimum(
                pu, containers);

        logger.debug("Containers sorted by number of instances above minimum: " + RebalancingUtils.gscsToString(sortedContainers));
        
        boolean conflict = false;
        // relocation is done from a source container with too many instances
        // to a target container with too little instances
        for (int targetIndex = 0; targetIndex < sortedContainers.size(); targetIndex++) {

            GridServiceContainer target = sortedContainers.get(targetIndex);

            if (isConflictingDeploymentInProgress(target, maximumNumberOfRelocationsPerMachine)) {
                conflict = true;
                logger.debug("Cannot relocate instances to " + RebalancingUtils.gscToString(target)
                        + " since a conflicting relocation is already in progress.");
                continue;
            }

            int instancesInTarget = target.getProcessingUnitInstances(pu.getName()).length;
            if (instancesInTarget >= RebalancingUtils.getPlannedMaximumNumberOfInstancesForContainer(target,
                    containers, pu)) {
                // target cannot host any more instances
                // since the array is sorted there is no point in continuing the search
                break;
            }

            for (int sourceIndex = sortedContainers.size() - 1; sourceIndex > targetIndex; sourceIndex--) {

                GridServiceContainer source = sortedContainers.get(sourceIndex);

                if (isConflictingDeploymentInProgress(source, maximumNumberOfRelocationsPerMachine)) {
                    conflict = true;
                    logger.debug("Cannot relocate instances from " + RebalancingUtils.gscToString(source)
                            + " since a conflicting relocation is already in progress.");
                    continue;
                }

                int instancesInSource = source.getProcessingUnitInstances(pu.getName()).length;
                if (instancesInSource <= RebalancingUtils.getPlannedMinimumNumberOfInstancesForContainer(source,
                        containers, pu)) {
                    // source cannot give up any instances
                    // since the array is sorted there is no point in continuing the search
                    break;
                }

                if (instancesInTarget >= RebalancingUtils.getPlannedMinimumNumberOfInstancesForContainer(target,containers, pu)
                    && 
                    instancesInSource <= RebalancingUtils.getPlannedMaximumNumberOfInstancesForContainer(source, containers, pu)) {
                    // both source and target are balanced.
                    // since array is sorted there is no point in continuing the search
                    // as this condition will hold true.
                    break;
                }

                  // we have a target and a source container. 
                // now let's decide which pu instance to relocate from source to target
                for (ProcessingUnitInstance candidateInstance : source.getProcessingUnitInstances(pu.getName())) {

                    if (candidateInstance.getSpaceInstance() == null) {
                        logger.debug("Cannot relocate " + RebalancingUtils.puInstanceToString(candidateInstance)
                                + " since embedded space is not detected");
                        continue;
                    }

                    if (onlyBackups && candidateInstance.getSpaceInstance().getMode() != SpaceMode.BACKUP) {
                        logger.debug("Prefer not to relocate "
                                + RebalancingUtils.puInstanceToString(candidateInstance)
                                + " since it is not a backup, and backups are preffered for relocation");
                        continue;
                    }

                    if (!RebalancingUtils.isProcessingUnitPartitionIntact(candidateInstance)) {
                        logger.debug("Cannot relocate " + RebalancingUtils.puInstanceToString(candidateInstance)
                                + " since instances from the same partition are missing");
                        conflict = true;
                        continue;
                    }

                    if (isConflictingStatefulDeploymentInProgress(candidateInstance)) {
                        logger.debug("Cannot relocate " + RebalancingUtils.puInstanceToString(candidateInstance)
                                + " " + "since another instance from the same partition is being relocated");
                        conflict = true;
                        continue;
                    }

                    for (Machine sourceReplicationMachine : RebalancingUtils.getMachinesHostingContainers(RebalancingUtils.getReplicationSourceContainers(candidateInstance))) {
                        if (isConflictingOperationInProgress(sourceReplicationMachine,
                                maximumNumberOfRelocationsPerMachine)) {
                            logger.debug("Cannot relocate " + RebalancingUtils.puInstanceToString(candidateInstance)
                                    + " " + "since replication source is on machine "
                                    + RebalancingUtils.machineToString(sourceReplicationMachine) + " "
                                    + "which is busy with another relocation");
                            conflict = true;
                            continue;
                        }
                    }

                    // check limit of pu instances from same partition per container
                    if (pu.getMaxInstancesPerVM() > 0) {
                        int numberOfOtherInstancesFromPartitionInTargetContainer = RebalancingUtils.getOtherInstancesFromSamePartitionInContainer(
                                target, candidateInstance)
                            .size();

                        if (numberOfOtherInstancesFromPartitionInTargetContainer >= pu.getMaxInstancesPerVM()) {
                            logger.debug("Cannot relocate " + RebalancingUtils.puInstanceToString(candidateInstance)
                                    + " " + "to container " + RebalancingUtils.gscToString(target) + " "
                                    + "since container already hosts "
                                    + numberOfOtherInstancesFromPartitionInTargetContainer + " "
                                    + "instance(s) from the same partition.");
                            continue;
                        }
                    }

                    // check limit of pu instances from same partition per machine 
                    if (pu.getMaxInstancesPerMachine() > 0) {
                        int numberOfOtherInstancesFromPartitionInTargetMachine = 
                            RebalancingUtils.getOtherInstancesFromSamePartitionInMachine(
                                target.getMachine(), candidateInstance)
                            .size();

                        if (numberOfOtherInstancesFromPartitionInTargetMachine >= pu.getMaxInstancesPerMachine()) {
                            logger.debug("Cannot relocate " + RebalancingUtils.puInstanceToString(candidateInstance)
                                    + " " + "to container " + RebalancingUtils.gscToString(target) + " "
                                    + "since machine already contains "
                                    + numberOfOtherInstancesFromPartitionInTargetMachine + " "
                                    + "instance(s) from the same partition.");
                            continue;
                        }
                    }

                    logger.info("Relocating " + RebalancingUtils.puInstanceToString(candidateInstance) + " "
                            + "from " + RebalancingUtils.gscToString(source) + " " + "to "
                            + RebalancingUtils.gscToString(target));
                    return RebalancingUtils.relocateProcessingUnitInstanceAsync(target, candidateInstance,
                            logger, DEPLOYMENT_TIMEOUT_FAILURE_SECONDS, TimeUnit.SECONDS);

                }// for pu instance
            }// for source container
        }// for target container

        if (// we tried to relocate primaries
            !onlyBackups &&
            
             // backup instances exist and they are the reason we are here due to max instances per machine limitation
            pu.getNumberOfBackups() > 0 &&
            
            // no future operations that may conflict
            state.getNumberOfFutureDeployments(pu) == 0 &&
            
            // all instances are deployed
            RebalancingUtils.isProcessingUnitIntact(pu) &&
            
            // we're not done rebalancing yet!
            !RebalancingUtils.isEvenlyDistributedAcrossContainers(pu, containers)) {
            
            logger.debug("Optimal rebalancing hueristics failed balancing instances per container in this deployment. "+
            "Performing non-optimal relocation heuristics. Starting with partition " + lastResortPartitionRelocate);

            // algorithm failed. we need to use heuristics.
            // The reason the algorithm failed is that the machine that has an empty spot also has instances from partition that prevent a relocation into that machine.
            // For example, the excess machine wants to relocate Primary1 but the empty GSC is on a machine that has Backup1.
            // The workaround is to relocate any backup from another machine to the empty GSC, and so the "emptiness" would move to that other machine.
            // we look for backups by their partition number to avoid an endless loop.

            for (; lastResortPartitionRelocate < pu.getNumberOfInstances() - 1; lastResortPartitionRelocate++) {

                // find backup to relocate
                ProcessingUnitInstance candidateInstance = pu.getPartition(lastResortPartitionRelocate).getBackup();
                
                GridServiceContainer source = candidateInstance.getGridServiceContainer();
                
                for (int targetIndex = 0; targetIndex < sortedContainers.size(); targetIndex++) {

                    GridServiceContainer target = sortedContainers.get(targetIndex);
                    
                    if (target.getMachine().equals(source.getMachine())) {
                        // there's no point in relocating a backup into the same machine
                        // since we want another machine to have an "empty" container. 
                        continue;
                    }

                    int instancesInTarget = target.getProcessingUnitInstances(pu.getName()).length;
                    if (instancesInTarget >= RebalancingUtils.getPlannedMaximumNumberOfInstancesForContainer(
                            target, containers, pu)) {
                        // target cannot host any more instances
                        continue;
                    }

                    // check limit of pu instances from same partition per container
                    if (pu.getMaxInstancesPerVM() > 0) {
                        int numberOfOtherInstancesFromPartitionInTargetContainer = RebalancingUtils.getOtherInstancesFromSamePartitionInContainer(
                                target, candidateInstance)
                            .size();

                        if (numberOfOtherInstancesFromPartitionInTargetContainer >= pu.getMaxInstancesPerVM()) {
                            logger.debug("Cannot relocate " + RebalancingUtils.puInstanceToString(candidateInstance)
                                    + " " + "to container " + RebalancingUtils.gscToString(target) + " "
                                    + "since container already hosts "
                                    + numberOfOtherInstancesFromPartitionInTargetContainer + " "
                                    + "instance(s) from the same partition.");
                            continue;
                        }
                    }

                    // check limit of pu instances from same partition per machine
                    if (pu.getMaxInstancesPerMachine() > 0) {
                        int numberOfOtherInstancesFromPartitionInTargetMachine = RebalancingUtils.getOtherInstancesFromSamePartitionInMachine(
                                target.getMachine(), candidateInstance)
                            .size();

                        if (numberOfOtherInstancesFromPartitionInTargetMachine >= pu.getMaxInstancesPerMachine()) {
                            logger.debug("Cannot relocate " + RebalancingUtils.puInstanceToString(candidateInstance)
                                    + " " + "to container " + RebalancingUtils.gscToString(target) + " "
                                    + "since machine already contains "
                                    + numberOfOtherInstancesFromPartitionInTargetMachine + " "
                                    + "instance(s) from the same partition.");
                            continue;
                        }
                    }

                    logger.info("Relocating " + RebalancingUtils.puInstanceToString(candidateInstance) + " "
                            + "from " + RebalancingUtils.gscToString(source)
                            + " " + "to " + RebalancingUtils.gscToString(target));
                    return RebalancingUtils.relocateProcessingUnitInstanceAsync(target, candidateInstance,
                            logger, DEPLOYMENT_TIMEOUT_FAILURE_SECONDS, TimeUnit.SECONDS);

                }
            }

            // we haven't found any partition to relocate, probably the instance that requires
            // relocation has a partition lower than lastResortPartitionRelocate.

            if (lastResortPartitionRelocate >= pu.getNumberOfInstances() - 1) {
                lastResortPartitionRelocate = 0; // better luck next time. continuation programming
            }
        }
        
        if (conflict) {
            throw new RebalancingSlaEnforcementInProgressException(pu);
        }

        
        return null;
    }

    /**
     * Makes sure that across machines the number of primary instances divided by the number of containers is balanced.  
     * @param containers
     * @param sla
     * @throws RebalancingSlaEnforcementInProgressException
     */
    private void rebalanceNumberOfPrimaryInstancesPerMachine(GridServiceContainer[] containers,
            RebalancingSlaPolicy sla) throws RebalancingSlaEnforcementInProgressException {

        while (true) {
            final FutureStatefulProcessingUnitInstance futureInstance = 
                rebalanceNumberOfPrimaryInstancesPerCpuCoreStep(containers, sla);

            if (futureInstance == null) {
                break;
            }

            state.addFutureStatefulDeployment(futureInstance);
        }
    }

    /**
     * Restarts one pu so that the number of primary instances divided by the number of containers is more balanced.  
     * @param containers
     * @param sla
     * @throws RebalancingSlaEnforcementInProgressException
     */
    private FutureStatefulProcessingUnitInstance rebalanceNumberOfPrimaryInstancesPerCpuCoreStep(
            GridServiceContainer[] containers, 
            RebalancingSlaPolicy sla)
            throws RebalancingSlaEnforcementInProgressException {

        // sort all machines (including those not in the allocated containers)
        // by (numberOfPrimaryInstancesPerMachine - minNumberOfPrimaryInstances)
        // meaning machines that need primaries the most are first.
        Machine[] machines = RebalancingUtils.getMachinesHostingContainers(containers);
        final List<Machine> sortedMachines = 
            RebalancingUtils.sortMachinesByNumberOfPrimaryInstancesPerCpuCore(
                pu, 
                machines,
                sla.getAllocatedCapacity());

        Fraction optimalCpuCoresPerPrimary = 
            RebalancingUtils.getAverageCpuCoresPerPrimary(pu,sla.getAllocatedCapacity());
        boolean conflict = false;
        // the source machine is the machine where the primary is restarted (high primaries per core)
        // the target machine is the machine where a new primary is elected (low primaries per core)
        // try to match a source container with a target container and then do a primary restart.
        for (int targetIndex = 0; targetIndex < sortedMachines.size(); targetIndex++) {

            Machine target = sortedMachines.get(targetIndex);

            for (int sourceIndex = sortedMachines.size() - 1; sourceIndex > targetIndex; sourceIndex--) {

                Machine source = sortedMachines.get(sourceIndex);

                if (!RebalancingUtils.isRestartRecommended(pu, source, target, optimalCpuCoresPerPrimary, sla.getAllocatedCapacity())) {
                    // source cannot give up any primary instances
                    // since the array is sorted there is no point in continuing the search
                    break;
                }
                
                if (isConflictingOperationInProgress(target, 1)) {
                    // number of primaries on machine might be skewed.
                    conflict = true;
                    logger.debug("Cannot restart a primary instance whos backup is on machine "
                            + RebalancingUtils.machineToString(target)
                            + " since a conflicting relocation is already in progress.");
                    continue;
                }

                if (isConflictingOperationInProgress(source, 1)) {
                    // number of primaries on machine might be skewed.
                    conflict = true;
                    logger.debug("Cannot restart a primary instance from machine "
                            + RebalancingUtils.machineToString(source)
                            + " since a conflicting relocation is already in progress.");
                    continue;
                }
                
                // we have a target and a source container. 
                // now all we need is a primary instance on the source container that has a backup on the target container
                for (ProcessingUnitInstance candidateInstance : source.getProcessingUnitInstances(pu.getName())) {

                    if (candidateInstance.getSpaceInstance() == null) {
                        logger.debug("Cannot relocate " + RebalancingUtils.puInstanceToString(candidateInstance)
                                + " since embedded space is not detected");
                        continue;
                    }

                    if (candidateInstance.getSpaceInstance().getMode() != SpaceMode.PRIMARY) {
                        logger.debug("Cannot restart instance "
                                + RebalancingUtils.puInstanceToString(candidateInstance)
                                + " since it is not primary.");
                        continue;
                    }

                    if (!RebalancingUtils.isProcessingUnitPartitionIntact(candidateInstance)) {
                        logger.debug("Cannot restart " + RebalancingUtils.puInstanceToString(candidateInstance)
                                + " since instances from the same partition are missing");
                        conflict = true;
                        continue;
                    }

                    if (isConflictingStatefulDeploymentInProgress(candidateInstance)) {
                        logger.debug("Cannot relocate " + RebalancingUtils.puInstanceToString(candidateInstance)
                                + " " + "since another instance from the same partition is being relocated");
                        conflict = true;
                        continue;
                    }

                    Machine[] sourceReplicationMachines = RebalancingUtils.getMachinesHostingContainers(RebalancingUtils.getReplicationSourceContainers(candidateInstance));
                    if (sourceReplicationMachines.length > 1) {
                        throw new IllegalArgumentException("pu " + pu.getName() + " must have exactly one backup instance per partition in order for the primary restart algorithm to work.");
                    }
                    
                    if (!sourceReplicationMachines[0].equals(target)) {
                        logger.debug("Cannot restart " + RebalancingUtils.puInstanceToString(candidateInstance)
                                + "since replication source is on "
                                + RebalancingUtils.machineToString(sourceReplicationMachines[0]) + " "
                                + "and not on the target machine " + RebalancingUtils.machineToString(target));
                        continue;
                    }
                
                    if (logger.isInfoEnabled()) {
                        String sourceToString = RebalancingUtils.machineToString(source);
                        String targetToString = RebalancingUtils.machineToString(target);
                        int numberOfPrimaryInstancesOnTarget = RebalancingUtils.getNumberOfPrimaryInstancesOnMachine(pu, target);
                        Fraction numberOfCpuCoresOnTarget = RebalancingUtils.getNumberOfCpuCores(target, sla.getAllocatedCapacity());
                        int numberOfPrimaryInstancesOnSource = RebalancingUtils.getNumberOfPrimaryInstancesOnMachine(pu, source);
                        Fraction numberOfCpuCoresOnSource = RebalancingUtils.getNumberOfCpuCores(source, sla.getAllocatedCapacity());
                        logger.info(
                            "Restarting " + RebalancingUtils.puInstanceToString(candidateInstance) + " "
                            + "instance on machine " + sourceToString + " so that machine "
                            + sourceToString + " would have less instances per cpu core, and "
                            + targetToString + " would have more primary instances per cpu core. "
                            + sourceToString +" has " + numberOfPrimaryInstancesOnSource + " primary instances "+
                            "running on " + numberOfCpuCoresOnSource + " cpu cores. "
                            + targetToString +" has " + numberOfPrimaryInstancesOnTarget + " primary instances "+
                            "running on " + numberOfCpuCoresOnTarget + " cpu cores.");
                    }
                    
                    return RebalancingUtils.restartProcessingUnitInstanceAsync(candidateInstance,
                            logger, DEPLOYMENT_TIMEOUT_FAILURE_SECONDS, TimeUnit.SECONDS);
                }
            }
        }

        if (state.getNumberOfFutureDeployments(pu) == 0 &&
            RebalancingUtils.isProcessingUnitIntact(pu) &&
            RebalancingUtils.isEvenlyDistributedAcrossContainers(pu, containers) &&
            !RebalancingUtils.isEvenlyDistributedAcrossMachines(pu, sla.getAllocatedCapacity())) {
                
                logger.debug("Optimal primary rebalancing hueristics failed balancing primaries in this deployment. "+
                             "Performing non-optimal restart heuristics. Starting with partition " + lastResortPartitionRestart);
                
                //
                // We cannot balance primaries per cpu core with one restart.
                // That means we need forward looking logic for more than one step, which we currently haven't implemented.
                // So we just restart a primary on a machine that has too many parimaries per cpu core.
                // In order to make the algorithm deterministic and avoid loops we restart primaries by
                // their natural order (by partition number)
                //
                // lastResortPartitionRestart is the next partition we should restart. 
                for (;lastResortPartitionRestart < pu.getNumberOfInstances()-1 ; lastResortPartitionRestart++) {
                    
                    ProcessingUnitInstance candidateInstance = pu.getPartition(lastResortPartitionRestart).getPrimary();
                    Machine source = candidateInstance.getMachine();
                    
                    Machine[] sourceReplicationMachines = RebalancingUtils.getMachinesHostingContainers(RebalancingUtils.getReplicationSourceContainers(candidateInstance));
                    if (sourceReplicationMachines.length > 1) {
                        throw new IllegalArgumentException("pu " + pu.getName() + " must have exactly one backup instance per partition in order for the primary restart algorithm to work.");
                    }
                    
                    if (sourceReplicationMachines[0].equals(source)) {
                        logger.debug("Cannot restart " + RebalancingUtils.puInstanceToString(candidateInstance)
                                + "since replication source is on same machine as primary, so restarting will have not change number of primaries on machine.");
                        continue;
                    }
                
                    
                    Fraction numberOfCpuCoresOnSource = 
                        RebalancingUtils.getNumberOfCpuCores(source, sla.getAllocatedCapacity());
                    
                    Fraction optimalCpuCores = 
                        new Fraction(RebalancingUtils.getNumberOfPrimaryInstancesOnMachine(pu, source))
                        .multiply(optimalCpuCoresPerPrimary);
                    
                    if (numberOfCpuCoresOnSource.compareTo(optimalCpuCores) <= 0) {
                        
                        // number of cores is below optimal, 
                        // which means there are too many primaries on the machine                        
                        if (logger.isInfoEnabled()) {
                            String sourceToString = RebalancingUtils.machineToString(source);
                            int numberOfPrimaryInstancesOnSource = RebalancingUtils.getNumberOfPrimaryInstancesOnMachine(pu, source);
                            logger.info(
                                "Restarting " + RebalancingUtils.puInstanceToString(candidateInstance) + " "
                                + "instance on machine " + sourceToString + " so that machine "
                                + sourceToString + " would have less instances per cpu core. "
                                + sourceToString +" has " + numberOfPrimaryInstancesOnSource + " primary instances "+
                                "running on " + numberOfCpuCoresOnSource + " cpu cores. ");
                        }
                        
                        return RebalancingUtils.restartProcessingUnitInstanceAsync(candidateInstance,
                                logger, DEPLOYMENT_TIMEOUT_FAILURE_SECONDS, TimeUnit.SECONDS);
                    }
                }
                // we haven't found any partition to restart, probably the instance that requires restart
                // has a partition lower than lastResortPartitionRestart.
                
                if (lastResortPartitionRestart >= pu.getNumberOfInstances()-1) {
                    lastResortPartitionRestart = 0; //better luck next time. continuation programming
                }
        }                    

        if (conflict) {
            throw new RebalancingSlaEnforcementInProgressException(pu);
        }

        return null;
    }

    private void cleanFutureStatefulDeployments() throws RebalancingSlaEnforcementInProgressException {

        while(true) {
            
            FutureStatefulProcessingUnitInstance future = state.removeOneDoneFutureStatefulDeployments(pu);
            
            if (future == null) {
                // no more done futures
                break;
            }
                
            Throwable throwable = null;

            try {
                ProcessingUnitInstance puInstance = future.get();
                logger.info("Processing unit instance deployment completed successfully "
                        + RebalancingUtils.puInstanceToString(puInstance));

            } catch (ExecutionException e) {
                throwable = e.getCause();
            } catch (TimeoutException e) {
                throwable = e;
            }

            if (throwable != null) {
                state.addFailedStatefulDeployment(future);
                throwFutureProcessingUnitInstanceException(throwable);
            }
        
        }

        cleanFailedFutureStatefulDeployments();
    }

    
    private void cleanFutureStatelessDeployments() throws RebalancingSlaEnforcementInProgressException {

        while(true) {
            
            FutureStatelessProcessingUnitInstance future = state.removeOneDoneFutureStatelessDeployments(pu);
            
            if (future == null) {
                // no more done futures
                break;
            }
                           
            Throwable throwable = null;

            try {
                ProcessingUnitInstance puInstance = future.get();
                logger.info("Processing unit instance deployment completed successfully "
                        + RebalancingUtils.puInstanceToString(puInstance));

            } catch (ExecutionException e) {
                throwable = e.getCause();
            } catch (TimeoutException e) {
                throwable = e;
            }

            if (throwable != null) {
                state.addFailedStatelessDeployment(future);
                throwFutureProcessingUnitInstanceException(throwable);
            }
        }
         
        cleanFailedFutureStatelessDeployments();
    }
    /**
     * This method removes failed relocations from the list allowing a retry attempt to take place.
     * Some failures are removed immediately, while others stay in the list for
     * RELOCATION_TIMEOUT_FAILURE_IGNORE_SECONDS.
     */
    private void cleanFailedFutureStatefulDeployments() {

        for (FutureStatefulProcessingUnitInstance future : state.getFailedStatefulDeployments(pu)) {
            int passedSeconds = (int) ((System.currentTimeMillis() - future.getTimestamp().getTime()) / 1000);

            if (future.getException() != null
                    && future.getException().getCause() instanceof WrongContainerProcessingUnitRelocationException
                    && future.getTargetContainer().isDiscovered()
                    && passedSeconds < DEPLOYMENT_TIMEOUT_FAILURE_FORGET_SECONDS) {

                // do not remove future from list since the target container did not have enough
                // memory
                // meaning something is very wrong with our assumptions on the target container.
                // We leave this future in the list so it will cause conflicting exceptions.
                // Once RELOCATION_TIMEOUT_FAILURE_FORGET_SECONDS passes it is removed from the
                // list.
            } else {
                logger.info("Forgetting relocation error " + future.getFailureMessage());
                state.removeFailedFutureStatefulDeployment(future);
            }
        }
    }
    
    private void cleanRemovedStatelessProcessingUnitInstances() {
        for (ProcessingUnitInstance instance : state.getRemovedStatelessProcessingUnitInstances(pu)) {
             if (!instance.isDiscovered()) {
                state.removeRemovedStatelessProcessingUnitInstance(instance);
                logger.info("Processing Unit Instance " + RebalancingUtils.puInstanceToString(instance) + " removed succesfully.");
            }
        }
    }

    private void throwFutureProcessingUnitInstanceException(Throwable throwable) throws RebalancingSlaEnforcementInProgressException {
        
        if (throwable instanceof RebalancingSlaEnforcementInProgressException){
            throw (RebalancingSlaEnforcementInProgressException)throwable;
        }
        else if (throwable instanceof AdminException) {
            throw new FutureProcessingUnitInstanceDeploymentException(pu,(AdminException)throwable);
        }
        else if (throwable instanceof TimeoutException){
            throw new FutureProcessingUnitInstanceDeploymentException(pu,(TimeoutException)throwable);
        }
        else {
            throw new IllegalStateException("Unexpected exception type", throwable);
        }
    }
    
    /**
     * This method removes failed stateless deployments from the list allowing a retry attempt to take place.
     * Failed deployment stay in the list for RELOCATION_TIMEOUT_FAILURE_IGNORE_SECONDS.
     * Unless the target container has been removed.
     */
    private void cleanFailedFutureStatelessDeployments() {
        for (FutureStatelessProcessingUnitInstance future : state.getFailedStatelessDeployments(pu)) {

            int passedSeconds = (int) ((System.currentTimeMillis() - future.getTimestamp().getTime()) / 1000);

            if (future.getException() != null
                && future.getTargetContainer().isDiscovered()
                && passedSeconds < DEPLOYMENT_TIMEOUT_FAILURE_FORGET_SECONDS) {

                // do not remove future from list until timeout failure forget
                // since something is very wrong with target container.
                
                if (logger.isDebugEnabled()) {
                    logger.debug("Ignoring failure to relocate stateless pu instance " + future.getProcessingUnit() + " Will try again in " + (DEPLOYMENT_TIMEOUT_FAILURE_FORGET_SECONDS- passedSeconds) + " seconds.",future.getException());
                }
                
            } else {
                logger.info("Forgetting relocation error " + future.getFailureMessage());
                state.removeFailedFutureStatelessDeployment(future);
            }
        }
    }
    
    private boolean isConflictingDeploymentInProgress(
            GridServiceContainer container,
            int maximumNumberOfConcurrentRelocationsPerMachine) {

        if (maximumNumberOfConcurrentRelocationsPerMachine <= 0) {
            throw new IllegalStateException("maximumNumberOfConcurrentRelocationsPerMachine must be 1 or higher");
        }

        int concurrentRelocationsInContainer = 0;

        for (FutureStatefulProcessingUnitInstance future : state.getAllFutureStatefulProcessingUnitInstances()) {

            GridServiceContainer targetContainer = future.getTargetContainer();
            GridServiceContainer sourceContainer = future.getSourceContainer();
            List<GridServiceContainer> replicationSourceContainers = Arrays.asList(future.getReplicaitonSourceContainers());

            if (sourceContainer.equals(container) || // wrong reading of #instances on source
                targetContainer.equals(container) || // wrong reading of #instances on target
                replicationSourceContainers.contains(container)) { // replication source is busy
                                                                   // now with sending data to
                                                                   // the new backup

                concurrentRelocationsInContainer++;
            }
        }
        
        for (FutureStatelessProcessingUnitInstance future : state.getAllFutureStatelessProcessingUnitInstances()) {

            GridServiceContainer targetContainer = future.getTargetContainer();

            if (targetContainer.equals(container)) { // deployment already in progress
                  concurrentRelocationsInContainer++;
            }
        }


        return concurrentRelocationsInContainer > 0 ||

        isConflictingOperationInProgress(container.getMachine(), maximumNumberOfConcurrentRelocationsPerMachine);
    }
    
    private boolean isConflictingOperationInProgress(Machine machine, int maximumNumberOfConcurrentRelocationsPerMachine) {

        if (maximumNumberOfConcurrentRelocationsPerMachine <= 0) {
            // maximumNumberOfConcurrentRelocationsPerMachine is disabled
            maximumNumberOfConcurrentRelocationsPerMachine = Integer.MAX_VALUE;
        }

        int concurrentRelocationsInMachine = 0;

        for (FutureStatefulProcessingUnitInstance future : state.getAllFutureStatefulProcessingUnitInstances()) {

            GridServiceContainer targetContainer = future.getTargetContainer();
            List<GridServiceContainer> replicationSourceContainers = Arrays.asList(future.getReplicaitonSourceContainers());

            Machine targetMachine = targetContainer.getMachine();
            Set<Machine> replicaitonSourceMachines = new HashSet<Machine>();
            for (GridServiceContainer replicationSourceContainer : replicationSourceContainers) {
                replicaitonSourceMachines.add(replicationSourceContainer.getMachine());
            }

            if (targetMachine.equals(machine) || // target machine is busy with replication
                    replicaitonSourceMachines.contains(machine)) { // replication source machine is
                                                                   // busy with replication

                concurrentRelocationsInMachine++;
            }
        }
        
        for (FutureStatelessProcessingUnitInstance future : state.getAllFutureStatelessProcessingUnitInstances()) {

            GridServiceContainer targetContainer = future.getTargetContainer();
            Machine targetMachine = targetContainer.getMachine();
            
            if (targetMachine.equals(machine)) { // target machine is busy with deployment

                concurrentRelocationsInMachine++;
            }
        }


        return concurrentRelocationsInMachine >= maximumNumberOfConcurrentRelocationsPerMachine;
    }

    private boolean isConflictingStatefulDeploymentInProgress(ProcessingUnitInstance candidateInstance) {

        for (FutureStatefulProcessingUnitInstance future : state.getAllFutureStatefulProcessingUnitInstances()) {
            if (future.getProcessingUnit().equals(candidateInstance.getProcessingUnit())
                    && future.getInstanceId() == candidateInstance.getInstanceId()) {
                return true;
            }
        }

        return false;
    }
}
