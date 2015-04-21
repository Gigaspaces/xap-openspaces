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
package org.openspaces.admin.alert.alerts;

import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.events.AlertTriggeredEventListener;
import org.openspaces.admin.space.Space;

/**
 * A Space partition split-brain alert triggered when two primaries are detected in the same partition.
 * The alert is resolved after split-brain recovery.
 * <p>
 * This alert will be received on the call to
 * {@link AlertTriggeredEventListener#alertTriggered(Alert)} for registered listeners.
 * 
 * @author Moran Avigdor
 * @since 10.2.0
 */
public class SpacePartitionSplitBrainAlert extends AbstractAlert {

    private static final long serialVersionUID = 1L;

    public static final String SPACE_NAME = "space-name";
    public static final String SPACE_PARTITION_ID = "space-partition-id";

    /** required by java.io.Externalizable */
    public SpacePartitionSplitBrainAlert() {
    }

    public SpacePartitionSplitBrainAlert(Alert alert) {
        super(alert);
    }
    
    /**
     * {@inheritDoc}
     * The component UID is equivalent to {@link Space#getUid()}
     */
    @Override
    public String getComponentUid() {
        return super.getComponentUid();
    }

    public int getSpacePartitionId() {
        return Integer.valueOf(getProperties().get(SPACE_PARTITION_ID)).intValue();
    }

    public String getSpaceName() {
        return getProperties().get(SPACE_NAME);
    }
}
