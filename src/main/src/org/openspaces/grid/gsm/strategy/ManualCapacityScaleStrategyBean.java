package org.openspaces.grid.gsm.strategy;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.grid.gsm.ElasticMachineProvisioningAware;
import org.openspaces.grid.gsm.GridServiceContainerConfigAware;
import org.openspaces.grid.gsm.ProcessingUnitAware;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.containers.ContainersSlaPolicy;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.machines.MachinesSlaPolicy;
import org.openspaces.grid.gsm.machines.NonBlockingElasticMachineProvisioning;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaPolicy;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;

public class ManualCapacityScaleStrategyBean 
    implements ScaleStrategyBean, 
               RebalancingSlaEnforcementEndpointAware , 
               ContainersSlaEnforcementEndpointAware, 
               MachinesSlaEnforcementEndpointAware,
               ProcessingUnitAware,
               ElasticMachineProvisioningAware,
               GridServiceContainerConfigAware,
               Runnable {

    private static final Log logger = LogFactory.getLog(ManualCapacityScaleStrategyBean.class);
    private static final String rebalancingAlertGroupUidPrefix = "4499C1ED-1584-4387-90CF-34C5EC236644";
    private static final String containersAlertGroupUidPrefix = "47A94111-5665-4214-9F7A-2962D998DD12";
    private static final String machinesAlertGroupUidPrefix = "3BA87E89-449A-4abc-A632-4732246A9EE4";

    // injected 
    private InternalAdmin admin;
    private ManualCapacityScaleConfig slaConfig;
    private MachinesSlaEnforcementEndpoint machinesService;
    private ContainersSlaEnforcementEndpoint containersService;
    private RebalancingSlaEnforcementEndpoint rebalancingService;
    private ProcessingUnit pu;
    private GridServiceContainerConfig containersConfig;
    private ProcessingUnitSchemaConfig schemaConfig;
    
    // created by afterPropertiesSet()
    private ScheduledFuture<?> scheduledTask;
    private int targetNumberOfContainers;
    private int minimumNumberOfMachines;
    private NonBlockingElasticMachineProvisioning machineProvisioning;
    

    public Map<String, String> getProperties() {
        return slaConfig.getProperties();
    }

    public void setProcessingUnit(ProcessingUnit pu) {
        this.pu = pu;
    }

    public void setProcessingUnitSchema(ProcessingUnitSchemaConfig schemaConfig) {
        this.schemaConfig = schemaConfig;
    }
    
    public void setAdmin(Admin admin) {
        this.admin = (InternalAdmin) admin;
    }

    public void setMachinesSlaEnforcementEndpoint(MachinesSlaEnforcementEndpoint machinesService) {
        this.machinesService = machinesService;
    }

    public void setContainersSlaEnforcementEndpoint(ContainersSlaEnforcementEndpoint containersService) {
        this.containersService = containersService;
    }
    
    public void setRebalancingSlaEnforcementEndpoint(RebalancingSlaEnforcementEndpoint relocationService) {
        this.rebalancingService = relocationService;
    }

    public void setElasticMachineProvisioning(NonBlockingElasticMachineProvisioning elasticMachineProvisioning) {
        this.machineProvisioning = elasticMachineProvisioning;
     }

    public void setGridServiceContainerConfig(GridServiceContainerConfig containersConfig) {
         this.containersConfig = containersConfig;
    }
     
    public void afterPropertiesSet() {
        if (slaConfig == null) {
            throw new IllegalStateException("slaConfig cannot be null.");
        }
        
        logger.info("sla properties: "+slaConfig.toString());
        
        if (!schemaConfig.isPartitionedSync2BackupSchema()) {
            throw new BeanConfigurationException("Processing Unit " + pu.getName() + " cannot scale by memory capacity, since it is not stateful and not a datagrid (it is " + schemaConfig.getSchema() +" . Choose a different scale algorithm.");
        }
        
        int numberOfBackups = pu.getNumberOfBackups();
       
        if (slaConfig.getMinNumberOfContainers() != 0 &&
            slaConfig.getMinNumberOfContainers() <  1 + numberOfBackups) {
            throw new BeanConfigurationException(
                    "Minimum number of containers " + slaConfig.getMinNumberOfContainers() + " " + 
                    "cannot support " + (numberOfBackups==1?"one backup":numberOfBackups+" backups") + " per partition. "+
                    "Either don't use the minimum number of containers property or set it to " + (numberOfBackups+1));
        }
        
        // calculate minimum number of machines
        minimumNumberOfMachines = calcMinNumberOfMachines(pu);

        this.targetNumberOfContainers = calcTargetNumberOfContainers();
        
        scheduledTask = 
        (admin).scheduleWithFixedDelayNonBlockingStateChange(
        this, 0L, slaConfig.getPollingIntervalSeconds(), TimeUnit.SECONDS);
    }

    private int calcMinNumberOfMachines(ProcessingUnit pu) {
        int minNumberOfMachines;
        if (pu.getMaxInstancesPerMachine() == 0) {
            minNumberOfMachines = 1;
            logger.info("minNumberOfMachines=1 (since maxInstancesPerMachine is disabled)");
        }
        
        else {
            minNumberOfMachines = (int)Math.ceil(
                    (1 + pu.getNumberOfBackups())/1.0*pu.getMaxInstancesPerMachine());
            logger.info("minNumberOfMachines= " +
                    "ceil((1+backupsPerPartition)/maxInstancesPerMachine)= "+
                    "ceil("+(1+pu.getNumberOfBackups())+"/"+pu.getMaxInstancesPerMachine() + ")= " +
                    minNumberOfMachines);
        }
        
        return minNumberOfMachines;
    }

    public void destroy() {
        
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
    }

    public void setProperties(Map<String, String> properties) {
        slaConfig = new ManualCapacityScaleConfig(properties);       
    }

    public void run() {
        try {
            
            boolean machinesSlaEnforced = enforceMachinesSla();
            
            if (machinesSlaEnforced || 
                machinesService.getGridServiceAgentsPendingShutdown().length >0) {

                boolean containersSlaEnforced = enforceContainersSla();

                if (containersSlaEnforced || 
                    containersService.getContainersPendingShutdown().length > 0) {

                    enforceRebalancingSla(containersService.getContainers());
                }
            }
        }
        catch (ServiceLevelAgreementEnforcementEndpointDestroyedException e) {
            logger.debug("AdminService was destroyed",e);
        }
        catch (AdminException e) {
            logger.warn("Unhandled AdminException",e);
        }
        catch (Exception e) {
            logger.error("Unhandled Exception",e);
        }
        
    }

    private int calcTargetNumberOfContainers() {
        
        if (slaConfig.getMemoryCapacityInMB() <= 0) {
            throw new BeanConfigurationException("The specified memory capacity " + slaConfig.getMemoryCapacityInMB() + "m cannot be negative or zero.");
        }
        
        double totalNumberOfInstances = pu.getTotalNumberOfInstances();
        double instanceCapacityInMB = slaConfig.getMemoryCapacityInMB()/totalNumberOfInstances;
        logger.info(
                "instanceCapacityInMB= "+
                "memoryCapacityInMB/(numberOfInstances*(1+numberOfBackups))= "+
                slaConfig.getMemoryCapacityInMB()+"/"+totalNumberOfInstances+"= " +
                instanceCapacityInMB);
        
        double containerCapacityInMB = containersConfig.getMaximumJavaHeapSizeInMB();
        
        if (containerCapacityInMB < instanceCapacityInMB) {
            throw new BeanConfigurationException(
                    "Container capacity is " + containerCapacityInMB+"MB , "+
                    "given " + totalNumberOfInstances + " instances, the total capacity =" 
                    +containerCapacityInMB+"MB *"+totalNumberOfInstances + "= " + 
                    containerCapacityInMB*totalNumberOfInstances+"MB. "+
                    "Reduce total capacity from " + slaConfig.getMemoryCapacityInMB() +"MB to " + containerCapacityInMB*totalNumberOfInstances+"MB."); 
        }
        double maxNumberOfInstancesPerContainer = Math.floor(containerCapacityInMB / instanceCapacityInMB); 
        logger.info(
                "maxNumberOfInstancesPerContainer= "+
                "floor(containerCapacityInMB/instanceCapacityInMB)= "+
                "floor("+containerCapacityInMB+"/"+instanceCapacityInMB+") =" +
                maxNumberOfInstancesPerContainer);
        
        double targetNumberOfContainers = Math.ceil(totalNumberOfInstances/ maxNumberOfInstancesPerContainer);
        logger.info(
                "targetNumberOfContainers= "+
                "ceil(totalNumberOfInstances/maxNumberOfInstancesPerContainer)= "+
                "ceil("+totalNumberOfInstances+"/"+maxNumberOfInstancesPerContainer+") =" +
                targetNumberOfContainers);
        
        int minNumberOfContainers = slaConfig.getMinNumberOfContainers();
        if (minNumberOfContainers != 0 && targetNumberOfContainers < minNumberOfContainers) {
                
            // raise exception if min number of containers conflicts with the specified memory capacity.
            int recommendedMemoryCapacityInMB = (int)(minNumberOfContainers * containerCapacityInMB);
            throw new BeanConfigurationException(
                    targetNumberOfContainers + " containers are needed in order to scale to " + slaConfig.getMemoryCapacityInMB() + "m . "+
                    "The minimum number of containers is set to " + minNumberOfContainers + ". "+
                    "Either decrease the minimum number of containers to " + targetNumberOfContainers +" or " +
                    "increase the memory capacity to " + recommendedMemoryCapacityInMB +"m");
        }
                
        int numberOfBackups = pu.getNumberOfBackups();
        if (targetNumberOfContainers < numberOfBackups +1) {
         // raise exception if min number of containers conflicts with the specified memory capacity.
            int recommendedMemoryCapacityInMB = (int)((numberOfBackups +1) * containerCapacityInMB);
            throw new BeanConfigurationException(
                    targetNumberOfContainers + " containers are needed in order to scale to " + slaConfig.getMemoryCapacityInMB() + "m , "+
                    "which cannot support " + (numberOfBackups==1?"one backup":numberOfBackups+" backups") + " per partition. "+
                    "Increase the memory capacity to " + recommendedMemoryCapacityInMB +"m");
        }
        
        int maxNumberOfContainers = slaConfig.getMaxNumberOfContainers();
        if (maxNumberOfContainers != 0 && targetNumberOfContainers > maxNumberOfContainers) {
            
            // raise exception if max number of containers conflicts with the specified memory capacity.
            double recommendedMaxNumberOfInstancesPerContainer = Math.ceil(totalNumberOfInstances/maxNumberOfContainers);
            double recommendedInstanceCapacityInMB = containerCapacityInMB / recommendedMaxNumberOfInstancesPerContainer;
            int recommendedMemoryCapacityInMB = (int)Math.floor(recommendedInstanceCapacityInMB * totalNumberOfInstances);
            if (recommendedMemoryCapacityInMB >= slaConfig.getMemoryCapacityInMB()) {
                throw new IllegalStateException("recommended capacity is bigger than specified capacity. recommendedMemoryCapacityInMB (="+recommendedInstanceCapacityInMB+") >= " + slaConfig.getMemoryCapacityInMB());
            }
                
            throw new BeanConfigurationException(
                    targetNumberOfContainers + " containers are needed in order to scale to " + slaConfig.getMemoryCapacityInMB() + "m . "+
                    "The maximum number of containers is set to " + maxNumberOfContainers + ". "+
                    "Either increase the maximum number of containers to " + targetNumberOfContainers +" or " +
                    "decrease the memory capacity to " + recommendedMemoryCapacityInMB +"m");
        }
        
        return (int) targetNumberOfContainers;
    }


    private boolean enforceMachinesSla() {
        final MachinesSlaPolicy sla = new MachinesSlaPolicy();
        sla.setMachineProvisioning(machineProvisioning);
        sla.setCpu(0); // TODO: slaConfig.getCpu()
        long targetMemory = targetNumberOfContainers * containersConfig.getMaximumJavaHeapSizeInMB();
        sla.setMemoryCapacityInMB(targetMemory);
        sla.setMinimumNumberOfMachines(minimumNumberOfMachines);
        boolean reachedSla = machinesService.enforceSla(sla);
        
        if (reachedSla) {
            fireAlert(
                AlertSeverity.WARNING,
                AlertStatus.RESOLVED,
                machinesAlertGroupUidPrefix,
                "Machines Capacity SLA",
                "Total machines memory for " + pu.getName() + " " + 
                "has reached its target of " + targetMemory + "MB");
        }
        else {
            fireAlert(
                AlertSeverity.WARNING,
                AlertStatus.RAISED,
                containersAlertGroupUidPrefix,
                "Machines Capacity SLA",
                "Total machines memory for " + pu.getName() + " " + 
                "is below the target "+ targetMemory + "MB");
        }
        
        return reachedSla;

    }
    
    private boolean enforceContainersSla() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        
        final ContainersSlaPolicy sla = new ContainersSlaPolicy();
        sla.setTargetNumberOfContainers(targetNumberOfContainers);
        sla.setNewContainerConfig(containersConfig);
        sla.setGridServiceAgents(machinesService.getGridServiceAgents());
        sla.setMinimumNumberOfMachines(minimumNumberOfMachines);
        
        boolean reachedSla = containersService.enforceSla(sla);
        
        if (reachedSla) {
            fireAlert(
                AlertSeverity.WARNING,
                AlertStatus.RESOLVED,
                containersAlertGroupUidPrefix,
                "Containers Capacity SLA",
                "Target number of containers for " + pu.getName() + " " + 
                "has been reached: " + targetNumberOfContainers);
        }
        else {
            fireAlert(
                AlertSeverity.WARNING,
                AlertStatus.RAISED,
                containersAlertGroupUidPrefix,
                "Containers Capacity SLA",
                "Target number of containers for " + pu.getName() + " " + 
                "is " + targetNumberOfContainers + ". " +
                "Current number of containers is " + 
                containersService.getContainers().length);
        }
        
        return reachedSla;
    }
    
    private boolean enforceRebalancingSla(GridServiceContainer[] containers) 
        throws ServiceLevelAgreementEnforcementEndpointDestroyedException 
    {
        RebalancingSlaPolicy sla = new RebalancingSlaPolicy();
        sla.setContainers(containers);
        sla.setMaximumNumberOfConcurrentRelocationsPerMachine(slaConfig.getMaximumNumberOfConcurrentRelocationsPerMachine());
        
        boolean slaEnforced = rebalancingService.enforceSla(sla);
        
        if (slaEnforced) {
            fireAlert(
                AlertSeverity.WARNING,
                AlertStatus.RESOLVED,
                rebalancingAlertGroupUidPrefix,
                "Processing Unit Rebalancing SLA",
                "Rebalancing of " + pu.getName() + " is complete.");
        }
        else {
            fireAlert(
                AlertSeverity.WARNING,
                AlertStatus.RAISED,
                rebalancingAlertGroupUidPrefix,
                "Processing Unit Rebalancing SLA",
                "Rebalancing of " + pu.getName() + " is in progress.");
        }
        
        return slaEnforced;
    }

    private void fireAlert(AlertSeverity severity, AlertStatus status, String alertGroupUidPrefix, String alertName, String alertDescription) {
        AlertFactory alertFactory = new AlertFactory();
        alertFactory.name(alertName);
        alertFactory.description(alertDescription);
        alertFactory.severity(severity);        
        alertFactory.componentUid(pu.getName());
        alertFactory.groupUid(alertGroupUidPrefix + "-" + pu.getName());
        admin.getAlertManager().fireAlert(alertFactory.toAlert());
        logger.debug(alertDescription);
    }
}
