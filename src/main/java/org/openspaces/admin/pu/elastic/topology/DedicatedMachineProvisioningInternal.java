package org.openspaces.admin.pu.elastic.topology;

import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;

/**
 * @author yohana
 * @since 10.1
 */

/**
 * Internal use only.
 * Used by dedicated-machine-provisioning bean to hold the discoveredMachineProvisioningConfig object.
 */
public class DedicatedMachineProvisioningInternal {
    public ElasticMachineProvisioningConfig elasticMachineProvisioningConfig;

    public ElasticMachineProvisioningConfig getElasticMachineProvisioningConfig() {
        return elasticMachineProvisioningConfig;
    }

    public void setElasticMachineProvisioningConfig(ElasticMachineProvisioningConfig elasticMachineProvisioningConfig) {
        this.elasticMachineProvisioningConfig= elasticMachineProvisioningConfig;
    }
}
