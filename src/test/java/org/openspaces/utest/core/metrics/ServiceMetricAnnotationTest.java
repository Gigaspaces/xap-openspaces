package org.openspaces.utest.core.metrics;

import com.gigaspaces.metrics.Gauge;
import com.gigaspaces.metrics.LongCounter;
import com.gigaspaces.metrics.Metric;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/utest/core/metrics/service-metric-test.xml")
public class ServiceMetricAnnotationTest {
    @Autowired
    protected ApplicationContext ac;

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/utest/core/metrics/service-metric-test.xml"};
    }

    @Test
    public void testServiceMetricAnnotation() throws Exception {
        final MockMetricRegistrator metricRegistrator = (MockMetricRegistrator)ac.getBean("metricRegistrator");
        final Map<String, Metric> registeredMetrics = metricRegistrator.metrics;

        Assert.assertEquals(2, registeredMetrics.size());
        Gauge fooMetric = (Gauge) registeredMetrics.get("foo");
        LongCounter barMetric = (LongCounter) registeredMetrics.get("bar");

        Assert.assertEquals(0, fooMetric.getValue());
        Assert.assertEquals(0, barMetric.getCount());

        final MetricsBean metricsBean = (MetricsBean)ac.getBean("myBean");
        metricsBean.setFoo(7);
        metricsBean.getBar().inc();
        Assert.assertEquals(7, fooMetric.getValue());
        Assert.assertEquals(1, barMetric.getCount());
    }
}
