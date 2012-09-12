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
package org.openspaces.admin.pu.elastic.topology;

import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleConfigurer;

/**
 * @author itaif
 * @since 9.0.0
 */
public interface AutomaticCapacityScaleTopology {

    /**
     * Enables the specified scale strategy, and disables all other scale strategies.
     * Automatic scale, starts and stops containers to maintain monitored statistics
     * between predefined thresholds.
     *  
     * Scale strategies can also be reconfigured after deployment.
     * @see AutomaticCapacityScaleConfig
     * @see AutomaticCapacityScaleConfigurer
     */
    AutomaticCapacityScaleTopology scale(AutomaticCapacityScaleConfig strategy);
}
