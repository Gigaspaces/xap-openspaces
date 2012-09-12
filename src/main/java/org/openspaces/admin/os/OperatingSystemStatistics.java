/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.admin.os;

import java.util.List;
import java.util.Map;

/**
 * Statistics of a specific {@link org.openspaces.admin.os.OperatingSystem}.
 *
 * @author kimchy
 */
public interface OperatingSystemStatistics {

    /**
     * Returns <code>true</code> if the statistics are not available.
     */
    boolean isNA();

    /**
     * Returns the timestamp when the statistics were take.
     */
    long getTimestamp();

    /**
     * Returns a timestamp that is in sync with where the admin API is running. Can return
     * -1 if the clocks have are not sync yet.
     */
    long getAdminTimestamp();

    /**
     * Returns the operating system details.
     */
    OperatingSystemDetails getDetails();

    /**
     * Returns the timeline (from newest to oldest) history statistics, including this one.
     */
    List<OperatingSystemStatistics> getTimeline();

    /**
     * Returns the previous statistics taken. Returns <code>null</code> if this is the fist one.
     */
    OperatingSystemStatistics getPrevious();

    long getFreeSwapSpaceSizeInBytes();

    double getFreeSwapSpaceSizeInMB();

    double getFreeSwapSpaceSizeInGB();

    /**
     * Returns the percentage used of swap space out of the total swap space.
     */
    double getSwapSpaceUsedPerc();

    long getFreePhysicalMemorySizeInBytes();

    double getFreePhysicalMemorySizeInMB();

    double getFreePhysicalMemorySizeInGB();

    long getActualFreePhysicalMemorySizeInBytes();

    double getActualFreePhysicalMemorySizeInMB();

    double getActualFreePhysicalMemorySizeInGB();

    /**
     * Returns the percentage used of physical memory out of the total physical memory space.
     * Uses the total free memory e.g. e.g. Linux plus cached.
     */
    double getPhysicalMemoryUsedPerc();

    /**
     * Returns the percentage used of the actual physical memory out of the total physical memory.
     */
    double getActualPhysicalMemoryUsedPerc();


    /**
     * Returns the combined cpu perc (User + Sys + Nice + Wait)
     */
    double getCpuPerc();

    String getCpuPercFormatted();

    /**
     * Returns the network statistics per network device.
     */
    Map<String, NetworkStatistics> getNetworkStats();

    interface NetworkStatistics {

        /**
         * The name of the network device.
         */
        String getName();

        /**
         * The total rx bytes received.
         */
        long getRxBytes();

        /**
         * The number of bytes received per second (computed against the previous sampled stats).
         */
        double getRxBytesPerSecond();

        /**
         * The total tx bytes transmitted.
         */
        long getTxBytes();

        /**
         * The number of bytes transmitted per second (computed against the previous sampled stats).
         */
        double getTxBytesPerSecond();

        long getRxPackets();

        double getRxPacketsPerSecond();

        long getTxPackets();

        double getTxPacketsPerSecond();

        long getRxErrors();

        long getTxErrors();

        long getRxDropped();

        long getTxDropped();


        NetworkStatistics getPrevious();
    }
}
