package org.openspaces.admin.pu.elastic.config;

import org.openspaces.admin.bean.BeanConfigurer;

public interface ScaleStrategyConfigurer<T extends ScaleStrategyConfig> extends BeanConfigurer<T> {

    /**
     * @see ScaleStrategyConfig#setMaxConcurrentRelocationsPerMachine(int)
     */
    ScaleStrategyConfigurer<T> maxConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine);
    
}
