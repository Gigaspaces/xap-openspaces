/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.admin.alerts.config;

import java.util.concurrent.TimeUnit;

/**
 * A strongly typed high heap memory utilization alert bean configurer. Allows a more code-fluent
 * approach by use of method chaining. After all properties have been set, use the call to
 * {@link #getConfig()} to create a fully initialized configuration object based.
 * 
 * @see HeapMemoryUtilizationAlertBeanConfig
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class HeapMemoryUtilizationAlertBeanConfigurer implements AlertBeanConfigurer {

    private final HeapMemoryUtilizationAlertBeanConfig config = new HeapMemoryUtilizationAlertBeanConfig();

    /**
     * Constructs an empty heap memory utilization alert configuration.
     */
    public HeapMemoryUtilizationAlertBeanConfigurer() {
    }

    /**
     * Set the heap memory high threshold percentage value.
     * 
     * @param highThreshold
     *            high threshold percentage.
     * @return this.
     */
    public HeapMemoryUtilizationAlertBeanConfigurer highThresholdPerc(int highThreshold) {
        config.setHighThresholdPerc(highThreshold);
        return this;
    }

    /**
     * Set the heap memory low threshold percentage value.
     * 
     * @param lowThreshold
     *            low threshold percentage.
     * @return this.
     */
    public HeapMemoryUtilizationAlertBeanConfigurer lowThresholdPerc(int lowThreshold) {
        config.setLowThresholdPerc(lowThreshold);
        return this;
    }

    /**
     * Set the period of time a heap memory alert should be triggered if it's reading is above/below
     * the threshold setting.
     * 
     * @param period
     *            period of time.
     * @param timeUnit
     *            the time unit of the specified period.
     * @return this.
     */
    public HeapMemoryUtilizationAlertBeanConfigurer measurementPeriod(long period, TimeUnit timeUnit) {
        config.setMeasurementPeriod(period, timeUnit);
        return this;
    }

    /**
     * Get a fully configured heap memory utilization configuration (after all properties have been
     * set).
     * 
     * @return a fully configured alert bean configuration.
     */
    public HeapMemoryUtilizationAlertBeanConfig getConfig() {
        return config;
    }
}
