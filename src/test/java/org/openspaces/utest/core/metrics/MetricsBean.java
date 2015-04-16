package org.openspaces.utest.core.metrics;

import com.gigaspaces.metrics.LongCounter;
import com.gigaspaces.metrics.ServiceMetric;

import java.util.concurrent.atomic.AtomicInteger;

public class MetricsBean {

    private final AtomicInteger foo = new AtomicInteger();
    private final LongCounter bar = new LongCounter();

    @ServiceMetric(name="foo")
    public Integer getFoo() {
        return foo.get();
    }

    public void setFoo(int value) {
        foo.set(value);
    }

    @ServiceMetric(name="bar")
    public LongCounter getBar() {
        return bar;
    }
}
