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

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Marks configurations that aggregate statistics from time samples.
 * @author itaif
 * @since 9.0.0
 * @see ProcessingUnitStatisticsId
 */
public interface TimeWindowStatisticsConfig {
    
    Map<String,String> getProperties();

    /**
     * Checks the content of this config is valid.
     * @throws IllegalStateException - if state is found to be illegal
     */
    void validate() throws IllegalStateException;

    /**
     * @return the maximum number of samples required given the specified interval between consecutive samples.
     */
    int getMaxNumberOfSamples(long statisticsPollingInterval, TimeUnit timeUnit);

}
