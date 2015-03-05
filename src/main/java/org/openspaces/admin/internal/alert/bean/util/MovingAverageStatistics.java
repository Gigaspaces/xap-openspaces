package org.openspaces.admin.internal.alert.bean.util;

import com.j_spaces.kernel.time.SystemTime;

import java.util.HashMap;
import java.util.Map;

/**
 * A map between a grid components uid to its statistics.
 * The statistics are used to calculate moving average.
 *
 * Created by moran on 3/3/15.
 * @since 10.1.0
 */
public class MovingAverageStatistics {
    private Map<String, StatisticsTimeLine> map = new HashMap<String, StatisticsTimeLine>();
    private int period;

    /**
     * @param period number of samples in measurement period
     */
    public MovingAverageStatistics(int period) {
        this.period = period;
    }

    /**
     * statistics values are aggregated per-key.
     */
    public void addStatistics(String key, double value) {
        StatisticsTimeLine statisticsTimeLine = map.get(key);
        if (statisticsTimeLine == null) {
            statisticsTimeLine = new StatisticsTimeLine(period);
            map.put(key, statisticsTimeLine);
        }

        statisticsTimeLine.addAndGet(value);
    }

    /**
     * get the average and reset the moving-average time-window to return a only after all samples are available again.
     * @return get the average statistics values per key; returns -1 of not all samples are available.
     */
    public double getAverageAndReset(String key) {
        return doGetAverage(key, true);
    }

    /**
     * @return get the average statistics values per key; returns -1 of not all samples are available.
     */
    public double getAverage(String key) {
        return doGetAverage(key, false);
    }

    private double doGetAverage(String key, boolean reset) {
        StatisticsTimeLine statisticsTimeLine = map.get(key);
        if (statisticsTimeLine == null) {
            return -1;
        }
        if (!statisticsTimeLine.isAvailable()) {
            return -1; //not enough samples yet
        }

        double average = calculateAverage(statisticsTimeLine.getTimeLine());
        if (reset) {
            statisticsTimeLine.restartTimeLine();
        }
        return average;
    }


    private double calculateAverage(double[] values) {
        double sum = 0.0;
        for (int i=0; i<values.length; ++i) {
            sum += values[i];
        }
        return sum / values.length;
    }

    /**
     * @param key values under this key will be removed
     */
    public void clear(String key) {
        map.remove(key);
    }

    /**
     * clear all values mapped
     */
    public void clear() {
        map.clear();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    /**
     * @return a toString of the specific key
     */
    public String toString(String key) {
        return String.valueOf(map.get(key));
    }
}
