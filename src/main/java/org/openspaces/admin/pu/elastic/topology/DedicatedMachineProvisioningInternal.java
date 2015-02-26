package org.openspaces.admin.pu.elastic.topology;

import org.openspaces.admin.pu.elastic.config.DiscoveredMachineProvisioningConfig;

/**
 * @author yohana
 * @since 10.1
 */

/**
 * Internal use only.
 * Used by dedicated-machine-provisioning bean to hold the discoveredMachineProvisioningConfig object.
 */
public class DedicatedMachineProvisioningInternal {
    public DiscoveredMachineProvisioningConfig discoveredMachineProvisioningConfig;

    public DiscoveredMachineProvisioningConfig getDiscoveredMachineProvisioningConfig() {
        return discoveredMachineProvisioningConfig;
    }

    public void setDiscoveredMachineProvisioningConfig(DiscoveredMachineProvisioningConfig discoveredMachineProvisioningConfig) {
        this.discoveredMachineProvisioningConfig = discoveredMachineProvisioningConfig;
    }
}
