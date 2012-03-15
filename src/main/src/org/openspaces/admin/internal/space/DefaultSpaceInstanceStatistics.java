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
package org.openspaces.admin.internal.space;

import org.openspaces.admin.space.SpaceInstanceStatistics;
import org.openspaces.admin.support.StatisticsUtils;

import com.gigaspaces.cluster.replication.async.mirror.MirrorStatistics;
import com.j_spaces.core.filters.ReplicationStatistics;
import com.j_spaces.core.filters.StatisticsHolder;

/**
 * @author kimchy
 */
public class DefaultSpaceInstanceStatistics implements SpaceInstanceStatistics {

    private final long timeDelta;

    private final StatisticsHolder statisticsHolder;

    private volatile SpaceInstanceStatistics previousStats;

    public DefaultSpaceInstanceStatistics(StatisticsHolder statisticsHolder, SpaceInstanceStatistics previousStats, int historySize, long timeDelta) {
        this.statisticsHolder = statisticsHolder;
        this.previousStats = previousStats;
        this.timeDelta = timeDelta;

        SpaceInstanceStatistics lastStats = previousStats;
        if (lastStats != null) {
            for (int i = 0; i < historySize; i++) {
                if (lastStats.getPrevious() == null) {
                    break;
                }
                lastStats = lastStats.getPrevious();
            }
            ((DefaultSpaceInstanceStatistics) lastStats).setPreviousStats(null);
        }
    }

    public boolean isNA() {
        return statisticsHolder.getOperationsCount()[0] == -1;
    }

    public long getTimestamp() {
        return statisticsHolder.getTimestamp();
    }

    public long getAdminTimestamp() {
        if (statisticsHolder.getTimestamp() != -1 && timeDelta != Integer.MIN_VALUE) {
            return statisticsHolder.getTimestamp() + timeDelta;
        }
        return -1;
    }

    public long getPreviousTimestamp() {
        if (previousStats == null) {
            return -1;
        }
        return previousStats.getTimestamp();
    }

    public SpaceInstanceStatistics getPrevious() {
        return previousStats;
    }

    public void setPreviousStats(SpaceInstanceStatistics previousStats) {
        this.previousStats = previousStats;
    }

    public long getWriteCount() {
        return statisticsHolder.getOperationsCount()[0];
    }

    public double getWritePerSecond() {
        if (previousStats == null) {
            return -1;
        }
        return StatisticsUtils.computePerSecond(getWriteCount(), getPrevious().getWriteCount(), getTimestamp(), getPreviousTimestamp());
    }

    public long getReadCount() {
        // read and read multiple
        return statisticsHolder.getOperationsCount()[1] + statisticsHolder.getOperationsCount()[6];
    }

    public double getReadPerSecond() {
        if (previousStats == null) {
            return -1;
        }
        return StatisticsUtils.computePerSecond(getReadCount(), getPrevious().getReadCount(), getTimestamp(), getPreviousTimestamp());
    }

    public long getTakeCount() {
        // take and take multiple
        return statisticsHolder.getOperationsCount()[2] + statisticsHolder.getOperationsCount()[7];
    }

    public double getTakePerSecond() {
        if (previousStats == null) {
            return -1;
        }
        return StatisticsUtils.computePerSecond(getTakeCount(), getPrevious().getTakeCount(), getTimestamp(), getPreviousTimestamp());
    }

    public long getNotifyRegistrationCount() {
        return statisticsHolder.getOperationsCount()[3];
    }

    public double getNotifyRegistrationPerSecond() {
        if (previousStats == null) {
            return -1;
        }
        return StatisticsUtils.computePerSecond(getNotifyRegistrationCount(), getPrevious().getNotifyRegistrationCount(), getTimestamp(), getPreviousTimestamp());
    }

    public long getCleanCount() {
        return statisticsHolder.getOperationsCount()[4];
    }

    public double getCleanPerSecond() {
        if (previousStats == null) {
            return -1;
        }
        return StatisticsUtils.computePerSecond(getCleanCount(), getPrevious().getCleanCount(), getTimestamp(), getPreviousTimestamp());
    }

    public long getUpdateCount() {
        return statisticsHolder.getOperationsCount()[5];
    }

    public double getUpdatePerSecond() {
        if (previousStats == null) {
            return -1;
        }
        return StatisticsUtils.computePerSecond(getUpdateCount(), getPrevious().getUpdateCount(), getTimestamp(), getPreviousTimestamp());
    }

    public long getNotifyTriggerCount() {
        return statisticsHolder.getOperationsCount()[8];
    }

    public double getNotifyTriggerPerSecond() {
        if (previousStats == null) {
            return -1;
        }
        return StatisticsUtils.computePerSecond(getNotifyTriggerCount(), getPrevious().getNotifyTriggerCount(), getTimestamp(), getPreviousTimestamp());
    }

    public long getNotifyAckCount() {
        return statisticsHolder.getOperationsCount()[9];
    }

    public double getNotifyAckPerSecond() {
        if (previousStats == null) {
            return -1;
        }
        return StatisticsUtils.computePerSecond(getNotifyAckCount(), getPrevious().getNotifyAckCount(), getTimestamp(), getPreviousTimestamp());
    }

    public long getExecuteCount() {
        return statisticsHolder.getOperationsCount()[10];
    }

    public double getExecutePerSecond() {
        if (previousStats == null) {
            return -1;
        }
        return StatisticsUtils.computePerSecond(getExecuteCount(), getPrevious().getExecuteCount(), getTimestamp(), getPreviousTimestamp());
    }

    /**
     * Remove happens when an entry is removed due to lease expiration or lease cancel.
     */
    public long getRemoveCount() {
        return statisticsHolder.getOperationsCount()[11];
    }

    public double getRemovePerSecond() {
        if (previousStats == null) {
            return -1;
        }
        return StatisticsUtils.computePerSecond(getRemoveCount(), getPrevious().getRemoveCount(), getTimestamp(), getPreviousTimestamp());
    }

    public ReplicationStatistics getReplicationStatistics() {
        return statisticsHolder.getReplicationStatistics();
    }
    public MirrorStatistics getMirrorStatistics() {
        return statisticsHolder.getMirrorStatistics();
    }
    
    public int getProcessorQueueSize() {
        return statisticsHolder.getProcessorQueueSize();
    }
    
    public int getNotifierQueueSize() {
        return statisticsHolder.getNotifierQueueSize();
    }
}
