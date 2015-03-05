package org.openspaces.utest.admin.alerts;

import junit.framework.TestCase;
import org.junit.Test;
import org.openspaces.admin.internal.alert.bean.util.StatisticsTimeLine;

/**
 * Created by moran on 3/4/15.
 * @since 10.1.0
 */
public class StatisticsTimeLineTest extends TestCase {

    @Test
    public void test1() {
        StatisticsTimeLine statisticsTimeLine = new StatisticsTimeLine(10);

        //add values to fill statistics
        double[] values1 = {11.2, 14.5, 16.1, 10.1, 9.4, 2.34, 6.54, 8.64, 21.2, 41.2};
        for (int i=0; i<values1.length; ++i) {
            double prev = statisticsTimeLine.addAndGet(values1[i]);
            assertEquals(0.0, prev);
            if (i+1 == values1.length) {
                assertTrue(statisticsTimeLine.isAvailable());
            } else {
                assertFalse(statisticsTimeLine.isAvailable());
            }
        }

        //add more values to fill in a cyclic manner, replacing the oldest each time
        //use a partial length array - e.g. < values1.length
        //expected [19.2, 11.5, 2.1, 5.7, 1.3, 6.44, *6.54*, *8.64*, *21.2*, *41.2*] - not replaced marked with *
        double[] values2 = {19.2, 11.5, 2.1, 5.7, 1.3, 6.44};
        for (int i=0; i<values2.length; ++i) {
            double prev = statisticsTimeLine.addAndGet(values2[i]);
            assertEquals(values1[i], prev);
            assertTrue(statisticsTimeLine.isAvailable());
        }

        //check expected values stored in time-line
        double[] expected = {19.2, 11.5, 2.1, 5.7, 1.3, 6.44, 6.54, 8.64, 21.2, 41.2};
        double[] actual = statisticsTimeLine.getTimeLine();
        for (int i=0; i<values1.length; ++i) {
            assertEquals(expected[i], actual[i]);
        }

        //verify that the cyclic insertion replaced the oldest
        //expected [*19.2*, *11.5*, *2.1*, *5.7*, *1.3*, *6.44*, 0.0, 0.0, 0.0, 0.0] - not replaced marked with *
        for (int i=values2.length; i<values1.length; ++i) {
            double prev = statisticsTimeLine.addAndGet(0.0);
            assertEquals(values1[i], prev);
            assertTrue(statisticsTimeLine.isAvailable());
        }

        //expected [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, *0.0*, *0.0*, *0.0*, *0.0*] - not replaced marked with *
        for (int i=0; i<values2.length; ++i) {
            double prev = statisticsTimeLine.addAndGet(0.0);
            assertEquals(values2[i], prev);
            assertTrue(statisticsTimeLine.isAvailable());
        }

    }

    @Test
    public void test2() {
        StatisticsTimeLine statisticsTimeLine = new StatisticsTimeLine(10);

        //add values to fill statistics
        double[] values1 = {11.2, 14.5, 16.1, 10.1, 9.4, 2.34, 6.54, 8.64, 21.2, 41.2};
        double[] values2 = {19.2, 11.5, 2.1, 5.7, 1.3, 6.44, 9.11, 2.3, 67.2, 19.2};
        for (int i=0; i<values1.length; ++i) {
            statisticsTimeLine.addAndGet(values1[i]);
            if (i+1 == values1.length) {
                assertTrue(statisticsTimeLine.isAvailable());
            } else {
                assertFalse(statisticsTimeLine.isAvailable());
            }
        }

        double[] timeLine1 = statisticsTimeLine.getTimeLine();
        for (int i=0; i<values1.length; ++i) {
            assertEquals(values1[i], timeLine1[i]);
        }

        statisticsTimeLine.restartTimeLine();
        assertFalse(statisticsTimeLine.isAvailable());

        for (int i=0; i<values2.length; ++i) {
            statisticsTimeLine.addAndGet(values2[i]);
            if (i+1 == values2.length) {
                assertTrue(statisticsTimeLine.isAvailable());
            } else {
                assertFalse(statisticsTimeLine.isAvailable());
            }
        }

        double[] timeLine2 = statisticsTimeLine.getTimeLine();
        for (int i=0; i<values2.length; ++i) {
            assertEquals(values2[i], timeLine2[i]);
        }

        statisticsTimeLine.restartTimeLine();
        assertFalse(statisticsTimeLine.isAvailable());
    }
}
