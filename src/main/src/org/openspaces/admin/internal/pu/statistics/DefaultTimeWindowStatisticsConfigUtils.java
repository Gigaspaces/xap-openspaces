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
package org.openspaces.admin.internal.pu.statistics;

import java.util.concurrent.TimeUnit;

import org.openspaces.admin.pu.statistics.AbstractTimeWindowStatisticsConfig;
import org.openspaces.admin.pu.statistics.TimeWindowStatisticsConfig;

/**
 * A helper class for Configurers that create a new {@link TimeWindowStatisticsConfig} object
 * @author itaif
 * @since 9.0.0
 */
public class DefaultTimeWindowStatisticsConfigUtils {
    
        
    public static void timeWindow(AbstractTimeWindowStatisticsConfig config, long timeWindow, TimeUnit timeUnit) {
        config.setTimeWindowSeconds(timeUnit.toSeconds(timeWindow));
    }
    
    public static void minimumTimeWindow(AbstractTimeWindowStatisticsConfig config, long timeWindow, TimeUnit timeUnit) {
        config.setMinimumTimeWindowSeconds(timeUnit.toSeconds(timeWindow));
    }
    
    public static void maximumTimeWindow(AbstractTimeWindowStatisticsConfig config, long timeWindow, TimeUnit timeUnit) {
        config.setMaximumTimeWindowSeconds(timeUnit.toSeconds(timeWindow));
    }
        
    public static void create(AbstractTimeWindowStatisticsConfig config) {
        
        if (config.getMinimumTimeWindowSeconds() == null) {
            // By default, respect the time window, and wait until enough samples exist.
            config.setMinimumTimeWindowSeconds(config.getTimeWindowSeconds());
        }
        
        if (config.getMaximumTimeWindowSeconds() == null) {
            // Assuming that time window is at least one sampling period.
            // By default, include the second sample in case it has a small jitter.
            config.setMaximumTimeWindowSeconds((long)(config.getTimeWindowSeconds()*2));
        }
        
        config.validate();
    }
}
