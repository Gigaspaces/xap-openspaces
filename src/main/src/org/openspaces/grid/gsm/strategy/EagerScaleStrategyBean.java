package org.openspaces.grid.gsm.strategy;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.grid.gsm.GridServiceContainerConfigAware;
import org.openspaces.grid.gsm.capacity.ClusterCapacityRequirements;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.containers.ContainersSlaPolicy;
import org.openspaces.grid.gsm.containers.exceptions.ContainersSlaEnforcementPendingProcessingUnitDeallocationException;
import org.openspaces.grid.gsm.machines.EagerMachinesSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.EagerMachinesSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.machines.EagerMachinesSlaPolicy;
import org.openspaces.grid.gsm.machines.exceptions.MachinesSlaEnforcementPendingContainerDeallocationException;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaPolicy;
import org.openspaces.grid.gsm.rebalancing.RebalancingUtils;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementEndpointDestroyedException;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementException;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementInProgressException;
import org.openspaces.grid.gsm.strategy.ProvisionedMachinesCache.AgentsNotYetDiscoveredException;

public class EagerScaleStrategyBean extends AbstractScaleStrategyBean 

    implements RebalancingSlaEnforcementEndpointAware , 
               ContainersSlaEnforcementEndpointAware, 
               EagerMachinesSlaEnforcementEndpointAware,
               GridServiceContainerConfigAware {
    
    // injected 
    private EagerScaleConfig slaConfig;
    private EagerMachinesSlaEnforcementEndpoint machinesEndpoint;
    private ContainersSlaEnforcementEndpoint containersEndpoint;
    private RebalancingSlaEnforcementEndpoint rebalancingEndpoint;
    private GridServiceContainerConfig containersConfig;

    public void setEagerMachinesSlaEnforcementEndpoint(EagerMachinesSlaEnforcementEndpoint endpoint) {
        this.machinesEndpoint = endpoint;
    }
    
    public void setContainersSlaEnforcementEndpoint(ContainersSlaEnforcementEndpoint containersService) {
        this.containersEndpoint = containersService;
    }
    
    public void setRebalancingSlaEnforcementEndpoint(RebalancingSlaEnforcementEndpoint relocationService) {
        this.rebalancingEndpoint = relocationService;
    }

    public void setGridServiceContainerConfig(GridServiceContainerConfig containersConfig) {
         this.containersConfig = containersConfig;
    }
    

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        
        if (machinesEndpoint == null) {
            throw new IllegalStateException("machines endpoint cannot be null.");
        }
        
        if (containersEndpoint == null) {
            throw new IllegalStateException("containers endpoint cannot be null");
        }
        
        if (rebalancingEndpoint == null) {
            throw new IllegalStateException("rebalancing endpoint cannot be null.");
        }
        
        slaConfig = new EagerScaleConfig(super.getProperties());
    }

    @Override
    public void enforceSla() throws SlaEnforcementException {
        
        SlaEnforcementInProgressException pendingException = null;
        
        try {
            enforceMachinesSla();
        }
        catch (MachinesSlaEnforcementPendingContainerDeallocationException e) {
            // fall through to containers sla enforcement since need to deallocate containers
            pendingException = e;
        }
        
        try {
            enforceContainersSla();
        }
        catch (ContainersSlaEnforcementPendingProcessingUnitDeallocationException e) {
            // fall through to rebalacing sla enforcement since need to deallocate pu instances
            pendingException = e;    
        }
        
        enforceRebalancingSla(containersEndpoint.getContainers());
        
        if (pendingException != null) {
            throw pendingException;
        }
    }

    private void enforceMachinesSla() throws AgentsNotYetDiscoveredException, SlaEnforcementException {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Enforcing machines SLA.");
        }
        final EagerMachinesSlaPolicy sla = getEagerMachinesSlaPolicy();
        
        try {
            machinesEndpoint.enforceSla(sla);
            //TODO: Add alert specific properties
            
            resolveMachinesAlert(
                    "Machines eager SLA for " + getProcessingUnit().getName() + " " + 
                    "has been reached: " + machinesEndpoint.getAllocatedCapacity().toDetailedString());
        }
        catch (SlaEnforcementException e) {
            raiseMachinesAlert(e);
            throw e;
        }

    }

    private void enforceContainersSla() throws SlaEnforcementException {
        
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Enforcing containers SLA.");
        }
        
        ClusterCapacityRequirements allocatedCapacity = machinesEndpoint.getAllocatedCapacity();
        
        final ContainersSlaPolicy sla = new ContainersSlaPolicy();
        sla.setNewContainerConfig(containersConfig);
        sla.setClusterCapacityRequirements(allocatedCapacity);

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Containers Eager SLA Policy: "+
                    "#gridServiceAgents=" + sla.getClusterCapacityRequirements().getAgentUids().size() + " "+
                    "newContainerConfig.maximumMemoryCapacityInMB="+sla.getNewContainerConfig().getMaximumMemoryCapacityInMB());
        }
        
        try {
            containersEndpoint.enforceSla(sla);
            //TODO: Add alert specific properties
            resolveContainersAlert(
                    "Eager containers SLA for " + getProcessingUnit().getName() + " " + 
                    "has been reached: " + machinesEndpoint.getAllocatedCapacity().toDetailedString());           
        } catch (SlaEnforcementException e) {
            //TODO: Add alert specific properties
            //TODO: Add inner inner exception message
            raiseContainersAlert(e);
            throw e;
        }
        
        
    }
    
    private void enforceRebalancingSla(GridServiceContainer[] containers) 
        throws SlaEnforcementEndpointDestroyedException 
    {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Enforcing rebalancing SLA.");
        }
        RebalancingSlaPolicy sla = new RebalancingSlaPolicy();
        sla.setContainers(containers);
        sla.setMaximumNumberOfConcurrentRelocationsPerMachine(slaConfig.getMaxConcurrentRelocationsPerMachine());
        sla.setSchemaConfig(getSchemaConfig());
        sla.setAllocatedCapacity(machinesEndpoint.getAllocatedCapacity());
        try {
            rebalancingEndpoint.enforceSla(sla);
            resolveRebalancingAlert(
                    "Rebalancing of " + getProcessingUnit().getName() + " is complete: " + 
                    RebalancingUtils.processingUnitDeploymentToString(getProcessingUnit()));
            
        } catch (SlaEnforcementException e) {
            raiseRebalancingAlert(e);
        }
    }
    
    private EagerMachinesSlaPolicy getEagerMachinesSlaPolicy() throws AgentsNotYetDiscoveredException {
        final EagerMachinesSlaPolicy sla = new EagerMachinesSlaPolicy();      
        sla.setMaximumNumberOfMachines(getMaximumNumberOfInstances());
        sla.setMinimumNumberOfMachines(getMinimumNumberOfMachines());
        sla.setMaximumNumberOfContainersPerMachine(getMaximumNumberOfContainersPerMachine());
        sla.setContainerMemoryCapacityInMB(containersConfig.getMaximumMemoryCapacityInMB());
        sla.setProvisionedAgents(getDiscoveredAgents());
        sla.setMachineIsolation(getIsolation());
        sla.setMachineProvisioning(super.getMachineProvisioning());
        return sla;
    }

    public EagerScaleConfig getConfig() {
        return slaConfig;
    }
    
    private int getMaximumNumberOfContainersPerMachine() {
        return slaConfig.isAtMostOneContainersPerMachine()?1:getMaximumNumberOfInstances();
    }
}
