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
package org.openspaces.grid.gsm.strategy;

import java.util.concurrent.TimeUnit;

import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleConfig;
import org.openspaces.admin.pu.elastic.config.AutomaticCapacityScaleRuleConfig;
import org.openspaces.admin.pu.elastic.config.CapacityRequirementConfig;
import org.openspaces.admin.pu.elastic.config.ScaleStrategyConfig;
import org.openspaces.admin.pu.statistics.TimeWindowStatisticsConfig;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaPolicy;
import org.openspaces.grid.gsm.autoscaling.AutoScalingSlaUtils;
import org.openspaces.grid.gsm.autoscaling.exceptions.AutoScalingSlaEnforcementInProgressException;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirement;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementInProgressException;

/**
+ * The business logic that scales an elastic processing unit based on the specified
+ * {@link AutomaticCapacityScaleConfig}
+ * 
+ * @author itaif
+ * @since 9.0.0
+ */
public class AutomaticCapacityScaleStrategyBean extends AbstractCapacityScaleStrategyBean
        implements AutoScalingSlaEnforcementEndpointAware {

    private AutoScalingSlaEnforcementEndpoint autoScalingEndpoint;

    @Override
    public void setAutoScalingSlaEnforcementEndpoint(AutoScalingSlaEnforcementEndpoint endpoint) {
        this.autoScalingEndpoint = endpoint;
    }
    
    // created by afterPropertiesSet()
    private AutomaticCapacityScaleConfig automaticCapacity;
    
    @Override
    public void afterPropertiesSet() {
        
        super.afterPropertiesSet();
                
        this.automaticCapacity = new AutomaticCapacityScaleConfig(super.getProperties());

        //inject initial manual scale capacity
        super.setCapacityRequirementConfig(automaticCapacity.getMinCapacity());
        super.setScaleStrategyConfig(automaticCapacity);
        
        enablePuStatistics();
    }
    
    @Override
    public void destroy() {
        disablePuStatistics();
        super.destroy();
    }
    
    @Override
    public ScaleStrategyConfig getConfig() {
        return automaticCapacity;
    }

    
    @Override
    protected void enforceSla() throws SlaEnforcementInProgressException {
        
        super.enforceCapacityRequirement();
        // no exception means that manual scale is complete. enforce auto-scaling SLA
        
        final CapacityRequirements capacityRequirements = super.getCapacityRequirementConfig().toCapacityRequirements();
        final CapacityRequirements newCapacityRequirements = enforceAutoScalingSla(capacityRequirements);
        if (!newCapacityRequirements.equals(capacityRequirements)) {
            super.setCapacityRequirementConfig(new CapacityRequirementConfig(newCapacityRequirements));
            super.enforceCapacityRequirement();
        }
        
        //TODO: Implement cooldown based on pu not intact.
        //TODO: Implement cooldown based on configuration.
        
        //TODO: Fire automatic completed event?? Is it needed?
    }

    private CapacityRequirements enforceAutoScalingSla(final CapacityRequirements capacityRequirements)
            throws AutoScalingSlaEnforcementInProgressException {
        final AutoScalingSlaPolicy sla = new AutoScalingSlaPolicy();
        sla.setCapacityRequirements(capacityRequirements);
        sla.setHighThresholdBreachedIncrease(getCapacityIncrease());
        sla.setMaxCapacity(automaticCapacity.getMaxCapacity().toCapacityRequirements());
        sla.setRules(automaticCapacity.getRules());
        if (getLogger().isTraceEnabled()) {
            getLogger().trace("AutoScalingSlaPolicy=" + sla);
        }
        autoScalingEndpoint.enforceSla(sla);
        
        final CapacityRequirements newCapacityRequirements = autoScalingEndpoint.getNewCapacityRequirements();
        return newCapacityRequirements;
    }

    private CapacityRequirements getCapacityIncrease() {
        long containerMemory = getGridServiceContainerConfig().getMaximumMemoryCapacityInMB();
        return new CapacityRequirements(new MemoryCapacityRequirement(containerMemory));
    }

    private void enablePuStatistics() {
        int maxNumberOfSamples = 1;
        for (AutomaticCapacityScaleRuleConfig rule : automaticCapacity.getRules()) {
            try {
                if (AutoScalingSlaUtils.compare(rule.getLowThreshold(),rule.getHighThreshold()) > 0) {
                    throw new BeanConfigurationException("Low threshold (" + rule.getLowThreshold() + ") cannot be higher than high threshold (" + rule.getHighThreshold() +")");
                }
            } catch (NumberFormatException e) {
                throw new BeanConfigurationException("Failed to compare low threshold (" + rule.getLowThreshold() + ") and high threshold (" + rule.getHighThreshold() +")",e);
            }
            
            TimeWindowStatisticsConfig timeWindowStatistics = rule.getStatistics().getTimeWindowStatistics();

            maxNumberOfSamples = 
                    Math.max(
                       maxNumberOfSamples,
                       timeWindowStatistics.getMaxNumberOfSamples(
                               automaticCapacity.getStatisticsPollingIntervalSeconds(), 
                               TimeUnit.SECONDS));
            
            getProcessingUnit().addStatisticsCalculation(rule.getStatistics());
                               
        }
        getLogger().info("Start statistics polling for " + getProcessingUnit().getName() + " to " + automaticCapacity.getStatisticsPollingIntervalSeconds() + " seconds, history size is " + maxNumberOfSamples + " samples.");
        getProcessingUnit().setStatisticsHistorySize(maxNumberOfSamples);
        getProcessingUnit().setStatisticsInterval(automaticCapacity.getStatisticsPollingIntervalSeconds(), TimeUnit.SECONDS);
        getProcessingUnit().startStatisticsMonitor();
    }

    private void disablePuStatistics() {
        getProcessingUnit().stopStatisticsMonitor();
    }
    
}
