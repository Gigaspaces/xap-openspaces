package org.openspaces.admin.space;

import org.openspaces.admin.pu.elastic.AdvancedElasticStatefulProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.ElasticStatefulProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.config.CapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.CapacityScaleConfigurer;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleConfigurer;
import org.openspaces.admin.pu.elastic.topology.ElasticStatefulDeploymentTopology;
import org.openspaces.core.util.MemoryUnit;

import com.gigaspaces.security.directory.UserDetails;

public class ElasticDataGridDeployment implements ElasticStatefulDeploymentTopology {

    private final AdvancedElasticStatefulProcessingUnitDeployment deployment;

    /**
     * Constructs a new Space deployment with the space name that will be created (it will also
     * be the processing unit name).
     */
    public ElasticDataGridDeployment(String spaceName) {
        this.deployment = new AdvancedElasticStatefulProcessingUnitDeployment("/templates/datagrid");
        this.deployment.name(spaceName);
        this.deployment.setContextProperty("dataGridName", spaceName);
    }

    public ElasticDataGridDeployment maxMemoryCapacity(int maxMemoryCapacity, MemoryUnit unit) {
        deployment.maxMemoryCapacity(maxMemoryCapacity,unit);
        return this;
    }

    public ElasticDataGridDeployment maxMemoryCapacity(String maxMemoryCapacity) {
        deployment.maxMemoryCapacity(maxMemoryCapacity);
        return this;
    }
    
    public ElasticDataGridDeployment memoryCapacityPerContainer(int memoryCapacityPerContainer, MemoryUnit unit) {
        deployment.memoryCapacityPerContainer(memoryCapacityPerContainer,unit);
        return this;
    }

    public ElasticDataGridDeployment memoryCapacityPerContainer(String memoryCapacityPerContainer) {
        deployment.memoryCapacityPerContainer(memoryCapacityPerContainer);
        return this;
    }
    
    public ElasticDataGridDeployment maxNumberOfCpuCores(int maxNumberOfCpuCores) {
        deployment.maxNumberOfCpuCores(maxNumberOfCpuCores);
        return this;
    }
    
    public ElasticDataGridDeployment scale(EagerScaleConfigurer strategy) {
        deployment.scale(strategy);
        return this;
    }

    public ElasticDataGridDeployment scale(ManualContainersScaleConfigurer strategy) {
        deployment.scale(strategy);
        return this;
    }

    public ElasticDataGridDeployment scale(ManualCapacityScaleConfigurer strategy) {
        deployment.scale(strategy);
        return this;
    }

    public ElasticDataGridDeployment scale(CapacityScaleConfigurer strategy) {
        deployment.scale(strategy);
        return this;
    }
    
    public ElasticDataGridDeployment scale(EagerScaleConfig strategy) {
        deployment.scale(strategy);
        return this;
    }

    public ElasticDataGridDeployment scale(ManualContainersScaleConfig strategy) {
        deployment.scale(strategy);
        return this;
    }

    public ElasticDataGridDeployment scale(ManualCapacityScaleConfig strategy) {
        deployment.scale(strategy);
        return this;
    }
    
    public ElasticDataGridDeployment scale(CapacityScaleConfig strategy) {
        deployment.scale(strategy);
        return this;
    }
    
    public ElasticDataGridDeployment name(String name) {
        deployment.name(name);
        return this;
    }

    /*UNIMPLEMENTED
    public ElasticDataGridDeployment zone(String zone) {
        deployment.zone(zone);
        return this;
    }
    */
    
    public ElasticDataGridDeployment setContextProperty(String key, String value) {
        deployment.setContextProperty(key, value);
        return this;
    }

    public ElasticDataGridDeployment secured(boolean secured) {
        deployment.secured(secured);
        return this;
    }

    public ElasticDataGridDeployment userDetails(UserDetails userDetails) {
        deployment.userDetails(userDetails);
        return this;
    }

    public ElasticDataGridDeployment userDetails(String userName, String password) {
        deployment.userDetails(userName, password);
        return this;
    }

    /*UNIMPLEMENTED
    public ElasticDataGridDeployment isolation(DedicatedIsolation isolation) {
        deployment.isolation(isolation);
        return this;
    }

    public ElasticDataGridDeployment isolation(SharedTenantIsolation isolation) {
        deployment.isolation(isolation);
        return this;
    }
    
    public ElasticDataGridDeployment isolation(PublicIsolation isolation) {
        deployment.isolation(isolation);
        return this;
    }
    */
    
    public ElasticDataGridDeployment useScriptToStartContainer() {
        deployment.useScriptToStartContainer();
        return this;
    }

    public ElasticDataGridDeployment overrideCommandLineArguments() {
        deployment.overrideCommandLineArguments();
        return this;
    }

    public ElasticDataGridDeployment commandLineArgument(String commandLineArgument) {
        deployment.commandLineArgument(commandLineArgument);
        return this;
    }

    public ElasticDataGridDeployment environmentVariable(String name, String value) {
        deployment.environmentVariable(name, value);
        return this;
    }

    public ElasticDataGridDeployment highlyAvailable(boolean highlyAvailable) {
        deployment.highlyAvailable(highlyAvailable);
        return this;
    }

    public ElasticDataGridDeployment machineProvisioning(ElasticMachineProvisioningConfig config) {
        deployment.machineProvisioning(config);
        return this;
    }

    protected ElasticDataGridDeployment numberOfBackupsPerPartition(int numberOfBackupsPerPartition) {
        deployment.numberOfBackupsPerPartition(numberOfBackupsPerPartition);
        return this;
    }

    protected ElasticDataGridDeployment numberOfPartitions(int numberOfPartitions) {
        deployment.numberOfPartitions(numberOfPartitions);
        return this;
    }

    protected ElasticDataGridDeployment minNumberOfCpuCoresPerMachine(double minNumberOfCpuCoresPerMachine) {
        deployment.minNumberOfCpuCoresPerMachine(minNumberOfCpuCoresPerMachine);
        return this;
    }

    protected ElasticDataGridDeployment allowDeploymentOnSingleMachine(boolean allowDeploymentOnSingleMachine) {
        deployment.allowDeploymentOnSingleMachine(allowDeploymentOnSingleMachine);
        return this;
    }

    protected ElasticDataGridDeployment allowDeploymentOnManagementMachine(boolean allowDeploymentOnManagementMachine) {
        deployment.allowDeploymentOnManagementMachine(allowDeploymentOnManagementMachine);
        return this;
    }
    public ElasticStatefulProcessingUnitDeployment toElasticStatefulProcessingUnitDeployment() {
       return deployment;
    }
}
