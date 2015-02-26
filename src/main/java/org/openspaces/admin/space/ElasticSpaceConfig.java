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
package org.openspaces.admin.space;

import org.openspaces.admin.pu.config.ProcessingUnitConfig;
import org.openspaces.admin.pu.config.UserDetailsConfig;
import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;
import org.openspaces.admin.pu.elastic.config.ElasticStatefulProcessingUnitConfig;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.admin.pu.elastic.topology.DedicatedMachineProvisioningInternal;
import org.openspaces.admin.pu.elastic.topology.SharedMachineProvisioningInternal;

import javax.xml.bind.annotation.*;

/**
 * @author itaif
 * @since 9.0.1
 */
@XmlRootElement(name = "elastic-space")
public class ElasticSpaceConfig extends ElasticStatefulProcessingUnitConfig {

    public ElasticSpaceConfig() {
        super();
        super.setProcessingUnit("/templates/datagrid");
    }
    
    @Override
    public ProcessingUnitConfig toProcessingUnitConfig() {
        super.addContextProperty("dataGridName", getName());
        return super.toProcessingUnitConfig();
    }

    @Override
    @XmlAttribute(name = "name")
    public void setName(String name) {
        super.setName(name);
    }

    @Override
    @XmlAttribute(name = "memory-capacity-per-container-in-mb")
    public void setMemoryCapacityPerContainerInMB(long memoryInMB) {
        super.setMemoryCapacityPerContainerInMB(memoryInMB);
    }

    @Override
    @XmlAttribute(name = "max-memory-capacity-in-mb")
    public void setMaxMemoryCapacityInMB(long maxMemoryCapacityInMB) {
        super.setMaxMemoryCapacityInMB(maxMemoryCapacityInMB);
    }

    @Override
    @XmlAttribute(name = "number-of-partitions")
    public void setNumberOfPartitions(int numberOfPartitions) {
        super.setNumberOfPartitions(numberOfPartitions);
    }

    @Override
    @XmlAttribute(name = "max-number-of-cpu-cores")
    public void setMaxNumberOfCpuCores(double maxNumberOfCpuCores) {
        super.setMaxNumberOfCpuCores(maxNumberOfCpuCores);
    }

    /**
     * @see org.openspaces.admin.space.ElasticSpaceDeployment#highlyAvailable(boolean)
     */
    @XmlAttribute(name = "highly-available")
    public void setHighlyAvailable(boolean highlyAvailable) {
        super.setNumberOfBackupInstancesPerPartition((highlyAvailable? 1:0));
    }

    /**
     * @see org.openspaces.admin.space.ElasticSpaceDeployment#singleMachineDeployment()
     */
    @XmlAttribute(name = "single-machine-deployment")
    public void setSingleMachineDeployment(boolean singleMachineDeployment) {
        if (singleMachineDeployment) {
            super.setMaxProcessingUnitInstancesFromSamePartitionPerMachine(0);
        }
    }

    @Override
    @XmlAttribute(name = "secured")
    public void setSecured(Boolean secured) {
        super.setSecured(secured);
    }


    @Override
    @XmlElement(type = UserDetailsConfig.class)
    public void setUserDetails(UserDetailsConfig userDetails) {
        super.setUserDetails(userDetails);
    }

    @Override
    @XmlElement(type = ElasticMachineProvisioningConfig.class)
    public void setMachineProvisioning(ElasticMachineProvisioningConfig machineProvisioningConfig) {
        super.setMachineProvisioning(machineProvisioningConfig);
    }

    @Override
    @XmlElement(type = ScaleStrategyConfig.class)
    public void setScaleStrategy(ScaleStrategyConfig scaleStrategy) {
        super.setScaleStrategy(scaleStrategy);
    }

    @Override
    @XmlAttribute(name = "number-of-backups-per-partition")
    public void setNumberOfBackupInstancesPerPartition(int numberOfBackupInstancesPerPartition) {
        super.setNumberOfBackupInstancesPerPartition(numberOfBackupInstancesPerPartition);
    }

    /**
     * Parse the shared-machine-provisioning bean, get its data and apply them to the relevant methods to enable shared machine provisioning
     */
    @XmlElement(type = SharedMachineProvisioningInternal.class)
    public void setSharedMachineProvisioning(SharedMachineProvisioningInternal sharedMachineProvisioningInternal) {
        this.setSharedIsolation(sharedMachineProvisioningInternal.getSharingId());
        this.setMachineProvisioning(sharedMachineProvisioningInternal.getDiscoveredMachineProvisioningConfig());
    }

    /**
     * Parse the dedicated-machine-provisioning bean, get its data and apply them to the relevant methods to enable dedicated machine provisioning
     */
    @XmlElement(type = DedicatedMachineProvisioningInternal.class)
    public void setDedicatedMachineProvisioning(DedicatedMachineProvisioningInternal dedicatedMachineProvisioningInternal) {
        this.setDedicatedIsolation();
        this.setMachineProvisioning(dedicatedMachineProvisioningInternal.getDiscoveredMachineProvisioningConfig());
    }
}
