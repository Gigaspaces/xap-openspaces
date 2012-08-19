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
package org.openspaces.admin.pu.statistics;

import java.util.HashMap;
import java.util.Map;

/**
 * @author elip
 *
 */
public class ExactZonesStatisticsConfig 
    extends AbstractZoneStatisticsConfig {

    public ExactZonesStatisticsConfig(Map<String, String> properties) {
        super(properties);
        // TODO Auto-generated constructor stub
    }
    
    public ExactZonesStatisticsConfig() {
        this(new HashMap<String,String>());
    }

    /* (non-Javadoc)
     * @see org.openspaces.admin.pu.statistics.ZoneStatisticsConfig#satisfiedBy(org.openspaces.admin.pu.statistics.ZoneStatisticsConfig)
     */
    @Override
    public boolean satisfiedBy(ZoneStatisticsConfig existingZoneStatisticsConfig) {
        return this.getZones().equals(existingZoneStatisticsConfig.getZones());
    } 
}
