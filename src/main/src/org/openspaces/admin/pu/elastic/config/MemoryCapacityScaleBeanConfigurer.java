package org.openspaces.admin.pu.elastic.config;

import java.util.concurrent.TimeUnit;

/**
 * Provides fluent API for creating a new {@link MemoryCapacityScaleConfig} object.
 * 
 * For example {@code new MemoryCapacityScaleStrategyConfigurer().slidingTimeWindow(60,TimeUnit.SECONDS).scaleOutWhenAboveThreshold(70).getConfig()}
 * 
 * @author itaif
 * 
 */
public class MemoryCapacityScaleBeanConfigurer
    implements ScaleBeanConfigurer<MemoryCapacityScaleConfig> {

    private final MemoryCapacityScaleConfig config;
    
    /**
     * default constructor, creates an empty {@link MemoryCapacityScaleConfig} object
     */
    public MemoryCapacityScaleBeanConfigurer() {
        this.config = new MemoryCapacityScaleConfig();
    }
    
    /**
     * @see MemoryCapacityScaleConfig#setSlidingTimeWindowMilliseconds(int)
     */
    public MemoryCapacityScaleBeanConfigurer slidingTimeWindow(int duration, TimeUnit unit) {
        config.setSlidingTimeWindowMilliseconds(unit.toMillis(duration));
        return this;
    }

    /**
     * @see MemoryCapacityScaleConfig#setScaleInWhenAverageAbove(int)
     */
    public MemoryCapacityScaleBeanConfigurer scaleInWhenAverageBelow(int usagePercentage) {
        validatePercentage(usagePercentage);
        config.setScaleInWhenAverageBelow(usagePercentage);
        return this;
    }

    /**
     * @see MemoryCapacityScaleConfig#setScaleOutWhenAverageAbove(int)
     */
    public MemoryCapacityScaleBeanConfigurer scaleOutWhenAverageAbove(int usagePercentage) {
        validatePercentage(usagePercentage);
        config.setScaleOutWhenAverageAbove(usagePercentage);
        return this;
    }

    /**
     * @see MaxNumberOfContainersPerMachineScaleConfig#setMaxNumberOfContainersPerMachine(int)
     */
    public MemoryCapacityScaleBeanConfigurer maxNumberOfContainersPerMachine(int maxNumberOfContainersPerMachine) {
        config.setMaxNumberOfContainersPerMachine(maxNumberOfContainersPerMachine);
        return this;
    }

    /**
     * @see MinNumberOfContainersPerMachineScaleConfig#setMinNumberOfContainersPerMachine(int)
     */
    public MemoryCapacityScaleBeanConfigurer minNumberOfContainersPerMachine(int minNumberOfContainersPerMachine) {
        config.setMinNumberOfContainersPerMachine(minNumberOfContainersPerMachine);
        return this;
    }

    /**
     * @see MaxNumberOfContainersScaleConfig#setMaxNumberOfContainers(int)
     */
    public MemoryCapacityScaleBeanConfigurer maxNumberOfContainers(int maxNumberOfContainers) {
        config.setMaxNumberOfContainersPerMachine(maxNumberOfContainers);
        return this;
    }

    /**
     * @see MinNumberOfContainersScaleConfig#setMinNumberOfContainers(int)
     */
    public MemoryCapacityScaleBeanConfigurer minNumberOfContainers(int minNumberOfContainers) {
        config.setMinNumberOfContainers(minNumberOfContainers);
        return this;
    }

    /**
     * @see ScaleBeanConfigurer#getConfig()
     */
    public MemoryCapacityScaleConfig getConfig() {
        return config;
    }

    private void validatePercentage(int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("percentage must be between 0 and 100. The value " + percentage + " is illegal.");
        }
    }
}
