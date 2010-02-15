package org.openspaces.admin.internal.vm;

import org.openspaces.admin.support.StatisticsUtils;
import org.openspaces.admin.vm.VirtualMachineDetails;
import org.openspaces.admin.vm.VirtualMachineStatistics;

import com.gigaspaces.internal.jvm.JVMStatistics;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public class DefaultVirtualMachineStatistics implements VirtualMachineStatistics {

    private static final JVMStatistics NA_STATS = new JVMStatistics();

    private final JVMStatistics stats;

    private final VirtualMachineDetails details;

    private volatile VirtualMachineStatistics previousStats;

    public DefaultVirtualMachineStatistics() {
        this(NA_STATS, null, null, 0);
    }

    public DefaultVirtualMachineStatistics(JVMStatistics stats, VirtualMachineStatistics previousStats, VirtualMachineDetails details, int historySize) {
        this.stats = stats;
        this.previousStats = previousStats;
        this.details = details;

        VirtualMachineStatistics lastStats = previousStats;
        if (lastStats != null) {
            for (int i = 0; i < historySize; i++) {
                if (lastStats.getPrevious() == null) {
                    break;
                }
                lastStats = lastStats.getPrevious();
            }
            ((DefaultVirtualMachineStatistics) lastStats).setPreviousStats(null);
        }
    }

    public boolean isNA() {
        return stats.isNA();
    }

    public long getTimestamp() {
        return stats.getTimestamp();
    }

    public VirtualMachineDetails getDetails() {
        return this.details;
    }

    public List<VirtualMachineStatistics> getTimeline() {
        ArrayList<VirtualMachineStatistics> timeline = new ArrayList<VirtualMachineStatistics>();
        timeline.add(this);
        VirtualMachineStatistics current = this.getPrevious();
        while (current != null && !current.isNA()) {
            timeline.add(current);
        }
        return timeline;
    }

    public VirtualMachineStatistics getPrevious() {
        return previousStats;
    }

    public void setPreviousStats(VirtualMachineStatistics previousStats) {
        this.previousStats = previousStats;
    }

    public long getPreviousTimestamp() {
        if (previousStats == null) {
            return -1;
        }
        return previousStats.getTimestamp();
    }

    public long getUptime() {
        return stats.getUptime();
    }

    public long getMemoryHeapCommittedInBytes() {
        return stats.getMemoryHeapCommitted();
    }

    public double getMemoryHeapCommittedInMB() {
        return StatisticsUtils.convertToMB(getMemoryHeapCommittedInBytes());
    }

    public double getMemoryHeapCommittedInGB() {
        return StatisticsUtils.convertToGB(getMemoryHeapCommittedInBytes());
    }

    public long getMemoryHeapUsedInBytes() {
        return stats.getMemoryHeapUsed();
    }

    public double getMemoryHeapUsedInMB() {
        return StatisticsUtils.convertToMB(getMemoryHeapUsedInBytes());
    }

    public double getMemoryHeapUsedInGB() {
        return StatisticsUtils.convertToGB(getMemoryHeapUsedInBytes());
    }

    public double getMemoryHeapUsedPerc() {
        if (isNA()) {
            return -1;
        } else {
            return StatisticsUtils.computePerc(getMemoryHeapUsedInBytes(), getDetails().getMemoryHeapMaxInBytes());
        }
    }

    public double getMemoryHeapCommittedUsedPerc() {
        return StatisticsUtils.computePerc(getMemoryHeapUsedInBytes(), getMemoryHeapCommittedInBytes());
    }

    public long getMemoryNonHeapCommittedInBytes() {
        return stats.getMemoryNonHeapCommitted();
    }

    public double getMemoryNonHeapCommittedInMB() {
        return StatisticsUtils.convertToMB(getMemoryNonHeapCommittedInBytes());
    }

    public double getMemoryNonHeapCommittedInGB() {
        return StatisticsUtils.convertToGB(getMemoryNonHeapCommittedInBytes());
    }

    public long getMemoryNonHeapUsedInBytes() {
        return stats.getMemoryNonHeapUsed();
    }

    public double getMemoryNonHeapUsedInMB() {
        return StatisticsUtils.convertToMB(getMemoryNonHeapUsedInBytes());
    }

    public double getMemoryNonHeapUsedInGB() {
        return StatisticsUtils.convertToGB(getMemoryNonHeapUsedInBytes());
    }

    public double getMemoryNonHeapUsedPerc() {
        if (isNA()) {
            return -1;
        } else {
            return StatisticsUtils.computePerc(getMemoryNonHeapUsedInBytes(), getDetails().getMemoryNonHeapMaxInBytes());
        }
    }

    public double getMemoryNonHeapCommittedUsedPerc() {
        return StatisticsUtils.computePerc(getMemoryNonHeapUsedInBytes(), getMemoryNonHeapCommittedInBytes());
    }

    public int getThreadCount() {
        return stats.getThreadCount();
    }

    public int getPeakThreadCount() {
        return stats.getPeakThreadCount();
    }

    public long getGcCollectionCount() {
        return stats.getGcCollectionCount();
    }

    public long getGcCollectionTime() {
        return stats.getGcCollectionTime();
    }

    public double getGcCollectionPerc() {
        if (previousStats == null) {
            return -1;
        }
        return StatisticsUtils.computePercByTime(getGcCollectionTime(), previousStats.getGcCollectionTime(), getTimestamp(), getPreviousTimestamp());
    }

    public double getCpuPerc() {
        return stats.getCpuPerc();
    }

    public String getCpuPercFormatted() {
        return StatisticsUtils.formatPerc(getCpuPerc());
    }
}
