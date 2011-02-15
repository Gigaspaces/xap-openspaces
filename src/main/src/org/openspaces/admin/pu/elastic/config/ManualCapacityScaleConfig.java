package org.openspaces.admin.pu.elastic.config;

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
        implements ScaleStrategyConfig {

    private static final String STRATEGY_NAME = "scale-strategy.manual-memory";
    private static final String MEMORY_CAPACITY_MEGABYTES_KEY = "memory-capacity-megabytes";
    private static final int MEMORY_CAPACITY_MEGABYTES_DEFAULT = 0;
    private static final String CPU_CAPACITY_CORES_KEY = "cpu-capacity-cores";
    private static final double CPU_CAPACITY_CORES_DEFAULT = 0.0;
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

    /**
     * Specifies the total memory capacity of the processing unit.
     */
    public void setMemoryCapacityInMB(long memory) {
        properties.putLong(MEMORY_CAPACITY_MEGABYTES_KEY, memory);
    }

    public long getMemoryCapacityInMB() {
        return properties.getLong(MEMORY_CAPACITY_MEGABYTES_KEY, MEMORY_CAPACITY_MEGABYTES_DEFAULT);
    }

    /**
     * Specifies the total CPU cores for the processing unit.
     */
    public void setNumberOfCpuCores(double cpuCores) {
        properties.putDouble(CPU_CAPACITY_CORES_KEY, cpuCores);
    }

    public double getNumberOfCpuCores() {
        return properties.getDouble(CPU_CAPACITY_CORES_KEY, CPU_CAPACITY_CORES_DEFAULT);
    }

    public void setPollingIntervalSeconds(int seconds) {
        ScaleStrategyConfigUtils.setPollingIntervalSeconds(properties,seconds);
    }
    
    public int getPollingIntervalSeconds() {
        return ScaleStrategyConfigUtils.getPollingIntervalSeconds(properties);
    }

    public int getMaxConcurrentRelocationsPerMachine() {
        return ScaleStrategyConfigUtils.getMaxConcurrentRelocationsPerMachine(properties);
    }
    
    public void setMaxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        ScaleStrategyConfigUtils.setMaxConcurrentRelocationsPerMachine(properties, maxNumberOfConcurrentRelocationsPerMachine);
    }
    
    public int getReservedMemoryCapacityPerMachineInMB() {
        return ScaleStrategyConfigUtils.getReservedMemoryCapacityPerMachineInMB(properties); 
    }
    
    public void setReservedMemoryCapacityPerMachineInMB(int reservedInMB) {
        ScaleStrategyConfigUtils.setReservedMemoryCapacityPerMachineInMB(properties, reservedInMB); 
    }
    
    public boolean getDedicatedManagementMachines() {
        return ScaleStrategyConfigUtils.getDedicatedManagementMachines(properties);
    }

    public void setDedicatedManagementMachines(boolean dedicatedManagementMachines) {
        ScaleStrategyConfigUtils.setDedicatedManagementMachines(properties, dedicatedManagementMachines);
    }
    
    public Map<String,String> getProperties() {
        return properties.getProperties();
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = new StringProperties(properties);
    }

    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    public String getBeanClassName() {
        return ManualCapacityScaleStrategyBean.class.getName();
    }
   
    public String toString() {
        return this.properties.toString();
    } 
}
