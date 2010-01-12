package org.openspaces.grid.esm;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.esm.deployment.DeploymentContext;
import org.openspaces.admin.esm.deployment.ElasticDataGridDeployment;
import org.openspaces.admin.esm.deployment.IsolationLevel;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceContainerOptions;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.space.SpaceDeployment;
import org.openspaces.admin.zone.Zone;

import com.j_spaces.kernel.TimeUnitProperty;

public class InternalESMImpl {
    private final static Logger logger = Logger.getLogger("org.openspaces.grid.esm");
    private final static long initialDelay = TimeUnitProperty.getProperty("org.openspaces.grid.esm.initialDelay", "1m");
    private final static long fixedDelay = TimeUnitProperty.getProperty("org.openspaces.grid.esm.fixedDelay", "5s");
    
    private final Admin admin = new AdminFactory().createAdmin();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    
    public InternalESMImpl() {
        logger.setLevel(Level.parse(System.getProperty("logger.level", "INFO")));
        logger.config("Initial Delay: " + initialDelay + " ms");
        logger.config("Fixed Delay: " + fixedDelay + " ms");
        if (Boolean.getBoolean("server"))
            executorService.scheduleWithFixedDelay(new ScheduledTask(), initialDelay, fixedDelay, TimeUnit.MILLISECONDS);
    }

    //-Dorg.jini.rio.qos.address=moran2
 // -Dcom.gs.zones=barfoo -Dcom.gigaspaces.grid.gsc.serviceLimit=10
    public void deploy(ElasticDataGridDeployment deployment) {
        DeploymentContext context = deployment.getContext();
        final String zoneName = deployment.getDataGridName();
        SpaceDeployment spaceDeployment = new SpaceDeployment(deployment.getDataGridName());
        spaceDeployment.addZone(zoneName);
        int numberOfParitions = calculateNumberOfPartitions(context);
        if (context.isHighlyAvailable()) {
            spaceDeployment.maxInstancesPerMachine(1);
            spaceDeployment.partitioned(numberOfParitions, 1);
        } else {
            spaceDeployment.partitioned(numberOfParitions, 0);
        }
        
        spaceDeployment.setContextProperty("minMemory", context.getMinMemory());
        spaceDeployment.setContextProperty("maxMemory", context.getMaxMemory());
        spaceDeployment.setContextProperty("jvmSize", context.getJvmSize());
        spaceDeployment.setContextProperty("isolationLevel", context.getIsolationLevel().name());
        spaceDeployment.setContextProperty("scalingFactor", String.valueOf(calculateScalingFactor(context)));
        admin.getGridServiceManagers().waitForAtLeastOne().deploy(spaceDeployment);
    }
    
    private int calculateNumberOfPartitions(DeploymentContext context) {
        int numberOfPartitions = MemorySettings.valueOf(context.getMaxMemory()).floorDividedBy(context.getJvmSize());
        if (context.isHighlyAvailable()) {
            numberOfPartitions /= 2;
        }
        
        return numberOfPartitions;
    }
    
    /** scaling factor is used as the service limit of each GSC */
    private int calculateScalingFactor(DeploymentContext context) {
        int maxGSCs = MemorySettings.valueOf(context.getMaxMemory()).floorDividedBy(context.getJvmSize());
        int minGSCs = MemorySettings.valueOf(context.getMinMemory()).floorDividedBy(context.getJvmSize());
        double scalingFactor = Math.ceil(1.0*maxGSCs/minGSCs);
        return (int)scalingFactor;
    }
    
    private final class ScheduledTask implements Runnable {

        public void run() {
            logger.finest("running");
            ProcessingUnits processingUnits = admin.getProcessingUnits();
            for (ProcessingUnit pu : processingUnits) {
                logger.fine(puToString(pu));
                
                String isolationLevelPropVal = pu.getBeanLevelProperties().getContextProperties().getProperty("isolationLevel");
                
                DeploymentStatus status = pu.getStatus();
                if (IsolationLevel.valueOf(isolationLevelPropVal).equals(IsolationLevel.DEDICATED)) {
                    switch(status) {
                    case BROKEN:
                    case COMPROMISED:
                        handleBrokenDedicatedIsolation(pu);
                        break;
                    }
                }
            }
        }
        
        private String puToString(ProcessingUnit pu) {
            StringBuilder sb = new StringBuilder();
            sb.append("Processing Unit Configuration:")
            .append("\n\tName: ").append(pu.getName())
            .append("\n\tInstances: ").append(pu.getNumberOfInstances())
            .append("\n\tBackups: ").append(pu.getNumberOfBackups())
            .append("\n\tInstances per-machine: ").append(pu.getMaxInstancesPerMachine())
            .append("\n\tPlanned instances: ").append(pu.getTotalNumberOfInstances())
            .append("\n\tActual instances: ").append(pu.getProcessingUnitInstances().length)
            .append("\n\tDeployment status: ").append(pu.getStatus())
            ;
            return sb.toString();
        }


        //could be broken because of failed deployment,
        //could be compromised because of service limit, or max-per-machine constraints
        private void handleBrokenDedicatedIsolation(ProcessingUnit pu) {
            Properties contextProperties = pu.getBeanLevelProperties().getContextProperties();
            int scalingFactor = Integer.valueOf(contextProperties.getProperty("scalingFactor"));
            
            int minNumberOfGSCs = MemorySettings.valueOf(contextProperties.getProperty("minMemory")).floorDividedBy(
                    contextProperties.getProperty("jvmSize"));
            
            assert pu.getTotalNumberOfInstances() <= scalingFactor*minNumberOfGSCs;
            
            int maxNumberOfGSCsPerMachine = minNumberOfGSCs;
            if (pu.getNumberOfBackups() > 0) {
                maxNumberOfGSCsPerMachine = (int)Math.ceil(0.5*minNumberOfGSCs);
            }
            
            logger.finest("scalingFactor=" + scalingFactor + ", minNumberOfGSCs=" + minNumberOfGSCs
                    + ", maxNumberOfGSCsPerMachine=" + maxNumberOfGSCsPerMachine);
            
            String zoneName = pu.getRequiredZones()[0];
            while (numberOfGSCsInZone(zoneName) < minNumberOfGSCs) {
                boolean startedGSC = false;
                for (Machine machine : admin.getMachines()) {
                    if (!(machine.getGridServiceContainers().getSize() < maxNumberOfGSCsPerMachine))
                        continue;
                    if (meetsDedicatedIsolationConstaint(machine, zoneName)) {
                        GridServiceAgent agent = machine.getGridServiceAgent();
                        if (agent == null) continue; //to next machine

                        logger.finest("starting GSC");
                        //TODO check how many gscs we can load on this machine
                        agent.startGridServiceAndWait(new GridServiceContainerOptions()
                        .vmInputArgument("-Dcom.gs.zones="+zoneName)
                        .vmInputArgument("-Dcom.gigaspaces.grid.gsc.serviceLimit="+scalingFactor));
                        startedGSC = true;
                        break; //to check for min-gsc requirement
                    }
                }
                if (!startedGSC) {
                    logger.fine("needs another machine");
                    break; //try when scheduled again
                }
            }
        }
        
        
        private int numberOfGSCsInZone(String zoneName) {
            Zone zone = admin.getZones().getByName(zoneName);
            return zone == null ? 0 : zone.getGridServiceContainers().getSize();
        }



        // requires this machine to contain only GSCs matching the zone name provided
        private boolean meetsDedicatedIsolationConstaint(Machine machine, String zoneName) {
            for (GridServiceContainer gsc : machine.getGridServiceContainers()) {
                Map<String, Zone> gscZones = gsc.getZones();
                if (!gscZones.containsKey(zoneName)) {
                    return false; // GSC is of another zone
                }
            }

            return true;
        }
    }
    
    public static void main(String[] args) {
        new InternalESMImpl();
    }
}
