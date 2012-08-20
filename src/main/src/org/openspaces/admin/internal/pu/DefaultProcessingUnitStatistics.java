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
package org.openspaces.admin.internal.pu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.internal.pu.statistics.InstancesStatisticsCalculator;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatistics;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatisticsCalculator;
import org.openspaces.admin.internal.pu.statistics.StatisticsObjectListFunction;
import org.openspaces.admin.internal.pu.statistics.TimeWindowStatisticsCalculator;
import org.openspaces.admin.internal.pu.statistics.ZoneStatisticsCalculator;
import org.openspaces.admin.pu.statistics.InstancesStatisticsConfig;
import org.openspaces.admin.pu.statistics.ProcessingUnitStatisticsId;
import org.openspaces.admin.pu.statistics.SingleInstanceStatisticsConfig;
import org.openspaces.admin.zone.config.ExactZonesConfig;
import org.openspaces.admin.zone.config.ZonesConfig;

public class DefaultProcessingUnitStatistics implements InternalProcessingUnitStatistics {

    private volatile ProcessingUnitStatistics previous;

    private final long adminTimestamp;
    
    private final Map<ProcessingUnitStatisticsId, Object> statistics;

    private final InternalProcessingUnitStatisticsCalculator timeWindowStatisticsCalculator = new TimeWindowStatisticsCalculator();
    private final InternalProcessingUnitStatisticsCalculator instancesStatisticsCalculator = new InstancesStatisticsCalculator();
    private final InternalProcessingUnitStatisticsCalculator zoneStatisticsCalculator = new ZoneStatisticsCalculator();

    private Log logger = LogFactory.getLog(this.getClass());

    public DefaultProcessingUnitStatistics(
            long adminTimestamp, 
            ProcessingUnitStatistics lastStatistics,
            int historySize) {
    
        this.statistics = new HashMap<ProcessingUnitStatisticsId, Object>();
        this.adminTimestamp = adminTimestamp;
        this.previous = lastStatistics;
       
        if (lastStatistics != null) {
            for (int i = 0; i < historySize; i++) {
                if (lastStatistics.getPrevious() == null) {
                    break;
                }
                lastStatistics = lastStatistics.getPrevious();
            }
            ((DefaultProcessingUnitStatistics)lastStatistics).previous = null;
        }
        
    }
    
    @Override
    public long getAdminTimestamp() {
        return this.adminTimestamp;
    }
    
    @Override
    public ProcessingUnitStatistics getPrevious() {
        return this.previous;
    }

    @Override
    public Map<ProcessingUnitStatisticsId, Object> getStatistics() {
        return Collections.unmodifiableMap(statistics);
    }

    @Override
    public void addStatistics(ProcessingUnitStatisticsId statisticsId, Object statisticsValue) {
        statisticsId.validate();
        statistics.put(statisticsId, statisticsValue);
        
    }

    @Override
    public void calculateStatistics(Iterable<ProcessingUnitStatisticsId> statisticsIds) {
        
        calculateTimeWindowStatistics(statisticsIds);
        calculateZoneStatistics(statisticsIds);
        calculateInstancesStatistics(statisticsIds);
    }

    private void calculateInstancesStatistics(Iterable<ProcessingUnitStatisticsId> statisticsIds) {
        
        final List<ProcessingUnitStatisticsId> instancesCalculatedStatistics = new ArrayList<ProcessingUnitStatisticsId>();
        for (final ProcessingUnitStatisticsId statisticsId : statisticsIds) {
            if (statisticsId.getInstancesStatistics() instanceof StatisticsObjectListFunction) {
                instancesCalculatedStatistics.add(statisticsId);
            }
        }
    
        instancesStatisticsCalculator.calculateNewStatistics(this, instancesCalculatedStatistics);
    }
    
    private void calculateZoneStatistics(Iterable<ProcessingUnitStatisticsId> statisticIds) {
        
        List<ProcessingUnitStatisticsId> zoneCalculatedStatistics = new ArrayList<ProcessingUnitStatisticsId>();
        for (final ProcessingUnitStatisticsId statisticsId : statisticIds) {
            zoneCalculatedStatistics.add(statisticsId);
        }
        zoneStatisticsCalculator.calculateNewStatistics(this, zoneCalculatedStatistics);
        
    }
    private void calculateTimeWindowStatistics(Iterable<ProcessingUnitStatisticsId> statisticsIdsToCalculate) {
        
        Map<SingleInstanceStatisticsConfig,ExactZonesConfig> instances = new HashMap<SingleInstanceStatisticsConfig, ExactZonesConfig>();
        
        // construct a set containing all instances UIDS for the current processing unit
        for (ProcessingUnitStatisticsId processingUnitStatisticsId : statistics.keySet()) {
            InstancesStatisticsConfig instancesStatistics = processingUnitStatisticsId.getInstancesStatistics();
            ZonesConfig zoneStatistics = processingUnitStatisticsId.getAgentZones();
            if (instancesStatistics instanceof SingleInstanceStatisticsConfig &&
                zoneStatistics instanceof ExactZonesConfig) {
                instances.put(
                        (SingleInstanceStatisticsConfig) instancesStatistics,
                        (ExactZonesConfig) processingUnitStatisticsId.getAgentZones());
            } 
        }
        
        final List<ProcessingUnitStatisticsId> singleInstanceCalculatedStatistics = new ArrayList<ProcessingUnitStatisticsId>();

        // iterate over every existing processingUnitStatisticsId to create requests that have matching zone statistics id's. 
        for (final ProcessingUnitStatisticsId statisticsId : statisticsIdsToCalculate) {
            if (statisticsId.getInstancesStatistics() instanceof SingleInstanceStatisticsConfig) {
                // instance UID is already specified. Just check that it is still discovered and has correct zones
                SingleInstanceStatisticsConfig instancesStatistics = (SingleInstanceStatisticsConfig)(statisticsId.getInstancesStatistics());
                if (!instances.containsKey(instancesStatistics)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Failed to find instance UID " + instancesStatistics.getInstanceUid());
                    }
                    continue;
                }
                
                ExactZonesConfig zoneStatistics = instances.get(instancesStatistics);
                if (!statisticsId.getAgentZones().isSatisfiedBy(zoneStatistics)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Failed to find instance UID " + instancesStatistics.getInstanceUid() + " with zones " + zoneStatistics.getZones() + " which satisfies zones " + statisticsId.getAgentZones());
                    }
                    continue;
                }
                
                // fix zone statistics for timewindow calculator so it finds the instance
                ProcessingUnitStatisticsId fixedStatisticsId = statisticsId.shallowClone();
                fixedStatisticsId.setAgentZones(zoneStatistics);
                singleInstanceCalculatedStatistics.add(fixedStatisticsId);
            
            }
            else {
                //expand to all instance UIDs
                for (Entry<SingleInstanceStatisticsConfig, ExactZonesConfig>  pair : instances.entrySet()) {
                    ProcessingUnitStatisticsId fixedStatisticsId = statisticsId.shallowClone();
                    fixedStatisticsId.setInstancesStatistics(pair.getKey());
                    fixedStatisticsId.setAgentZones(pair.getValue());
                    singleInstanceCalculatedStatistics.add(fixedStatisticsId);
                }
            }
        }
        
        
        timeWindowStatisticsCalculator.calculateNewStatistics(this, singleInstanceCalculatedStatistics);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ProcessingUnitStatistics {adminTimestamp=" + adminTimestamp + ", statistics=" + statistics + "}";
    }

}
