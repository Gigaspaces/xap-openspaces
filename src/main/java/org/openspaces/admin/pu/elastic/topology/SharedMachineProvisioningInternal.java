package org.openspaces.admin.pu.elastic.topology;

import org.openspaces.admin.pu.elastic.config.DiscoveredMachineProvisioningConfig;

/**
 * @author yohana
 * @since 10.1
 */
/**
 * Internal use only.
 * Used by shared-machine-provisioning bean to hold the discoveredMachineProvisioningConfig object.
 */
public class SharedMachineProvisioningInternal {
    public DiscoveredMachineProvisioningConfig discoveredMachineProvisioningConfig;
    public String sharingId;

    public DiscoveredMachineProvisioningConfig getDiscoveredMachineProvisioningConfig() {
        return discoveredMachineProvisioningConfig;
    }

    public void setDiscoveredMachineProvisioningConfig(DiscoveredMachineProvisioningConfig discoveredMachineProvisioningConfig) {
        this.discoveredMachineProvisioningConfig = discoveredMachineProvisioningConfig;
    }

    public String getSharingId() {
        return sharingId;
    }

    public void setSharingId(String sharingId) {
        this.sharingId = sharingId;
    }
}
