package org.openspaces.admin.pu.statistics;

import org.openspaces.admin.internal.pu.statistics.StatisticsObjectListFunction;
import org.openspaces.admin.internal.pu.statistics.StatisticsObjectList;


public class AverageTimeWindowStatisticsConfig 
            extends AbstractTimeWindowStatisticsConfig 
            implements StatisticsObjectListFunction {

    @Override
    public String toString() {
        return "averageTimeWindowStatistics {timeWindowSeconds="+getTimeWindowSeconds() + ", minimumTimeWindowSeconds="+getMinimumTimeWindowSeconds() + ", maximumTimeWindowSeconds="+ getMaximumTimeWindowSeconds()+"}";
    }

    /* (non-Javadoc)
     * @see org.openspaces.admin.internal.pu.statistics.InternalTimeWindowStatisticsConfig#getValue(org.openspaces.admin.internal.pu.statistics.StatisticsObjectList)
     */
    @Override
    public Object calc(StatisticsObjectList values) {
        return values.getAverage();
    }
}
