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
package org.openspaces.admin.zone.config;

import java.util.HashSet;


/**
 * @author elip
 * @since 9.1.0
 */
public class AtLeastOneZoneStatisticsConfigurer {
    
    AtLeastOneZoneConfig config;
    
    public AtLeastOneZoneStatisticsConfigurer() {
        config = new AtLeastOneZoneConfig();
    }
    
    public AtLeastOneZoneStatisticsConfigurer zone(String zone) {
        HashSet<String> zones = new HashSet<String>();
        zones.add(zone);
        config.setZones(zones);
        return this;
    }
    
    public AtLeastOneZoneConfig create() {
        config.validate();
        return config;
    }
}
