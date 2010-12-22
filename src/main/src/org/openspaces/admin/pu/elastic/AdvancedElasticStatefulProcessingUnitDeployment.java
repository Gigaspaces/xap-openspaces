package org.openspaces.admin.pu.elastic;

import java.io.File;

import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualContainersScaleConfigurer;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.ManualCapacityScaleConfigurer;
import org.openspaces.admin.pu.elastic.config.MemoryCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.MemoryCapacityScaleConfigurer;

import com.gigaspaces.security.directory.UserDetails;

/**
 * Exposes internal advanced properties that are not public in super class {@link ElasticStatefulProcessingUnitDeployment}
 * 
 * @author itaif
 *
 */
public class AdvancedElasticStatefulProcessingUnitDeployment extends ElasticStatefulProcessingUnitDeployment {

    public AdvancedElasticStatefulProcessingUnitDeployment(File processingUnit) {
        super(processingUnit);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment(String processingUnit) {
        super(processingUnit);
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment numberOfBackups(int numberOfBackups) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.numberOfBackups(numberOfBackups);
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment numberOfInstances(int numberOfInstances) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.numberOfInstances(numberOfInstances);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment maxInstancesPerMachine(int maxInstancesPerMachine) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.maxInstancesPerMachine(maxInstancesPerMachine);
    }
   
    public AdvancedElasticStatefulProcessingUnitDeployment scale(EagerScaleConfigurer beanConfig) {
        return scale(beanConfig.getConfig());
    }

    public AdvancedElasticStatefulProcessingUnitDeployment scale(ManualContainersScaleConfigurer beanConfig) {
        return scale(beanConfig.getConfig());
    }

    public AdvancedElasticStatefulProcessingUnitDeployment scale(ManualCapacityScaleConfigurer beanConfig) {
        return scale(beanConfig.getConfig());
    }

    public AdvancedElasticStatefulProcessingUnitDeployment scale(MemoryCapacityScaleConfigurer beanConfig) {
        return scale(beanConfig.getConfig());
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment scale(EagerScaleConfig strategy) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.scale(strategy);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment scale(ManualContainersScaleConfig strategy) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.scale(strategy);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment scale(ManualCapacityScaleConfig strategy) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.scale(strategy);
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment scale(MemoryCapacityScaleConfig strategy) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.scale(strategy);
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment name(String name) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.name(name);
    }
/* NOT IMPLEMENTED YET
    public AdvancedElasticStatefulProcessingUnitDeployment zone(String zone) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.zone(zone);
    }
    
    
    public AdvancedElasticStatefulProcessingUnitDeployment isolation(DedicatedIsolation isolation) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.isolation(isolation);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment isolation(SharedTenantIsolation isolation) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.isolation(isolation);
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment isolation(PublicIsolation isolation) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.isolation(isolation);
    }

*/
    public AdvancedElasticStatefulProcessingUnitDeployment setContextProperty(String key, String value) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.setContextProperty(key, value);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment secured(boolean secured) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.secured(secured);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment userDetails(UserDetails userDetails) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.userDetails(userDetails);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment userDetails(String userName, String password) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.userDetails(userName, password);
    }
    
    public AdvancedElasticStatefulProcessingUnitDeployment useScript() {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.useScript();
    }

    public AdvancedElasticStatefulProcessingUnitDeployment overrideCommandLineArguments() {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.overrideCommandLineArguments();
    }

    public AdvancedElasticStatefulProcessingUnitDeployment commandLineArgument(String vmInputArgument) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.commandLineArgument(vmInputArgument);
    }

    public AdvancedElasticStatefulProcessingUnitDeployment environmentVariable(String name, String value) {
        return (AdvancedElasticStatefulProcessingUnitDeployment) super.environmentVariable(name, value);
    }
    
}
