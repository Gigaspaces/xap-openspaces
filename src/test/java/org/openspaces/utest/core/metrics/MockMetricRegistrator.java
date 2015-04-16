package org.openspaces.utest.core.metrics;

import com.gigaspaces.metrics.DummyMetricRegistrator;
import com.gigaspaces.metrics.Metric;

import java.util.HashMap;
import java.util.Map;

public class MockMetricRegistrator extends DummyMetricRegistrator {
    public final Map<String, Metric> metrics = new HashMap<String, Metric>();

    @Override
    public void register(String name, Metric metric) {
        metrics.put(name, metric);
    }
}
