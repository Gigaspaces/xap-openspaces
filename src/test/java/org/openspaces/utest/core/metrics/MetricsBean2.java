package org.openspaces.utest.core.metrics;

import com.gigaspaces.metrics.BeanMetricManager;
import com.gigaspaces.metrics.Gauge;
import com.gigaspaces.metrics.LongCounter;
import org.openspaces.pu.container.ProcessingUnitContainerContext;
import org.openspaces.pu.container.ProcessingUnitContainerContextAware;

import java.util.concurrent.atomic.AtomicInteger;

public class MetricsBean2 implements ProcessingUnitContainerContextAware {

    public final AtomicInteger foo = new AtomicInteger();
    public final LongCounter bar = new LongCounter();

    private BeanMetricManager metricsManager;

    @Override
    public void setProcessingUnitContainerContext(ProcessingUnitContainerContext processingUnitContainerContext) {
        this.metricsManager = processingUnitContainerContext.createBeanMetricManager("custom-name");
        this.metricsManager.register("foo2", new Gauge<Integer>() {
            @Override
            public Integer getValue() throws Exception {
                return foo.get();
            }
        });
        this.metricsManager.register("bar2", bar);
    }
}
