package org.openspaces.utest.admin.alerts;

import junit.framework.TestCase;
import org.junit.Test;
import org.openspaces.admin.internal.alert.bean.util.MovingAverageStatistics;

/**
 * Created by moran on 3/4/15.
 * @since 10.1.0
 */
public class MovingAverageStatisticsTest extends TestCase {

    @Test
    public void test1() {
        MovingAverageStatistics movingAverageStatistics = new MovingAverageStatistics(12);

        //add values to fill statistics for key-10
        double[] values1 = {28.2, 30.5, 27.1, 29.5, 30.4, 32.2, 37.1, 39.44, 40.1, 50.32, 55.01, 56.7};
        for (int i=0; i<values1.length; ++i) {
            movingAverageStatistics.addStatistics("key-10", values1[i]);
            double average = movingAverageStatistics.getAverage("key-10");
            if (i+1 == values1.length) {
                assertEquals(calcAvg(values1), average);
            } else {
                assertEquals(-1.0, average);
            }
        }

        //add values to fill statistics for key-20
        double[] values2 = {18.1, 10.5, 17.2, 19.5, 13.4, 12.4, 17.2, 19.14, 11.4, 12.2, 18.01, 16.7};
        for (int i=0; i<values2.length; ++i) {
            movingAverageStatistics.addStatistics("key-20", values1[i]);
            double average = movingAverageStatistics.getAverage("key-20");
            if (i+1 == values2.length) {
                assertEquals(calcAvg(values1), average);
            } else {
                assertEquals(-1.0, average);
            }
        }

        //verify average of key-10 is not influenced by statistics of key-20
        assertEquals(calcAvg(values1), movingAverageStatistics.getAverage("key-10"));
    }

    @Test
    public void test2() {

        MovingAverageStatistics movingAverageStatistics = new MovingAverageStatistics(6);

        //add values to fill statistics for key-30
        double[] values1 = {11.1, 20.5, 12.2, 11.5, 16.4, 10.4};
        for (int i=0; i<values1.length; ++i) {
            movingAverageStatistics.addStatistics("key-30", values1[i]);
            double average = movingAverageStatistics.getAverageAndReset("key-30");
            if (i+1 == values1.length) {
                assertEquals(calcAvg(values1), average);
            } else {
                assertEquals(-1.0, average);
            }
        }

        double[] values2 = {18.2, 17.14, 13.4, 9.2, 21.01, 13.7};
        for (int i=0; i<values2.length; ++i) {
            movingAverageStatistics.addStatistics("key-30", values2[i]);
            double average = movingAverageStatistics.getAverageAndReset("key-30");
            if (i+1 == values2.length) {
                assertEquals(calcAvg(values2), average);
            } else {
                assertEquals(-1.0, average);
            }
        }

    }

    private double calcAvg(double[] values) {
        double sum = 0;
        for (int i=0; i<values.length; i++) {
            sum += values[i];
        }
        return sum / values.length;
    }
}
