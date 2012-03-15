/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
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

    private final long timeDelta;

    private final NIOStatistics stats;

    private volatile TransportStatistics previousStats;

    private final TransportDetails details;

    public DefaultTransportStatistics() {
        this(NA_STATS, null, null, 0, -1);
    }

    public DefaultTransportStatistics(NIOStatistics stats, TransportStatistics previousStats, TransportDetails details, int historySize, long timeDelta) {
        this.stats = stats;
        this.previousStats = previousStats;
        this.details = details;
        this.timeDelta = timeDelta;

        TransportStatistics lastStats = previousStats;
        if (lastStats != null) {
            for (int i = 0; i < historySize; i++) {
                if (lastStats.getPrevious() == null) {
                    break;
                }
                lastStats = lastStats.getPrevious();
            }
            ((DefaultTransportStatistics) lastStats).setPreviousStats(null);
        }
    }

    public boolean isNA() {
        return stats.isNA();
    }

    public long getTimestamp() {
        return stats.getTimestamp();
    }

    public long getAdminTimestamp() {
        if (stats.getTimestamp() != -1 && timeDelta != Integer.MIN_VALUE) {
            return stats.getTimestamp() + timeDelta;
        }
        return -1;
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

    public void setPreviousStats(TransportStatistics previousStats) {
        this.previousStats = previousStats;
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
