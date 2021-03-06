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
package org.openspaces.admin.internal.alert.bean;

import java.text.NumberFormat;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jini.rio.resources.util.TimeUtil;
import org.openspaces.admin.Admin;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.alert.alerts.PhysicalMemoryUtilizationAlert;
import org.openspaces.admin.alert.config.PhysicalMemoryUtilizationAlertConfiguration;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.internal.alert.InternalAlertManager;
import org.openspaces.admin.internal.alert.bean.util.AlertBeanUtils;
import org.openspaces.admin.internal.alert.bean.util.MovingAverageStatistics;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.events.MachineRemovedEventListener;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEvent;
import org.openspaces.admin.os.events.OperatingSystemStatisticsChangedEventListener;

public class PhysicalMemoryUtilizationAlertBean implements AlertBean,
        OperatingSystemStatisticsChangedEventListener, MachineRemovedEventListener {

    private static final Logger logger = Logger.getLogger(PhysicalMemoryUtilizationAlert.class.getName());

    public static final String beanUID = "726a2752-4cae5258-f281-49d3-96b6-1e68e42bbd2c";
    public static final String ALERT_NAME = "Physical Memory Utilization";

    private final PhysicalMemoryUtilizationAlertConfiguration config = new PhysicalMemoryUtilizationAlertConfiguration();

    private Admin admin;
    private final static NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    private MovingAverageStatistics movingAverageStatistics;

    public PhysicalMemoryUtilizationAlertBean() {
        NUMBER_FORMAT.setMinimumFractionDigits(1);
        NUMBER_FORMAT.setMaximumFractionDigits(2);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        validateProperties();

        long measurementPeriod = config.getMeasurementPeriod();
        int period = (int) (measurementPeriod / StatisticsMonitor.DEFAULT_MONITOR_INTERVAL);
        movingAverageStatistics = new MovingAverageStatistics(period);

        admin.getMachines().getMachineRemoved().add(this);
        admin.getOperatingSystems().getOperatingSystemStatisticsChanged().add(this);
        admin.getOperatingSystems().startStatisticsMonitor();
    }

    @Override
    public void destroy() throws Exception {
        movingAverageStatistics.clear();
        admin.getMachines().getMachineRemoved().remove(this);
        admin.getOperatingSystems().getOperatingSystemStatisticsChanged().remove(this);
        admin.getOperatingSystems().stopStatisticsMonitor();
    }

    @Override
    public Map<String, String> getProperties() {
        return config.getProperties();
    }

    @Override
    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        config.setProperties(properties);
    }

    private void validateProperties() {
        
        if (config.getHighThresholdPerc() == null) {
            throw new BeanConfigurationException("High threshold property is null");
        }
        
        if (config.getLowThresholdPerc() == null) {
            throw new BeanConfigurationException("Low threshold property is null");
        }
        
        if (config.getMeasurementPeriod() == null) {
            throw new BeanConfigurationException("Measurement period property is null");
        }

        if (config.getHighThresholdPerc() < config.getLowThresholdPerc()) {
            throw new BeanConfigurationException("Low threshold [" + config.getLowThresholdPerc()
                    + "%] must be less than high threshold value [" + config.getHighThresholdPerc() + "%]");
        }

        if (config.getHighThresholdPerc() < 0) {
            throw new BeanConfigurationException("High threshold [" + config.getHighThresholdPerc()
                    + "%] must greater than zero");
        }

        if (config.getLowThresholdPerc() < 0) {
            throw new BeanConfigurationException("Low threshold [" + config.getLowThresholdPerc()
                    + "%] must greater or equal to zero");
        }

        if (config.getMeasurementPeriod() < StatisticsMonitor.DEFAULT_MONITOR_INTERVAL) {
            throw new BeanConfigurationException("Measurement period [" + config.getMeasurementPeriod()
                    + " ms] must be greater than ["+StatisticsMonitor.DEFAULT_MONITOR_INTERVAL+" ms]");
        }
    }
    

        //unreachable machine
    @Override
    public void machineRemoved(final Machine machine) {

        movingAverageStatistics.clear(machine.getOperatingSystem().getUid());

        final String groupUid = generateGroupUid(machine.getOperatingSystem().getUid());
        Alert[] alertsByGroupUid = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertsByGroupUid(groupUid);
        if (alertsByGroupUid.length != 0 && !alertsByGroupUid[0].getStatus().isResolved()) {
            AlertFactory factory = new AlertFactory();
            factory.name(ALERT_NAME);
            factory.groupUid(groupUid);
            factory.description("Memory measurement is unavailable; machine has been removed");
            factory.severity(AlertSeverity.WARNING);
            factory.status(AlertStatus.NA);
            factory.componentUid(machine.getOperatingSystem().getUid());
            factory.componentDescription(AlertBeanUtils.getMachineDescription(machine));
            factory.config(config.getProperties());

            Alert alert = factory.toAlert();
            admin.getAlertManager().triggerAlert( new PhysicalMemoryUtilizationAlert(alert));
        }
    }

    @Override
    public void operatingSystemStatisticsChanged(OperatingSystemStatisticsChangedEvent event) {

        int highThreshold = config.getHighThresholdPerc();
        int lowThreshold = config.getLowThresholdPerc();
        
        double memoryAvg = calcAverageWithinPeriod(event);
        if (memoryAvg < 0) return; //period hasn't passed

        if (memoryAvg > highThreshold) {
            final String groupUid = generateGroupUid(event.getOperatingSystem().getUid());
            AlertFactory factory = new AlertFactory();
            factory.name(ALERT_NAME);
            factory.groupUid(groupUid);
            factory.description("Memory crossed above a " + highThreshold + "% threshold, for a period of "
                    + TimeUtil.format(config.getMeasurementPeriod()) + ", with an average memory of " + NUMBER_FORMAT.format(memoryAvg) + "%");
            factory.severity(AlertSeverity.WARNING);
            factory.status(AlertStatus.RAISED);
            factory.componentUid(event.getOperatingSystem().getUid());
            factory.componentDescription(AlertBeanUtils.getMachineDescription(event.getStatistics().getDetails()));
            factory.config(config.getProperties());
            
            factory.putProperty(PhysicalMemoryUtilizationAlert.HOST_ADDRESS, event.getStatistics().getDetails().getHostAddress());
            factory.putProperty(PhysicalMemoryUtilizationAlert.HOST_NAME, event.getStatistics().getDetails().getHostName());
            factory.putProperty(PhysicalMemoryUtilizationAlert.CPU_UTILIZATION, String.valueOf(event.getStatistics().getCpuPerc()*100.0));
            factory.putProperty(PhysicalMemoryUtilizationAlert.MEMORY_UTILIZATION, String.valueOf(event.getStatistics().getPhysicalMemoryUsedPerc()));

            Alert alert = factory.toAlert();
            admin.getAlertManager().triggerAlert( new PhysicalMemoryUtilizationAlert(alert));
                
        } else if (memoryAvg < lowThreshold) {
            final String groupUid = generateGroupUid(event.getOperatingSystem().getUid());
            Alert[] alertsByGroupUid = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertsByGroupUid(groupUid);
            if (alertsByGroupUid.length != 0 && !alertsByGroupUid[0].getStatus().isResolved()) {
                AlertFactory factory = new AlertFactory();
                factory.name(ALERT_NAME);
                factory.groupUid(groupUid);
                factory.description("Memory crossed below a " + lowThreshold + "% threshold, for a period of "
                        + getPeriodOfTime(event) + ", with an average memory of " + NUMBER_FORMAT.format(memoryAvg) + "%");
                factory.severity(AlertSeverity.WARNING);
                factory.status(AlertStatus.RESOLVED);
                factory.componentUid(event.getOperatingSystem().getUid());
                factory.componentDescription(AlertBeanUtils.getMachineDescription(event.getStatistics().getDetails()));
                factory.config(config.getProperties());
                
                factory.putProperty(PhysicalMemoryUtilizationAlert.HOST_ADDRESS, event.getStatistics().getDetails().getHostAddress());
                factory.putProperty(PhysicalMemoryUtilizationAlert.HOST_NAME, event.getStatistics().getDetails().getHostName());
                factory.putProperty(PhysicalMemoryUtilizationAlert.CPU_UTILIZATION, String.valueOf(event.getStatistics().getCpuPerc()*100.0));
                factory.putProperty(PhysicalMemoryUtilizationAlert.MEMORY_UTILIZATION, String.valueOf(event.getStatistics().getPhysicalMemoryUsedPerc()));

                Alert alert = factory.toAlert();
                admin.getAlertManager().triggerAlert( new PhysicalMemoryUtilizationAlert(alert));
            }
        }
    }

    private String generateGroupUid(String uid) {
        return beanUID.concat("-").concat(uid);
    }

    private double calcAverageWithinPeriod(OperatingSystemStatisticsChangedEvent event) {
        if (event.getStatistics().isNA()) {
            return -1;
        }

        final String key = event.getOperatingSystem().getUid();
        movingAverageStatistics.addStatistics(key, event.getStatistics().getPhysicalMemoryUsedPerc());
        double average = movingAverageStatistics.getAverageAndReset(key);

        if (average != -1 && logger.isLoggable(Level.FINE)) {
            logger.fine("host=[" + event.getOperatingSystem().getDetails().getHostName()
                    + "] memory used=[" + event.getStatistics().getPhysicalMemoryUsedPerc() + "%]" +
                    " average=[" + average + "%] values: " + movingAverageStatistics.toString(key));
        }

        return average;
    }

    private String getPeriodOfTime(OperatingSystemStatisticsChangedEvent event) {
        return TimeUtil.format(config.getMeasurementPeriod());
    }
}
