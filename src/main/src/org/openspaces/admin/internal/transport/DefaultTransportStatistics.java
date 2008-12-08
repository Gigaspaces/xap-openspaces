package org.openspaces.admin.internal.transport;

import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import org.openspaces.admin.support.StatisticsUtils;
import org.openspaces.admin.transport.TransportDetails;
import org.openspaces.admin.transport.TransportStatistics;

/**
 * @author kimchy
 */
public class DefaultTransportStatistics implements TransportStatistics {

    private final static NIOStatistics NA_STATS = new NIOStatistics();

    private final NIOStatistics stats;

    private final TransportStatistics previousStats;

    private final TransportDetails details;

    public DefaultTransportStatistics() {
        this(NA_STATS, null, null);
    }

    public DefaultTransportStatistics(NIOStatistics stats, TransportStatistics previousStats, TransportDetails details) {
        this.stats = stats;
        this.previousStats = previousStats;
        this.details = details;
    }

    public boolean isNA() {
        return stats.isNA();
    }

    public long getTimestamp() {
        return stats.getTimestamp();
    }

    public TransportDetails getDetails() {
        return this.details;
    }

    public long getPreviousTimestamp() {
        if (previousStats == null) {
            return -1;
        }
        return previousStats.getTimestamp();
    }

    public TransportStatistics getPrevious() {
        return this.previousStats;
    }

    public long getCompletedTaskCount() {
        return stats.getCompletedTaskCount();
    }

    public double getCompletedTaskPerSecond() {
        if (previousStats == null) {
            return -1;
        }
        return StatisticsUtils.computePerSecond(getCompletedTaskCount(), getPrevious().getCompletedTaskCount(), getTimestamp(), getPreviousTimestamp());
    }

    public int getActiveThreadsCount() {
        return stats.getActiveThreadsCount();
    }

    public double getActiveThreadsPerc() {
        return StatisticsUtils.computePerc(getActiveThreadsCount(), getDetails().getMaxThreads());
    }

    public int getQueueSize() {
        return stats.getQueueSize();
    }
}
