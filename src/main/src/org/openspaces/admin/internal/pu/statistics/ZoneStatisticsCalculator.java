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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.openspaces.admin.zone.config.ZonesConfig;

/**
 * @author elip
 *
 */
public class ZoneStatisticsCalculator implements InternalProcessingUnitStatisticsCalculator {
    
    private final Log logger = LogFactory.getLog(this.getClass());

    /* (non-Javadoc)
     * @see org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatisticsCalculator#calculateNewStatistics(org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatistics, java.util.Collection)
     */
    @Override
    public void calculateNewStatistics(InternalProcessingUnitStatistics processingUnitStatistics,
            Collection<ProcessingUnitStatisticsId> statisticIds) {
        
        if (logger.isTraceEnabled()) {
            logger.trace("calculateNewStatistics(processingUnitStatistics="+processingUnitStatistics+" , statisticsIds="+ statisticIds);
        }
        // calculate new statistics for each request id.
        if (logger.isTraceEnabled()) {
            logger.trace("calculating ZoneStatistics in ZoneStatisticsCalculator");
        }
        for (ProcessingUnitStatisticsId processingUnitStatisticsId : statisticIds) {
            calculateNewStatistics(processingUnitStatistics, processingUnitStatisticsId);
        } 
        if (logger.isTraceEnabled()) {
            logger.trace("calculateNewStatistics(processingUnitStatistics="+processingUnitStatistics+" , statisticsIds="+ statisticIds);
        }
    } 
    
    private void calculateNewStatistics(InternalProcessingUnitStatistics processingUnitStatistics, ProcessingUnitStatisticsId processingUnitStatisticsId) {
        processingUnitStatisticsId.validate();
        //copy to avoid conc. modification exception on statistics
        Map<ProcessingUnitStatisticsId, Object> statistics = new HashMap<ProcessingUnitStatisticsId, Object>(processingUnitStatistics.getStatistics());
        // compare each request id with all current statistics entries.
        for (Map.Entry<ProcessingUnitStatisticsId, Object> entry : statistics.entrySet()) {
            calculateNewStatistics(processingUnitStatistics, entry, processingUnitStatisticsId);
        }
        
    }
    
    private void calculateNewStatistics(InternalProcessingUnitStatistics internalProcessingUnitStatistics, Map.Entry<ProcessingUnitStatisticsId, Object> statisticsEntry, ProcessingUnitStatisticsId requestedProcessingUnitStatisticsId) {
        
        ProcessingUnitStatisticsId existingProcessingUnitStatisticsId = statisticsEntry.getKey();
        Object value = statisticsEntry.getValue();
        
        // zones to compare
        ZonesConfig requestedZoneStatisticsConfig = requestedProcessingUnitStatisticsId.getAgentZones();
        if (logger.isDebugEnabled()) {
            logger.debug("requestedZoneStatisticsConfig = " + requestedZoneStatisticsConfig);
        }
        ExactZonesConfig existingZoneStatisticsConfig = (ExactZonesConfig) existingProcessingUnitStatisticsId.getAgentZones();
        if (logger.isDebugEnabled()) {
            logger.debug("existingZoneStatisticsConfig = " + existingZoneStatisticsConfig);
        }
        
        // keys without zones to compare
        ProcessingUnitStatisticsId erasedExistingProcessingUnitStatisticsId = erase(existingProcessingUnitStatisticsId);
        if (logger.isDebugEnabled()) {
            logger.debug("erasedExistingProcessingUnitStatisticsId = " + erasedExistingProcessingUnitStatisticsId);
        }
        ProcessingUnitStatisticsId erasedRequestedProcessingUnitStatisticsId = erase(requestedProcessingUnitStatisticsId);
        if (logger.isDebugEnabled()) {
            logger.debug("erasedRequestedProcessingUnitStatisticsId = " + erasedRequestedProcessingUnitStatisticsId);
        }
        
        if (requestedZoneStatisticsConfig.isSatisfiedBy(existingZoneStatisticsConfig)) {
            if (erasedRequestedProcessingUnitStatisticsId.equals(erasedExistingProcessingUnitStatisticsId)) {
                ProcessingUnitStatisticsId newProcessingUnitStatisticsId = existingProcessingUnitStatisticsId.shallowClone();
                requestedZoneStatisticsConfig.validate();
                newProcessingUnitStatisticsId.setAgentZones(requestedZoneStatisticsConfig);
                
                if (logger.isDebugEnabled()) {
                    logger.debug("adding statistics id " + newProcessingUnitStatisticsId + " with value " + value);
                }
                
                internalProcessingUnitStatistics.addStatistics(newProcessingUnitStatisticsId, value);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("requestedZoneStatisticsConfig : " + requestedZoneStatisticsConfig + " is not satisfied by existingZoneStatisticsConfig : " + existingZoneStatisticsConfig);
            }    
        }
    }
    
    private ProcessingUnitStatisticsId erase(ProcessingUnitStatisticsId processingUnitStatisticsId) {
        processingUnitStatisticsId.validate();
        ProcessingUnitStatisticsId erased = processingUnitStatisticsId.shallowClone();
        erased.setAgentZones(null);
        erased.setInstancesStatistics(null);
        return erased;
        
    }
}
