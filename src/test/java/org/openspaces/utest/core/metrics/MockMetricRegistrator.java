package org.openspaces.utest.core.metrics;

import com.gigaspaces.metrics.DummyMetricRegistrator;
import com.gigaspaces.metrics.Metric;
import com.gigaspaces.metrics.MetricRegistrator;

import java.util.HashMap;
import java.util.Map;

public class MockMetricRegistrator extends DummyMetricRegistrator {
    public final Map<String, Metric> metrics = new HashMap<String, Metric>();
    public Map<String, MockMetricRegistrator> children = new HashMap<String, MockMetricRegistrator>();

    @Override
    public MetricRegistrator extend(String prefix) {
        children.put(prefix, new MockMetricRegistrator());
        return children.get(prefix);
    }

    @Override
    public void register(String name, Metric metric) {
        metrics.put(name, metric);
    }
}
