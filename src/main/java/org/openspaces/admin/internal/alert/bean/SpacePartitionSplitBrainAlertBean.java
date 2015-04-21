/*******************************************************************************
 * 
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
 *  
 ******************************************************************************/
package org.openspaces.admin.internal.alert.bean;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import org.openspaces.admin.Admin;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.alert.alerts.SpacePartitionSplitBrainAlert;
import org.openspaces.admin.alert.config.SpacePartitionSplitBrainAlertConfiguration;
import org.openspaces.admin.internal.alert.InternalAlertManager;
import org.openspaces.admin.internal.alert.bean.util.AlertBeanUtils;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.SpacePartition;
import org.openspaces.admin.space.events.SpaceInstanceAddedEventListener;
import org.openspaces.admin.space.events.SpaceModeChangedEvent;
import org.openspaces.admin.space.events.SpaceModeChangedEventListener;
import org.openspaces.admin.space.events.SpaceRemovedEventListener;

import java.util.Map;

public class SpacePartitionSplitBrainAlertBean implements AlertBean, SpaceInstanceAddedEventListener, SpaceRemovedEventListener, SpaceModeChangedEventListener {


    public static final String beanUID = "2c80074c-186b7266-9e07-448b-8464-627c746bdfaf";
    public static final String ALERT_NAME = "Space Partition Split-Brain";
    private Admin admin;
    private final SpacePartitionSplitBrainAlertConfiguration config = new SpacePartitionSplitBrainAlertConfiguration();
    
    @Override
    public void afterPropertiesSet() throws Exception {
        admin.getSpaces().getSpaceInstanceAdded().add(this);
        admin.getSpaces().getSpaceRemoved().add(this);
        admin.getSpaces().getSpaceModeChanged().add(this);
    }

    @Override
    public void destroy() throws Exception {
        admin.getSpaces().getSpaceInstanceAdded().remove(this);
        admin.getSpaces().getSpaceRemoved().remove(this);
        admin.getSpaces().getSpaceModeChanged().remove(this);
    }

    @Override
    public Map<String, String> getProperties() {
        return config.getProperties();
    }

    @Override
    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        config.setProperties(properties);
    }


    @Override
    public void spaceInstanceAdded(SpaceInstance spaceInstance) {
        SpacePartition partition = spaceInstance.getPartition();
        if (partition.getInstances().length <= 1) {
            return;
        }

        int numberOfPrimaries = 0;
        int numberOfBackups = 0;
        for (SpaceInstance instance : partition.getInstances()) {
            if (instance.getMode() == SpaceMode.PRIMARY) {
                ++numberOfPrimaries;
            } else if (instance.getMode() == SpaceMode.BACKUP) {
                ++numberOfBackups;
            }
        }

        if (numberOfPrimaries > 1) {
            handleSplitBrainDetection(spaceInstance, AlertStatus.RAISED);
        } else if (numberOfPrimaries == 1 && numberOfBackups > 0){
            handleSplitBrainDetection(spaceInstance, AlertStatus.RESOLVED);
        }
    }


    @Override
    public void spaceModeChanged(SpaceModeChangedEvent event) {
        spaceInstanceAdded(event.getSpaceInstance());
    }

    @Override
    public void spaceRemoved(Space space) {
        for (SpaceInstance spaceInstance : space) {
            handleSplitBrainDetection(spaceInstance, AlertStatus.NA);
        }
    }

    private void handleSplitBrainDetection(SpaceInstance spaceInstance, AlertStatus alertStatus) {
        String spaceName = spaceInstance.getSpace().getName();
        String partitionName = spaceName + "." + (spaceInstance.getPartition().getPartitionId()+1);
        final String groupUid = generateGroupUid(partitionName);
        AlertFactory factory = new AlertFactory();
        factory.name(ALERT_NAME);
        factory.groupUid(groupUid);
        factory.componentUid(spaceInstance.getSpace().getUid());
        factory.componentDescription(getLocation(spaceInstance));

        factory.config(config.getProperties());
        factory.severity(AlertSeverity.SEVERE);
        if (alertStatus.isRaised()) {
            factory.description("Detected split-brain of Space partition " + partitionName);
        } else if (alertStatus.isResolved()) {
            factory.description("Resolved split-brain of Space partition " + partitionName);
        } else if (alertStatus.isNotAvailable()) {
            factory.description("Space " + spaceName + " is not available");
        }

        factory.status(alertStatus);

        factory.putProperty(SpacePartitionSplitBrainAlert.SPACE_NAME, spaceName);
        factory.putProperty(SpacePartitionSplitBrainAlert.SPACE_PARTITION_ID, String.valueOf(spaceInstance.getPartition().getPartitionId()));

        Alert alert = factory.toAlert();

        Alert[] alertsByGroupUid = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertsByGroupUid(groupUid);
        boolean alertOpen = alertsByGroupUid.length != 0 && !alertsByGroupUid[0].getStatus().isResolved();

        boolean triggerAlert = false;
        if (alertStatus.isRaised()) {
            triggerAlert = !alertOpen; //trigger alert if raised and not already open
        } else {
            triggerAlert = alertOpen; //trigger alert if resolved/NA and already open
        }

        if (triggerAlert) {
            admin.getAlertManager().triggerAlert( new SpacePartitionSplitBrainAlert(alert));
        }

    }

    private String getLocation(SpaceInstance spaceInstance) {
        String location = "";
        SpaceInstance[] instances = spaceInstance.getPartition().getInstances();
        int i=0;
        for (SpaceInstance instance : instances) {
            location += AlertBeanUtils.getSpaceInstanceDescription(instance);
            i++;
            if (i < instances.length) {
                location += ", ";
            }
        }
        return location;
    }

    private String generateGroupUid(String uid) {
        return beanUID.concat("-").concat(uid);
    }

}
