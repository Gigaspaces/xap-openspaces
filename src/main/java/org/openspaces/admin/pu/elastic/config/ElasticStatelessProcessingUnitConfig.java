/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.admin.pu.elastic.config;

import org.openspaces.admin.internal.pu.elastic.config.AbstractElasticProcessingUnitConfig;
import org.openspaces.admin.pu.config.ProcessingUnitConfig;
import org.openspaces.admin.pu.config.UserDetailsConfig;
import org.openspaces.admin.pu.dependency.ProcessingUnitDependency;
import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.topology.DedicatedMachineProvisioningInternal;
import org.openspaces.admin.pu.elastic.topology.SharedMachineProvisioningInternal;
import org.openspaces.admin.pu.topology.ProcessingUnitConfigHolder;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * @author itaif
 * @since 9.0.1
 */
@XmlRootElement(name = "elastic-stateless-pu")
public class ElasticStatelessProcessingUnitConfig extends AbstractElasticProcessingUnitConfig 
    implements ProcessingUnitConfigHolder {

    @Override
    public ProcessingUnitConfig toProcessingUnitConfig() {
        
        ProcessingUnitConfig processingUnitConfig = super.toProcessingUnitConfig();
        
        // disallow two instances to deploy on same Container
        processingUnitConfig.setMaxInstancesPerVM(1);
        
        // allow any number of instances to deploy on same Machine
        processingUnitConfig.setMaxInstancesPerMachine(0);
        
        return processingUnitConfig;
    }

    @XmlAttribute(name = "file")
    public void setProcessingUnitFile(String processingUnitFilePath) {
        super.setProcessingUnit(processingUnitFilePath);
    }

    @XmlAttribute(name = "puname")
    public void setProcessingUnitName(String processingUnitName) {
        super.setProcessingUnit(processingUnitName);
    }

    @Override
    @XmlAttribute(name = "memory-capacity-per-container-in-mb")
    public void setMemoryCapacityPerContainerInMB(long memoryInMB) {
        super.setMemoryCapacityPerContainerInMB(memoryInMB);
    }

    @Override
    @XmlElement(type = UserDetailsConfig.class)
    public void setUserDetails(UserDetailsConfig userDetails) {
        super.setUserDetails(userDetails);
    }

    @Override
    @XmlAttribute(name = "secured")
    public void setSecured(Boolean secured) {
        super.setSecured(secured);
    }

    @Override
    @XmlElement(type = ScaleStrategyConfig.class)
    public void setScaleStrategy(ScaleStrategyConfig scaleStrategy) {
        super.setScaleStrategy(scaleStrategy);
    }

    @Override
    @XmlElement(type = ElasticMachineProvisioningConfig.class)
    public void setMachineProvisioning(ElasticMachineProvisioningConfig machineProvisioningConfig) {
        super.setMachineProvisioning(machineProvisioningConfig);
    }

    @Override
    @XmlElement(type = ProcessingUnitDependency.class)
    public void setDeploymentDependencies(ProcessingUnitDependency[] dependencies) {
        super.setDeploymentDependencies(dependencies);
    }

    /**
     * Parse the shared-machine-provisioning bean, get its data and apply them to the relevant methods to enable shared machine provisioning
     */
    @XmlElement(type = SharedMachineProvisioningInternal.class)
    public void setSharedMachineProvisioning(SharedMachineProvisioningInternal sharedMachineProvisioningInternal) {
        this.setSharedIsolation(sharedMachineProvisioningInternal.getSharingId());
        this.setMachineProvisioning(sharedMachineProvisioningInternal.getElasticMachineProvisioningConfig());
    }

    /**
     * Parse the dedicated-machine-provisioning bean, get its data and apply them to the relevant methods to enable dedicated machine provisioning
     */
    @XmlElement(type = DedicatedMachineProvisioningInternal.class)
    public void setDedicatedMachineProvisioning(DedicatedMachineProvisioningInternal dedicatedMachineProvisioningInternal) {
        this.setDedicatedIsolation();
        this.setMachineProvisioning(dedicatedMachineProvisioningInternal.getElasticMachineProvisioningConfig());
    }
}
