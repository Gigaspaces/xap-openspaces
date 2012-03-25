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
package org.openspaces.admin.pu.elastic.config;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.internal.pu.elastic.ScaleStrategyConfigUtils;
import org.openspaces.core.util.StringProperties;
import org.openspaces.grid.gsm.strategy.ManualCapacityScaleStrategyBean;

/**
 * Defines a manual scaling strategy that consumes the specified memory capacity.
 * When enabled the processing unit scales out whenever the specified memory capacity deviates from the actual memory capacity.
 * 
 * When a backup partition is enabled (which usually is the case), the specified memory capacity is the total memory occupied by the
 * primary partition instances and the backup partition instances.
 *  
 * @see ManualCapacityScaleConfigurer
 * @since 8.0
 * @author itaif
 */
public class ManualCapacityScaleConfig 
    implements ScaleStrategyConfig , ScaleStrategyCapacityRequirementConfig, Externalizable {

    private static final long serialVersionUID = 1L;

    private static final String MEMORY_CAPACITY_MEGABYTES_KEY = "memory-capacity-megabytes";
    private static final int MEMORY_CAPACITY_MEGABYTES_DEFAULT = 0;
    private static final String CPU_CAPACITY_CORES_KEY = "cpu-capacity-cores";
    private static final double CPU_CAPACITY_CORES_DEFAULT = 0.0;
    private static final String DRIVE_CAPACITY_MEGABYTES_KEY = "drive-capacity-megabytes";
    private static final String DRIVE_CAPACITY_MEGABYTES_PAIR_SEPERATOR = ",";
    private static final String DRIVE_CAPACITY_MEGABYTES_KEYVALUE_SEPERATOR = "=";
    private static final Map<String,String> DRIVE_CAPACITY_MEGABYTES_DEFAULT = new HashMap<String,String>();
    
    private StringProperties properties;
    
    /**
     * Default constructor
     */
    public ManualCapacityScaleConfig() {
        this.properties = new StringProperties();
    }
    
    public ManualCapacityScaleConfig(Map<String,String> properties) {
        this.properties = new StringProperties(properties);
    }

    @Override
    public void setMemoryCapacityInMB(long memory) {
        properties.putLong(MEMORY_CAPACITY_MEGABYTES_KEY, memory);
    }

    @Override
    public long getMemoryCapacityInMB() throws NumberFormatException {
        return properties.getLong(MEMORY_CAPACITY_MEGABYTES_KEY, MEMORY_CAPACITY_MEGABYTES_DEFAULT);
    }
    
    @Override
    public void setNumberOfCpuCores(double cpuCores) {
        properties.putDouble(CPU_CAPACITY_CORES_KEY, cpuCores);
    }

    @Override
    public double getNumberOfCpuCores() {
        return properties.getDouble(CPU_CAPACITY_CORES_KEY, CPU_CAPACITY_CORES_DEFAULT);
    }

    @Override
    public void setPollingIntervalSeconds(int seconds) {
        ScaleStrategyConfigUtils.setPollingIntervalSeconds(properties,seconds);
    }
    
    @Override
    public int getPollingIntervalSeconds() {
        return ScaleStrategyConfigUtils.getPollingIntervalSeconds(properties);
    }

    @Override
    public int getMaxConcurrentRelocationsPerMachine() {
        return ScaleStrategyConfigUtils.getMaxConcurrentRelocationsPerMachine(properties);
    }
    
    @Override
    public void setMaxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        ScaleStrategyConfigUtils.setMaxConcurrentRelocationsPerMachine(properties, maxNumberOfConcurrentRelocationsPerMachine);
    }

    /*
     * @see ManualCapacityScaleConfig#isAtMostOneContainerPerMachine()
     */
    @Deprecated
    public boolean isAtMostOneContainersPerMachine() {
        return isAtMostOneContainerPerMachine();
    }
    
    @Override
    public boolean isAtMostOneContainerPerMachine() {
        return ScaleStrategyConfigUtils.isSingleContainerPerMachine(properties);
    }

    @Override
    public void setAtMostOneContainerPerMachine(boolean atMostOneContainerPerMachine) {
        ScaleStrategyConfigUtils.setAtMostOneContainerPerMachine(properties, atMostOneContainerPerMachine);
    }

    @Override
    public Map<String,String> getProperties() {
        return properties.getProperties();
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
    }

    @Override
    public String getBeanClassName() {
        return ManualCapacityScaleStrategyBean.class.getName();
    }
   
    @Override
    public String toString() {
        return this.properties.toString();
    }

    @Override
    public void setDrivesCapacityInMB(Map<String,Long> megaBytesPerDrive) {
        Map<String,String> capacityPerDrive = new HashMap<String,String>();
        for (String drive : megaBytesPerDrive.keySet()) {
            capacityPerDrive.put(drive, megaBytesPerDrive.get(drive).toString());
        }
        properties.putKeyValuePairs(
                DRIVE_CAPACITY_MEGABYTES_KEY, 
                capacityPerDrive, 
                DRIVE_CAPACITY_MEGABYTES_PAIR_SEPERATOR, 
                DRIVE_CAPACITY_MEGABYTES_KEYVALUE_SEPERATOR);
    }
    
    @Override
    public Map<String,Long> getDrivesCapacityInMB() throws NumberFormatException{
        Map<String, String> capacityPerDrive = properties.getKeyValuePairs(
                DRIVE_CAPACITY_MEGABYTES_KEY, 
                DRIVE_CAPACITY_MEGABYTES_PAIR_SEPERATOR, 
                DRIVE_CAPACITY_MEGABYTES_KEYVALUE_SEPERATOR, 
                DRIVE_CAPACITY_MEGABYTES_DEFAULT);
        
        Map<String,Long> megaBytesPerDrive = new HashMap<String,Long>();
        for (String drive : capacityPerDrive.keySet()) {
            megaBytesPerDrive.put(drive, Long.valueOf(capacityPerDrive.get(drive)));
        }
        return megaBytesPerDrive;
    }
    
    @Override
    public boolean equals(Object other) {
        return (other instanceof ManualCapacityScaleConfig) &&
                this.properties.equals(((ManualCapacityScaleConfig)other).properties);
    }
    
    @Override
    public int hashCode() {
        return this.properties.hashCode();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(this.properties.getProperties());
        
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.properties = new StringProperties((Map<String,String>)in.readObject());
        
    }
}
