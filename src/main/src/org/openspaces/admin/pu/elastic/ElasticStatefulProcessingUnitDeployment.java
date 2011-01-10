package org.openspaces.admin.pu.elastic;

import java.io.File;
import java.util.Map;

import org.openspaces.admin.bean.BeanConfig;
import org.openspaces.admin.internal.pu.elastic.AbstractElasticProcessingUnitDeployment;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
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

/**
 * Defines an elastic deployment of a processing unit that with an embedded space..
 * 
 * The advantage of this topology is that the code can access the data without
 * the network/serialization overhead and that space events can be used as code triggers.
 * The disadvantage compared to a stateless processing unit is that the ratio between 
 * the minimum and maximum number of containers is limited.
 * 
 * @author itaif
 */
public class ElasticStatefulProcessingUnitDeployment extends AbstractElasticProcessingUnitDeployment implements ElasticStatefulDeploymentTopology {

    public static final String MAX_MEMORY_CAPACITY_MEGABYTES_DYNAMIC_PROPERTY = "max-memory-capacity-megabytes";
    public static final String MIN_MEMORY_CAPACITY_MEGABYTES_DYNAMIC_PROPERTY = "min-memory-capacity-megabytes";
    
    Map<String,String> scaleStrategy;
    private long maxMemoryCapacityInMB;
    private int numberOfBackupInstancesPerPartition = 1;
    private int numberOfPartitions;
    private int maxPartitionInstancesPerMachine = 1;
    private double maxNumberOfCpuCores = 1;
    
    /**
     * Constructs a stateful processing unit deployment based on the specified processing unit name (should
     * exists under the <code>[GS ROOT]/deploy</code> directory.
     */
    public ElasticStatefulProcessingUnitDeployment(String processingUnit) {
        super(processingUnit);
        new ProcessingUnitSchemaConfig(super.getElasticProperties()).setPartitionedSync2BackupSchema();        
    }
    
    /**
     * Constructs a stateful processing unit deployment based on the specified processing unit file path 
     * (points either to a processing unit jar/zip file or a directory).
     */
    public ElasticStatefulProcessingUnitDeployment(File processingUnit) {
        this(processingUnit.getAbsolutePath());
    }

    public ElasticStatefulProcessingUnitDeployment maxMemoryCapacity(int maxMemoryCapacity, MemoryUnit unit) {
        this.maxMemoryCapacityInMB = unit.toMegaBytes(maxMemoryCapacity);
        return this;
    }

    public ElasticStatefulProcessingUnitDeployment maxMemoryCapacity(String maxMemoryCapacity) {
        this.maxMemoryCapacityInMB = MemoryUnit.toMegaBytes(maxMemoryCapacity);
        return this;
    }

    public ElasticStatefulProcessingUnitDeployment highlyAvailable(boolean highlyAvailable) {
        numberOfBackupsPerPartition(highlyAvailable ? 1 : 0);
        return this;
    }
    
    /**
     * Overrides the number of backup instances per partition.
     */
    protected ElasticStatefulProcessingUnitDeployment numberOfBackupsPerPartition(int numberOfBackupsPerPartition) {
        this.numberOfBackupInstancesPerPartition = numberOfBackupsPerPartition;
        return this;
    }
    
    /**
     * Overrides the number of partition.
     */
    protected ElasticStatefulProcessingUnitDeployment numberOfPartitions(int numberOfPartitions) {
        this.numberOfPartitions = numberOfPartitions;
        return this;
    }

    /**
     * Overrides the number of instances from the same partition per machine.
     */
    protected ElasticStatefulProcessingUnitDeployment maxParitionInstancesPerMachine(int maxPartitionInstancesPerMachine) {
        this.maxPartitionInstancesPerMachine  = maxPartitionInstancesPerMachine;
        return this;
    }
    
    public ElasticStatefulProcessingUnitDeployment maxNumberOfCpuCores(int maxNumberOfCpuCores) {
        this.maxNumberOfCpuCores = maxNumberOfCpuCores;
        return this;
    }

    public ElasticStatefulProcessingUnitDeployment memoryCapacityPerContainer(int memoryCapacityPerContainer, MemoryUnit unit) {
        super.memoryCapacityPerContainer(memoryCapacityPerContainer,unit);
        return this;
    }

    public ElasticStatefulProcessingUnitDeployment memoryCapacityPerContainer(String memoryCapacityPerContainer) {
        super.memoryCapacityPerContainer(memoryCapacityPerContainer);
        return this;
    }

    public ElasticStatefulProcessingUnitDeployment scale(EagerScaleConfigurer strategy) {
        return scale(strategy.getConfig());
    }

    public ElasticStatefulProcessingUnitDeployment scale(ManualContainersScaleConfigurer strategy) {
        return scale(strategy.getConfig());
    }

    public ElasticStatefulProcessingUnitDeployment scale(ManualCapacityScaleConfigurer strategy) {
        return scale(strategy.getConfig());
    }

    public ElasticStatefulProcessingUnitDeployment scale(CapacityScaleConfigurer strategy) {
        return scale(strategy.getConfig());
    }
    
    public ElasticStatefulProcessingUnitDeployment scale(EagerScaleConfig strategy) {
        return (ElasticStatefulProcessingUnitDeployment) super.scale(strategy);
    }

    public ElasticStatefulProcessingUnitDeployment scale(ManualContainersScaleConfig strategy) {
        return (ElasticStatefulProcessingUnitDeployment) super.scale(strategy);
    }

    public ElasticStatefulProcessingUnitDeployment scale(ManualCapacityScaleConfig strategy) {
        return (ElasticStatefulProcessingUnitDeployment) super.scale(strategy);
    }
    
    public ElasticStatefulProcessingUnitDeployment scale(CapacityScaleConfig strategy) {
        return (ElasticStatefulProcessingUnitDeployment) super.scale(strategy);
    }
    
    public ElasticStatefulProcessingUnitDeployment name(String name) {
        return (ElasticStatefulProcessingUnitDeployment) super.name(name);
    }

    /* Unimplemented 
    public ElasticStatefulProcessingUnitDeployment zone(String zone) {
        return (ElasticStatefulProcessingUnitDeployment) super.zone(zone);
    }
    */
    
    public ElasticStatefulProcessingUnitDeployment setContextProperty(String key, String value) {
        return (ElasticStatefulProcessingUnitDeployment) super.setContextProperty(key, value);
    }

    public ElasticStatefulProcessingUnitDeployment secured(boolean secured) {
        return (ElasticStatefulProcessingUnitDeployment) super.secured(secured);
    }

    public ElasticStatefulProcessingUnitDeployment userDetails(UserDetails userDetails) {
        return (ElasticStatefulProcessingUnitDeployment) super.userDetails(userDetails);
    }

    public ElasticStatefulProcessingUnitDeployment userDetails(String userName, String password) {
        return (ElasticStatefulProcessingUnitDeployment) super.userDetails(userName, password);
    }
    /* Unimplemented
    public ElasticStatefulProcessingUnitDeployment isolation(DedicatedIsolation isolation) {
        return (ElasticStatefulProcessingUnitDeployment) super.isolation(isolation);
    }

    
    public ElasticStatefulProcessingUnitDeployment isolation(SharedTenantIsolation isolation) {
        return (ElasticStatefulProcessingUnitDeployment) super.isolation(isolation);
    }
    
    public ElasticStatefulProcessingUnitDeployment isolation(PublicIsolation isolation) {
        return (ElasticStatefulProcessingUnitDeployment) super.isolation(isolation);
    }
    */
    
    public ElasticStatefulProcessingUnitDeployment useScriptToStartContainer() {
        return (ElasticStatefulProcessingUnitDeployment) super.useScriptToStartContainer();
    }

    public ElasticStatefulProcessingUnitDeployment overrideCommandLineArguments() {
        return (ElasticStatefulProcessingUnitDeployment) super.overrideCommandLineArguments();
    }

    public ElasticStatefulProcessingUnitDeployment commandLineArgument(String vmInputArgument) {
        return (ElasticStatefulProcessingUnitDeployment) super.commandLineArgument(vmInputArgument);
    }

    public ElasticStatefulProcessingUnitDeployment environmentVariable(String name, String value) {
        return (ElasticStatefulProcessingUnitDeployment) super.environmentVariable(name, value);
    }
    
    public ElasticStatefulProcessingUnitDeployment machineProvisioning(BeanConfig config) {
        return (ElasticStatefulProcessingUnitDeployment) super.machineProvisioning(config);
    }
    
    public ProcessingUnitDeployment toProcessingUnitDeployment() {

        ProcessingUnitDeployment deployment = super.toProcessingUnitDeployment();
        
        if (this.maxMemoryCapacityInMB == 0) {
            throw new IllegalStateException("maxMemoryCapacity is too low.");
        }

        int numberOfInstances = this.numberOfPartitions;
        if (numberOfInstances == 0) {
            numberOfInstances = Math.max(calcNumberOfPartitionsFromMemoryRequirements(),calcNumberOfPartitionsFromCpuRequirements());
        }
        
        deployment
        .maxInstancesPerMachine(this.maxPartitionInstancesPerMachine)
        .partitioned(numberOfInstances, this.numberOfBackupInstancesPerPartition);
      
        return deployment;
    }
    
    protected int calcNumberOfPartitionsFromMemoryRequirements() {
        
        long maximumJavaHeapSizeMegabytes = new GridServiceContainerConfig(super.getElasticProperties()).getMaximumJavaHeapSizeInMB();
                
        if (maximumJavaHeapSizeMegabytes == 0) {
            throw new IllegalStateException("-Xmx CommandLineArgument is undefined.");    
        }
                
        double totalNumberOfInstances = Math.ceil(((double)maxMemoryCapacityInMB)/maximumJavaHeapSizeMegabytes);
        int numberOfPartitions = (int) Math.ceil(totalNumberOfInstances / (numberOfBackupInstancesPerPartition+1));
                
        return Math.max(1, numberOfPartitions);
    }

    protected int calcNumberOfPartitionsFromCpuRequirements() {
        int coresPerMachine = super.getMachineProvisioningConfig().getMinimumNumberOfCpuCoresPerMachine();
        int maximumNumberOfActiveInstances =(int) Math.ceil(this.maxNumberOfCpuCores / coresPerMachine); 
        return maximumNumberOfActiveInstances; 
    }

}