package org.openspaces.admin.pu.elastic.topology;

import org.openspaces.admin.pu.elastic.ElasticMachineProvisioningConfig;

/**
 * @author yohana
 * @since 10.1
 */
/**
 * Internal use only.
 * Used by shared-machine-provisioning bean to hold the discoveredMachineProvisioningConfig object.
 */
public class SharedMachineProvisioningInternal {
    public ElasticMachineProvisioningConfig elasticMachineProvisioningConfig;
    public String sharingId;

    public ElasticMachineProvisioningConfig getElasticMachineProvisioningConfig() {
        return elasticMachineProvisioningConfig;
    }

    public void setElasticMachineProvisioningConfig(ElasticMachineProvisioningConfig elasticMachineProvisioningConfig) {
        this.elasticMachineProvisioningConfig = elasticMachineProvisioningConfig;
    }

    public String getSharingId() {
        return sharingId;
    }

    public void setSharingId(String sharingId) {
        this.sharingId = sharingId;
    }
}
