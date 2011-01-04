package org.openspaces.grid.gsm.machines;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CpuCapacityRequirement;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirment;
import org.openspaces.grid.gsm.capacity.NumberOfMachinesCapacityRequirement;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcement;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointAlreadyExistsException;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;

public class MachinesSlaEnforcement implements
        ServiceLevelAgreementEnforcement<MachinesSlaPolicy, ProcessingUnit, MachinesSlaEnforcementEndpoint> {

    private static final Log logger = LogFactory.getLog(MachinesSlaEnforcementEndpoint.class);
    private static final int START_AGENT_TIMEOUT_SECONDS = 10*60;
    private static final long STOP_AGENT_TIMEOUT_SECONDS = 10*60;

    private final Map<ProcessingUnit, MachinesSlaEnforcementEndpoint> endpoints;

    private final Admin admin;

    public MachinesSlaEnforcement(Admin admin) {

        endpoints = new HashMap<ProcessingUnit, MachinesSlaEnforcementEndpoint>();
        this.admin = admin;
    }

    public MachinesSlaEnforcementEndpoint createEndpoint(ProcessingUnit pu)
            throws ServiceLevelAgreementEnforcementEndpointAlreadyExistsException {
        if (endpoints.containsKey(pu)) {
            throw new ServiceLevelAgreementEnforcementEndpointAlreadyExistsException();
        }

        if (pu.getRequiredZones().length != 1) {
            throw new IllegalStateException("PU has to have exactly 1 zone defined");
        }

        String zone = pu.getRequiredZones()[0];
        Set<GridServiceAgent> agents = new HashSet<GridServiceAgent>();
        
        for (GridServiceContainer container : admin.getGridServiceContainers()) {
            
            // found a container associated with this pu. take it.
            if (container.getZones().size() == 1 &&
                container.getZones().containsKey(zone) &&
                container.getMachine().getGridServiceAgents().getSize() == 1) {

                agents.add(container.getMachine().getGridServiceAgent());
           }
        }

        for (MachinesSlaEnforcementEndpoint endpoint : endpoints.values()) {
            
            for (GridServiceAgent gsa : endpoint.getGridServiceAgents()) {
                // some other pu's agent - remove
                agents.remove(gsa);
            }
            
            for (GridServiceAgent gsa : endpoint.getGridServiceAgentsPendingShutdown()) {
                // some other pu's agent - remove
                agents.remove(gsa);
            }
        }

        // we need to decide what to do with these agents...
        // it really depends if its a VM based agent in which case it 
        // could be passed in the constructor and then the endpoint would either
        // consume it or destroy it. Also we need to get the list of machines from
        // the machines provisioning not admin, since admin may return machines that
        // are non-VMed.
        //
        // if its not a VM, then we need a shared pool of those for all endpoints
        // and the endpoints need to consume those only on demand
        // the pool could implement a degenerated form of machines provisioning
        // that does not implement start/stop, but rather just implements getallmachines
        //
        MachinesSlaEnforcementEndpoint endpoint = 
            new DefaultMachinesSlaEnforcementEndpoint(pu, agents);
        endpoints.put(pu, endpoint);
        return endpoint;
    }

    public void destroyEndpoint(ProcessingUnit pu) {

        DefaultMachinesSlaEnforcementEndpoint endpoint = (DefaultMachinesSlaEnforcementEndpoint) endpoints.remove(pu);
        if (endpoint != null) {
            endpoint.destroy();
        }
    }

    public void destroy() throws Exception {
        for (ProcessingUnit pu : endpoints.keySet()) {
            destroyEndpoint(pu);
        }
    }
    
    /**
     * Logic that decides werther to start a new machine machine or stop an existing machine
     * based on the specified SLA policy.
     * 
     * @author itaif
     * @see MachinesSlaEnforcement - creates this endpoint
     * @see MachinesSlaPolicy - defines the sla policy for this endpoint
     */
    class DefaultMachinesSlaEnforcementEndpoint implements MachinesSlaEnforcementEndpoint {

        private final ProcessingUnit pu;

        // state that tracks managed grid service agents, agents to be started and agents marked for shutdown.
        private final List<GridServiceAgent> agentsStarted;
        private final List<FutureGridServiceAgents> futureAgents;
        private final List<GridServiceAgent> agentsPendingShutdown;
        
        // flag indicating that destroyed() was called
        private boolean destroyed;
        
        public DefaultMachinesSlaEnforcementEndpoint(ProcessingUnit pu, Collection<GridServiceAgent> agents) {
            
            if (pu == null) {
                throw new IllegalArgumentException("pu cannot be null.");
            }
            
            this.pu = pu;
            this.destroyed = false;
            
            this.agentsStarted = new ArrayList<GridServiceAgent>();
            agentsStarted.addAll(agents);
            this.futureAgents = new ArrayList<FutureGridServiceAgents>();
            this.agentsPendingShutdown = new ArrayList<GridServiceAgent>();
            
        }
        
        public GridServiceAgent[] getGridServiceAgents() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
            validateNotDestroyed();
           
            return agentsStarted.toArray(new GridServiceAgent[agentsStarted.size()]);
        }

            
        public GridServiceAgent[] getGridServiceAgentsPendingShutdown() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
            validateNotDestroyed();
            
            return agentsPendingShutdown.toArray(new GridServiceAgent[agentsPendingShutdown.size()]);
            
        }

        public boolean enforceSla(MachinesSlaPolicy sla) throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
            validateNotDestroyed();
            
            if (sla == null) {
                throw new IllegalArgumentException("SLA cannot be null");
            }
            
            if (sla.getCpu() < 0 ) {
                throw new IllegalArgumentException("CPU cannot be negative");
            }
            
            if (sla.getMemoryCapacityInMB() < 0) {
                throw new IllegalArgumentException("Memory capacity cannot be negative");
            }
            
            if (sla.getMachineProvisioning() == null) {
                throw new IllegalArgumentException("MachineProvisioning cannot be null.");
            }
            
            try {
                return enforceSlaInternal(sla);
            } catch (ConflictingOperationInProgressException e) {
                logger.info("Cannot enforce Machines SLA since a conflicting operation is in progress. Try again later.", e);
                return false; // try again next time
            }
        }
        
        public ProcessingUnit getId() {
            return pu;
        }

        
        public void destroy() {
            
            destroyed = true;
        }
        
        private boolean enforceSlaInternal(MachinesSlaPolicy sla)
                throws ConflictingOperationInProgressException {

            cleanAgentsMarkedForShutdown(sla.getMachineProvisioning());
            cleanFutureAgents();

            if (!futureAgents.isEmpty() && !agentsPendingShutdown.isEmpty()) {
                throw new IllegalStateException("Cannot have both agents pending to be started and agents pending shutdown.");
            }
            
            boolean slaReached = futureAgents.isEmpty() && agentsPendingShutdown.isEmpty();
            
            long targetMemory = sla.getMemoryCapacityInMB();
            double targetCpu = sla.getCpu();

            int existingMemory = 0;
            double existingCpu = 0;
            
            for (GridServiceAgent agent : agentsStarted) {
                existingMemory += getMemoryInMB(agent);
                existingCpu += getCpu(agent);
            }
            
            for (GridServiceAgent agent : agentsPendingShutdown) {
                existingMemory += getMemoryInMB(agent);
                existingCpu += getCpu(agent);
            }

            if (existingMemory > targetMemory && 
                existingCpu > targetCpu && 
                agentsStarted.size() + agentsPendingShutdown.size() > sla.getMinimumNumberOfMachines()) {
                
                logger.debug("Considering scale in: "+
                        "target memory is "+ targetMemory +"MB, "+
                        "existing memory is " + existingMemory +"MB, "+
                        "target CPU is " + targetCpu + " " +
                        "existing CPU is " + existingCpu + ", " +
                        "minimum #machines is " + sla.getMinimumNumberOfMachines() + ", " +
                        "existing #machines is " + agentsStarted.size() + agentsPendingShutdown.size());
                
                // scale in
                long surplusMemory = existingMemory - targetMemory;
                double surplusCpu = existingCpu - targetCpu;
                
                int surplusMachines = agentsStarted.size() + agentsPendingShutdown.size() - sla.getMinimumNumberOfMachines();
                
                // adjust surplusMemory based on agents marked for shutdown
                // remove mark if it would cause surplus to be below zero
                // remove mark if it would reduce the number of machines below the sla minimum.
                Iterator<GridServiceAgent> iterator = Arrays.asList(
                        getGridServiceAgentsPendingShutdown()).iterator();
                while (iterator.hasNext()) {

                    GridServiceAgent agent = iterator.next();
                    int machineMemory = getMemoryInMB(agent);
                    double machineCpu = getCpu(agent);
                    if (surplusMemory >= machineMemory &&
                        surplusCpu >= machineCpu &&
                        surplusMachines > 0) {
                        // this machine is already marked for shutdown, so surplus
                        // is adjusted to reflect that
                        surplusMemory -= machineMemory;
                        surplusCpu -= machineCpu;
                        surplusMachines--;
                    } else {
                        iterator.remove();
                        logger.info("machine agent " + agent.getMachine().getHostAddress() + " is no longer marked for shutdown in order to maintain capacity.");
                    }
                }

                // mark agents for shutdown if there are not enough of them (scale in)
                // give priority to agents that do not host a GSM/LUS since we want to evacuate those last.
                for (GridServiceAgent agent : MachinesSlaUtils.sortGridServiceAgentsByManagementComponentsLast(getGridServiceAgents())) {
                    int machineMemory = getMemoryInMB(agent);
                    double machineCpu = getCpu(agent);
                    if (surplusMemory >= machineMemory && surplusCpu >= machineCpu && surplusMachines > 0) {

                        // mark machine for shutdown unless it is a management
                        // machine
                        this.agentsPendingShutdown.add(agent);
                        this.agentsStarted.remove(agent);
                        surplusMemory -= machineMemory;
                        surplusCpu -= machineCpu;
                        surplusMachines --;
                        slaReached = false;
                        logger.info("machine agent " + agent.getMachine().getHostAddress() + " is marked for shutdown in order to reduce capacity.");
                    }
                }
            }

            else if (futureAgents.isEmpty() && 
                    (agentsStarted.size() - agentsPendingShutdown.size() < sla.getMinimumNumberOfMachines())) {
                
                logger.info("Considering to start more machines to reach required minimum number of machines: " + 
                            agentsStarted.size() + " machine agents started, " +
                            agentsPendingShutdown.size() + " machine agents marked for shutdown, " +
                            sla.getMinimumNumberOfMachines() + " is the required minimum number of machines."
                );
                int machineShortage = sla.getMinimumNumberOfMachines() + agentsPendingShutdown.size() 
                                      - agentsStarted.size();
                
                for (int i =0 ; i < machineShortage && agentsPendingShutdown.size() > 0; i++) {
                    GridServiceAgent agent = agentsPendingShutdown.remove(0);
                    logger.info("machine agent " + agent.getMachine().getHostAddress() + " is no longer marked for shutdown in order to reach the minimum of " + sla.getMinimumNumberOfMachines() + " machines.");
                }
                
                machineShortage = sla.getMinimumNumberOfMachines() + agentsPendingShutdown.size() 
                                    - agentsStarted.size();
                if (machineShortage > 0) {
                    this.futureAgents.add(
                            sla.getMachineProvisioning().startMachinesAsync(
                                new CapacityRequirements(
                                        new NumberOfMachinesCapacityRequirement(machineShortage)),
                                START_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS));
                    slaReached = false;
                    logger.info(machineShortage+ " new machine(s) is scheduled to be started in order to reach the minimum of " + sla.getMinimumNumberOfMachines() + " machines.");
                }
            }
            
            else if (agentsPendingShutdown.isEmpty() && 
                    (agentsStarted.size()  < sla.getMinimumNumberOfMachines())) {
                
                int machineShortage = sla.getMinimumNumberOfMachines() - agentsStarted.size();
                
             // take into account expected machines into shortage calculate
                for (FutureGridServiceAgents future : this.futureAgents) {
                    
                    int expectedNumberOfMachines = future.getCapacityRequirements().getRequirement(NumberOfMachinesCapacityRequirement.class).getNumberOfMahines();
                    if (expectedNumberOfMachines == 0) {
                        throw new ConflictingOperationInProgressException();
                    }
                    machineShortage -= expectedNumberOfMachines;
                }

                if (machineShortage > 0) {
                
                    // scale out to get to the minimum number of agents
                     
                    this.futureAgents.add(
                            sla.getMachineProvisioning().startMachinesAsync(
                                new CapacityRequirements(
                                        new NumberOfMachinesCapacityRequirement(machineShortage)),
                                START_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS));
                    slaReached = false;
                    logger.info(machineShortage + " " + 
                            "new machine(s) is scheduled to be started in order to "+
                            "reach the minimum of " + sla.getMinimumNumberOfMachines() + " " +
                            "machines.");
                }
            }
            
            else if (existingMemory < targetMemory || existingCpu < targetCpu) {
                // scale out

                logger.info("Considering to start more machines inorder to reach target capacity:" + 
                        "target memory is "+ targetMemory +"MB, "+
                        "existing memory is " + existingMemory +"MB, "+
                        "target CPU is " + targetCpu + " " +
                        "existing CPU is " + existingCpu);
                
                // unmark all machines pending shutdown
                for (GridServiceAgent agent : agentsPendingShutdown) {
                    logger.info("machine agent " + agent.getMachine().getHostAddress() + " is no longer marked for shutdown in order to maintain capacity.");
                }
                this.agentsPendingShutdown.clear();
                    

                long shortageMemory = targetMemory - existingMemory;
                double shortageCpu = targetCpu - existingCpu;

                // take into account expected machines into shortage calculate
                for (FutureGridServiceAgents future : this.futureAgents) {
                    
                    long expectedMachineMemory = future.getCapacityRequirements().getRequirement(MemoryCapacityRequirment.class).getMemoryInMB();
                    if (shortageMemory > 0 && expectedMachineMemory == 0) {
                        throw new ConflictingOperationInProgressException();
                    }
                    shortageMemory -= expectedMachineMemory;

                    double expectedMachineCpu = future.getCapacityRequirements().getRequirement(CpuCapacityRequirement.class).getCpu();
                    if (shortageCpu > 0 && expectedMachineCpu == 0) {
                        throw new ConflictingOperationInProgressException();
                    }
                    shortageCpu -= expectedMachineCpu;
                }

                if (shortageMemory < 0) {
                    shortageMemory = 0;
                }
                if (shortageCpu < 0) {
                    shortageCpu = 0;
                }

                if (shortageCpu >0 || shortageMemory > 0) {
                    slaReached = false;
                
                    
                    this.futureAgents.add(
                        sla.getMachineProvisioning().startMachinesAsync(
                            new CapacityRequirements(
                                    new MemoryCapacityRequirment(shortageMemory),
                                    new CpuCapacityRequirement(shortageCpu)),
                            START_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS));
                    slaReached = false;
                    logger.info("One or more new machine were scheduled to be started in order to increase capacity.");
                }
            }
            else {
                logger.debug("No action required in order to enforce machines sla.");
            }
            return slaReached;
        }

        private void cleanAgentsMarkedForShutdown(NonBlockingElasticMachineProvisioning machineProvisioning) {
            
            final Iterator<GridServiceAgent> iterator = agentsPendingShutdown.iterator();
            while (iterator.hasNext()) {
                
                final GridServiceAgent agent = iterator.next();
                
                int numberOfChildProcesses = MachinesSlaUtils.getNumberOfChildProcesses(agent);
                
                if (!agent.isRunning()) {
                    logger.info("Agent machine " + agent.getMachine().getHostAddress() + " is confirmed to be shutdown.");
                    iterator.remove();
                } 
                else if (numberOfChildProcesses == 0) {
                     // nothing running on this agent (not even GSM/LUS). Get rid of it.
                    logger.info("Stopping agent machine " + agent.getMachine().getHostAddress());   
                    machineProvisioning.stopMachineAsync(agent, STOP_AGENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                }
            }
            
        }

        private void cleanFutureAgents() {
            final Iterator<FutureGridServiceAgents> iterator = futureAgents.iterator();
            while (iterator.hasNext()) {
                FutureGridServiceAgents future = iterator.next();
                
                if (future.isDone()) {
                
                    iterator.remove();
                    
                    Throwable exception = null;
                    
                    try {
                    
                        GridServiceAgent[] agents = future.get();
                        agentsStarted.addAll(Arrays.asList(agents));
                        if (logger.isInfoEnabled()) {
                            for (GridServiceAgent agent : agents) {
                                logger.info("Agent started succesfully on a new machine " + agent.getMachine().getHostAddress());
                            }
                        }
                    } catch (ExecutionException e) {
                        exception = e.getCause();
                    } catch (TimeoutException e) {
                        exception = e;
                    }
                    
                    if (exception != null) {
                        final String errorMessage = "Failed to start agent on new machine";
                        logger.warn(errorMessage , exception);
                    }
                }
            }
            
        }
        
        private void validateNotDestroyed() {
            if (destroyed) {
                throw new ServiceLevelAgreementEnforcementEndpointDestroyedException();
            }
        }
        
        private int getMemoryInMB(GridServiceAgent agent) {
            return (int) 
                agent.getMachine().getOperatingSystem()
                .getDetails()
                .getTotalPhysicalMemorySizeInMB();
        }

        private double getCpu(GridServiceAgent agent) {
            return agent.getMachine().getOperatingSystem().getDetails().getAvailableProcessors();
        }
    }
    

    @SuppressWarnings("serial")
    private static class ConflictingOperationInProgressException extends Exception  {}

}
