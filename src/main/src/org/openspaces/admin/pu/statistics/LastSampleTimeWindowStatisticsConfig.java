package org.openspaces.admin.pu.statistics;

import org.openspaces.admin.internal.pu.statistics.DoNothingProcessingUnitStatisticsCalculator;
import org.openspaces.admin.internal.pu.statistics.InternalProcessingUnitStatisticsCalculator;
import org.openspaces.admin.internal.pu.statistics.InternalTimeWindowStatisticsConfig;

public class LastSampleTimeWindowStatisticsConfig implements InternalTimeWindowStatisticsConfig {
   
    @Override
    public Class<? extends InternalProcessingUnitStatisticsCalculator> getProcessingUnitStatisticsCalculator() {
        return DoNothingProcessingUnitStatisticsCalculator.class;
    }

    @Override
    public void validate() throws IllegalStateException {
        // ok
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "lastSampleTimeWindowStatistics";
    }

    /* Default implementation for configs without members
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }

    /* Default implementation for configs without members
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        return true;
    }

}
