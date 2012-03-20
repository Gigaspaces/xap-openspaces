package org.openspaces.admin.pu.statistics;


/**
 * 
 * @author itaif
 * @see ProcessingUnitStatisticsIdConfigurer
 */
public class ProcessingUnitStatisticsId {

    private String monitor; 
    
    private String metric;
    
    private TimeWindowStatisticsConfig timeWindowStatistics; 
    
    private InstancesStatisticsConfig instancesStatistics; 
   
    public String getMonitor() {
        return monitor;
    }
    /**
     * @see ProcessingUnitStatisticsIdConfigurer#serviceMonitorsId(String) 
     */
    public void setMonitor(String monitor) {
        this.monitor = monitor;
    }
    
    public String getMetric() {
        return metric;
    }
    
    /**
     * @see ProcessingUnitStatisticsIdConfigurer#name(String)
     */
    public void setMetric(String metric) {
        this.metric = metric;
    }
    
    public TimeWindowStatisticsConfig getTimeWindowStatistics() {
        return timeWindowStatistics;
    }
    
    public void setTimeWindowStatistics(TimeWindowStatisticsConfig timeWindowStatistics) {
        this.timeWindowStatistics = timeWindowStatistics;
    }

    public InstancesStatisticsConfig getInstancesStatistics() {
        return instancesStatistics;
    }
    
    /**
     * @see ProcessingUnitStatisticsIdConfigurer#instancesStatistics(InstancesAggregationStatisticsConfig)
     */
    public void setInstancesStatistics(InstancesStatisticsConfig instancesStatistics) {
        this.instancesStatistics = instancesStatistics;
    }       
}
