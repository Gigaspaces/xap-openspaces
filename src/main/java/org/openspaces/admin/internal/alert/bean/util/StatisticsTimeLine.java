package org.openspaces.admin.internal.alert.bean.util;

import java.util.Arrays;

/**
 * A cyclic array holding statistics (double) values. The array is filled at the cyclicIndex, incremented
 * on each insertion modulo the size of the array. This ensures that new values replace the old ones.
 *
 * Created by moran on 3/3/15.
 * @since 10.1.0
 */
public class StatisticsTimeLine {
    private int cyclicIndex = 0;
    private double[] values;
    private boolean available;

    /**
     * @param length how many statistics samples to keep
     */
    public StatisticsTimeLine(int length) {
        values = new double[length];
    }

    /**
     * @return add value to statistics time-line and get the previous value
     */
    public double addAndGet(double value) {
        double prev = values[cyclicIndex];
        values[cyclicIndex] = value;
        cyclicIndex = (cyclicIndex+1)%values.length;
        if (cyclicIndex == 0) {
            available = true;
        }
        return prev;
    }

    /**
     * @return the values in the time-line. May contain obsolete values if {@link #isAvailable()} returns false.
     */
    public double[] getTimeLine() {
        return values;
    }

    /**
     * @return <code>true</code> if time-line has enough sampled statistics; otherwise <code>false</code>
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * restarts the time-line measurement. Will become unavailable until time-line has enough samples.
     */
    public void restartTimeLine() {
        available = false;
        cyclicIndex = 0;
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}
