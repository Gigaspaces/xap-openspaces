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
package org.openspaces.admin.alert.config;

/**
 * A Space partition split-brain alert configurer.
 * An alert is raised if there are two primaries detected in a partition.
 * An alert is resolved after split-brain recovery.
 * <p>
 * Use the call to {@link #create()} to create a fully initialized
 * {@link SpacePartitionSplitBrainAlertConfiguration} configuration.
 * 
 * @see SpacePartitionSplitBrainAlertConfiguration
 * 
 * @author Moran Avigdor
 * @since 10.2.0
 */
public class SpacePartitionSplitBrainAlertConfigurer implements AlertConfigurer {

    private final SpacePartitionSplitBrainAlertConfiguration config = new SpacePartitionSplitBrainAlertConfiguration();

    /**
     * Constructs an empty alert configuration.
     */
    public SpacePartitionSplitBrainAlertConfigurer() {
    }
    
    /*
     * (non-Javadoc)
     * @see org.openspaces.admin.alert.config.AlertConfigurer#enable(boolean)
     */
    @Override
    public SpacePartitionSplitBrainAlertConfigurer enable(boolean enabled) {
        config.setEnabled(enabled);
        return this;
    }
    
    /**
     * Get a fully configured space partition split-brain alert configuration (after all properties have been set).
     * @return a fully configured alert configuration.
     */
    @Override
    public AlertConfiguration create() {
        return config;
    }

}
