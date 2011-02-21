package org.openspaces.grid.gsm.strategy;

import java.util.Arrays;
import java.util.HashSet;
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
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.elastic.ElasticMachineIsolationConfig;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.config.DiscoveredMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.grid.gsm.DiscoveredMachineProvisioningConfigAware;
import org.openspaces.grid.gsm.ElasticMachineProvisioningAware;
import org.openspaces.grid.gsm.GridServiceContainerConfigAware;
import org.openspaces.grid.gsm.LogPerProcessingUnit;
import org.openspaces.grid.gsm.ProcessingUnitAware;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
import org.openspaces.grid.gsm.capacity.AggregatedAllocatedCapacity;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.containers.ContainersSlaPolicy;
import org.openspaces.grid.gsm.machines.CapacityMachinesSlaPolicy;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.machines.NonBlockingElasticMachineProvisioning;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;

public class UndeployScaleStrategyBean 

    implements ScaleStrategyBean, 
               ContainersSlaEnforcementEndpointAware, 
               MachinesSlaEnforcementEndpointAware,
               ProcessingUnitAware,
               ElasticMachineProvisioningAware,
               GridServiceContainerConfigAware,
               DiscoveredMachineProvisioningConfigAware,
               Runnable {

    private static final String containersAlertGroupUidPrefix = "47A94111-5665-4214-9F7A-2962D998DD12";
    private static final String machinesAlertGroupUidPrefix = "3BA87E89-449A-4abc-A632-4732246A9EE4";

    // injected 
    private InternalAdmin admin;
    private EagerScaleConfig slaConfig;
    private MachinesSlaEnforcementEndpoint machinesEndpoint;
    private ContainersSlaEnforcementEndpoint containersEndpoint;
    private ProcessingUnit pu;
    private GridServiceContainerConfig containersConfig;
    private ProcessingUnitSchemaConfig schemaConfig;
    private DiscoveredMachineProvisioningConfig discoveredMachineProvisioningConfig;

    // created by afterPropertiesSet()
    private Log logger;
    private ScheduledFuture<?> scheduledTask;
    private NonBlockingElasticMachineProvisioning machineProvisioning;
    private ElasticMachineIsolationConfig isolationConfig;
    
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

    public void setMachinesSlaEnforcementEndpoint(MachinesSlaEnforcementEndpoint endpoint) {
        this.machinesEndpoint = endpoint;
    }
    
    public void setContainersSlaEnforcementEndpoint(ContainersSlaEnforcementEndpoint containersService) {
        this.containersEndpoint = containersService;
    }
    
    public void setElasticMachineProvisioning(NonBlockingElasticMachineProvisioning elasticMachineProvisioning) {
        this.machineProvisioning = elasticMachineProvisioning;
    }
    
    public void setElasticMachineIsolation(ElasticMachineIsolationConfig isolationConfig) {
        this.isolationConfig = isolationConfig;
    }
    
    public void setGridServiceContainerConfig(GridServiceContainerConfig containersConfig) {
         this.containersConfig = containersConfig;
    }

    public void setDiscoveredMachineProvisioningConfig(DiscoveredMachineProvisioningConfig config) {
        this.discoveredMachineProvisioningConfig = config;
    }
        
    public void afterPropertiesSet() {
        if (slaConfig == null) {
            throw new IllegalStateException("slaConfig cannot be null.");
        }
        
        logger = new LogPerProcessingUnit(
                    new SingleThreadedPollingLog(
                            LogFactory.getLog(UndeployScaleStrategyBean.class)),
                    pu);
        logger.info("sla properties: "+slaConfig.toString());
        
        if (!schemaConfig.isPartitionedSync2BackupSchema()) {
            throw new BeanConfigurationException("Processing Unit " + pu.getName() + " cannot scale by memory capacity, since it is not stateful and not a datagrid (it is " + schemaConfig.getSchema() +" . Choose a different scale algorithm.");
        }
        
        scheduledTask = 
        (admin).scheduleWithFixedDelayNonBlockingStateChange(
                this, 
       0L, slaConfig.getPollingIntervalSeconds(), TimeUnit.SECONDS);
       logger.debug(pu.getName() + " is being monitored for SLA violations every " + slaConfig.getPollingIntervalSeconds() + " seconds");
    }

    public void destroy() {
        logger.debug("destroying " + this.getClass().getName());
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
    }

    public void setProperties(Map<String, String> properties) {
        slaConfig = new EagerScaleConfig(properties);       
    }

    public void run() {
        
        logger.debug("Undeploying processing unit " + pu.getName());

        //TODO: Move this check to EsmImpl, this component should not be aware it is running in an ESM
        //TODO: Raise an alert
        int numberOfEsms = admin.getElasticServiceManagers().getSize();
        if (numberOfEsms != 1) {
            logger.error("Number of ESMs must be 1. Currently " + numberOfEsms + " running.");
            return;
        }
        
        try {
            
            logger.debug("Undeploying containers for " + pu.getName());
            boolean containersSlaEnforced = enforceContainersSla();
            if (logger.isDebugEnabled()) {
                if (!containersSlaEnforced) {
                    logger.debug("Containers undeploy is incomplete.");
                }
            }
            logger.debug("Undeploying machines for " + pu.getName());
            boolean machinesSlaEnforced = enforceMachinesSla();
            if (logger.isDebugEnabled()) {
                if (!machinesSlaEnforced) {
                    logger.debug("Machines undeploy incomplete.");
                }
            }
            
            if (containersSlaEnforced && machinesSlaEnforced) {
                logger.info(pu.getName() + " undeploy is complete.");
                // self destruct since all machines and containers are undeployed
                destroy();
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

    private boolean enforceMachinesSla() {
        final CapacityMachinesSlaPolicy sla = new CapacityMachinesSlaPolicy();
        sla.setMachineProvisioning(machineProvisioning);
        sla.setCpuCapacity(0);
        sla.setMemoryCapacityInMB(0);
        sla.setMinimumNumberOfMachines(0);
        sla.setAllowDeploymentOnManagementMachine(!slaConfig.getDedicatedManagementMachines());
        sla.setReservedMemoryCapacityPerMachineInMB(slaConfig.getReservedMemoryCapacityPerMachineInMB());
        sla.setContainerMemoryCapacityInMB(containersConfig.getMaximumJavaHeapSizeInMB());
        sla.setDiscoveredMachineZones(new HashSet<String>(Arrays.asList(discoveredMachineProvisioningConfig.getGridServiceAgentZones())));
        boolean reachedSla = machinesEndpoint.enforceSla(sla);
        
        if (reachedSla) {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RESOLVED,
                machinesAlertGroupUidPrefix,
                "Machines Capacity SLA",
                "Machines for " + pu.getName() + " have been terminated.");
        }
        else {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RAISED,
                containersAlertGroupUidPrefix,
                "Machines Capacity SLA",
                "Machines for " + pu.getName() + " are being terminated.");
        }
        
        return reachedSla;

    }

    private boolean enforceContainersSla() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        
        final ContainersSlaPolicy sla = new ContainersSlaPolicy();
        sla.setNewContainerConfig(containersConfig);
        sla.setReservedMemoryCapacityPerMachineInMB(slaConfig.getReservedMemoryCapacityPerMachineInMB());
        sla.setAllocatedCapacity(new AggregatedAllocatedCapacity());
        sla.setMemoryCapacityInMB(0);
        sla.setCpuCapacity(0);
        sla.setMinimumNumberOfMachines(0);
        
        boolean reachedSla = containersEndpoint.enforceSla(sla);
        
        if (reachedSla) {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RESOLVED,
                containersAlertGroupUidPrefix,
                "Containers Capacity SLA",
                "Containers for " + pu.getName() + " have been terminated.");
        }
        else {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RAISED,
                containersAlertGroupUidPrefix,
                "Containers Capacity SLA",
                "Contains capacity for " + pu.getName() + " " + 
                "Containers for " + pu.getName() + " are being terminated.");
        }
        
        return reachedSla;
    }
    

    private void triggerAlert(AlertSeverity severity, AlertStatus status, String alertGroupUidPrefix, String alertName, String alertDescription) {
        AlertFactory alertFactory = new AlertFactory();
        alertFactory.name(alertName);
        alertFactory.description(alertDescription);
        alertFactory.severity(severity);    
        alertFactory.status(status);
        alertFactory.componentUid(pu.getName());
        alertFactory.groupUid(alertGroupUidPrefix + "-" + pu.getName());
        admin.getAlertManager().triggerAlert(alertFactory.toAlert());
        logger.debug(alertDescription);
    }

}
