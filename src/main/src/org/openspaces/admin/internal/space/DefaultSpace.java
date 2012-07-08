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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.space.events.DefaultReplicationStatusChangedEventManager;
import org.openspaces.admin.internal.space.events.DefaultSpaceInstanceAddedEventManager;
import org.openspaces.admin.internal.space.events.DefaultSpaceInstanceRemovedEventManager;
import org.openspaces.admin.internal.space.events.DefaultSpaceInstanceStatisticsChangedEventManager;
import org.openspaces.admin.internal.space.events.DefaultSpaceModeChangedEventManager;
import org.openspaces.admin.internal.space.events.DefaultSpaceStatisticsChangedEventManager;
import org.openspaces.admin.internal.space.events.InternalReplicationStatusChangedEventManager;
import org.openspaces.admin.internal.space.events.InternalSpaceInstanceAddedEventManager;
import org.openspaces.admin.internal.space.events.InternalSpaceInstanceRemovedEventManager;
import org.openspaces.admin.internal.space.events.InternalSpaceInstanceStatisticsChangedEventManager;
import org.openspaces.admin.internal.space.events.InternalSpaceModeChangedEventManager;
import org.openspaces.admin.internal.space.events.InternalSpaceStatisticsChangedEventManager;
import org.openspaces.admin.internal.support.NetworkExceptionHelper;
import org.openspaces.admin.pu.DeploymentStatus;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.space.ReplicationStatus;
import org.openspaces.admin.space.ReplicationTarget;
import org.openspaces.admin.space.ReplicationTargetType;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.SpaceInstanceStatistics;
import org.openspaces.admin.space.SpacePartition;
import org.openspaces.admin.space.SpaceReplicationManager;
import org.openspaces.admin.space.SpaceRuntimeDetails;
import org.openspaces.admin.space.SpaceStatistics;
import org.openspaces.admin.space.Spaces;
import org.openspaces.admin.space.events.ReplicationStatusChangedEventManager;
import org.openspaces.admin.space.events.SpaceInstanceAddedEventListener;
import org.openspaces.admin.space.events.SpaceInstanceAddedEventManager;
import org.openspaces.admin.space.events.SpaceInstanceLifecycleEventListener;
import org.openspaces.admin.space.events.SpaceInstanceRemovedEventManager;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEventManager;
import org.openspaces.admin.space.events.SpaceModeChangedEvent;
import org.openspaces.admin.space.events.SpaceModeChangedEventListener;
import org.openspaces.admin.space.events.SpaceModeChangedEventManager;
import org.openspaces.admin.space.events.SpaceStatisticsChangedEvent;
import org.openspaces.admin.space.events.SpaceStatisticsChangedEventManager;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;

import com.gigaspaces.cluster.activeelection.InactiveSpaceException;
import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import com.gigaspaces.internal.cluster.node.impl.gateway.GatewayPolicy;
import com.gigaspaces.internal.version.PlatformLogicalVersion;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.ISpaceState;
import com.j_spaces.core.admin.IRemoteJSpaceAdmin;
import com.j_spaces.core.admin.RuntimeHolder;
import com.j_spaces.core.exception.SpaceUnavailableException;
import com.j_spaces.core.exception.internal.InterruptedSpaceException;
import com.j_spaces.core.filters.ReplicationStatistics;
import com.j_spaces.core.filters.ReplicationStatistics.OutgoingChannel;
import com.j_spaces.core.filters.StatisticsHolder;
import com.j_spaces.kernel.JSpaceUtilities;
import com.j_spaces.kernel.SizeConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultSpace implements InternalSpace {

    private static final Log logger = LogFactory.getLog(DefaultSpace.class);

    private final InternalAdmin admin;

    private final InternalSpaces spaces;

    private final String uid;

    private final String name;

    private volatile IJSpace space;

    private volatile GigaSpace gigaSpace;

    private final Map<String, SpaceInstance> spaceInstancesByUID = new SizeConcurrentHashMap<String, SpaceInstance>();

    private final Map<String, SpaceInstance> spaceInstancesByMemberName = new ConcurrentHashMap<String, SpaceInstance>();

    private final Map<Integer, SpacePartition> spacePartitions = new SizeConcurrentHashMap<Integer, SpacePartition>();

    private final Map<String, Future> scheduledRuntimeFetchers = new ConcurrentHashMap<String, Future>();

    private final InternalSpaceInstanceAddedEventManager spaceInstanceAddedEventManager;

    private final InternalSpaceInstanceRemovedEventManager spaceInstanceRemovedEventManager;

    private final InternalSpaceModeChangedEventManager spaceModeChangedEventManager;

    private final InternalReplicationStatusChangedEventManager replicationStatusChangedEventManager;

    private final InternalSpaceStatisticsChangedEventManager statisticsChangedEventManager;

    private final InternalSpaceInstanceStatisticsChangedEventManager instanceStatisticsChangedEventManager;

    private volatile long statisticsInterval = StatisticsMonitor.DEFAULT_MONITOR_INTERVAL;

    private volatile int statisticsHistorySize = StatisticsMonitor.DEFAULT_HISTORY_SIZE;

    private long lastStatisticsTimestamp = 0;

    private long lastPrimariesStatisticsTimestamp = 0;

    private long lastBackupsStatisticsTimestamp = 0;

    private SpaceStatistics lastStatistics;

    private SpaceStatistics lastPrimariesStatistics;

    private SpaceStatistics lastBackupStatistics;
    
    private final SpaceRuntimeDetails spaceRuntimeDetails;
    
    private final SpaceReplicationManager spaceReplicationManager;

    private Future scheduledStatisticsMonitor;
    
    private int scheduledStatisticsRefCount = 0;

    public DefaultSpace(InternalSpaces spaces, String uid, String name) {
        this.spaces = spaces;
        this.admin = (InternalAdmin) spaces.getAdmin();
        this.uid = uid;
        this.name = name;

        this.spaceInstanceAddedEventManager = new DefaultSpaceInstanceAddedEventManager(admin, this);
        this.spaceInstanceRemovedEventManager = new DefaultSpaceInstanceRemovedEventManager(admin);
        this.spaceModeChangedEventManager = new DefaultSpaceModeChangedEventManager(this, admin);
        this.replicationStatusChangedEventManager = new DefaultReplicationStatusChangedEventManager(admin);
        this.statisticsChangedEventManager = new DefaultSpaceStatisticsChangedEventManager(admin);
        this.instanceStatisticsChangedEventManager = new DefaultSpaceInstanceStatisticsChangedEventManager(admin);
        this.spaceRuntimeDetails = new DefaultSpaceRuntimeDetails(this);
        this.spaceReplicationManager = new DefaultSpaceReplicationManager(this);
    }

    public Spaces getSpaces() {
        return this.spaces;
    }

    public String getUid() {
        return this.uid;
    }

    public String getName() {
        return this.name;
    }

    public synchronized void setStatisticsInterval(long interval, TimeUnit timeUnit) {
        statisticsInterval = timeUnit.toMillis(interval);
        if (isMonitoring()) {
            rescheduleStatisticsMonitor();
        }
        for (SpaceInstance spaceInstance : spaceInstancesByUID.values()) {
            spaceInstance.setStatisticsInterval(interval, timeUnit);
        }
    }

    public void setStatisticsHistorySize(int historySize) {
        statisticsHistorySize = historySize;
        for (SpaceInstance spaceInstance : spaceInstancesByUID.values()) {
            spaceInstance.setStatisticsHistorySize(historySize);
        }
    }

    public synchronized void startStatisticsMonitor() {
        rescheduleStatisticsMonitor();
        for (SpaceInstance spaceInstance : spaceInstancesByUID.values()) {
            spaceInstance.startStatisticsMonitor();
        }
    }

    public synchronized void stopStatisticsMonitor() {
        stopScheduledStatisticsMonitor();
        for (SpaceInstance spaceInstance : spaceInstancesByUID.values()) {
            spaceInstance.stopStatisticsMonitor();
        }
    }

    private void stopScheduledStatisticsMonitor() {
        if (scheduledStatisticsRefCount!=0 && --scheduledStatisticsRefCount > 0) return;
        
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
            scheduledStatisticsMonitor = null;
        }
    }

    public synchronized boolean isMonitoring() {
        return scheduledStatisticsMonitor != null;
    }

    private void rescheduleStatisticsMonitor() {
        if (scheduledStatisticsRefCount++ > 0) return;
        
        if (scheduledStatisticsMonitor != null) {
            scheduledStatisticsMonitor.cancel(false);
        }
        scheduledStatisticsMonitor = admin.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                SpaceStatistics stats = getStatistics();
                SpaceStatisticsChangedEvent event = new SpaceStatisticsChangedEvent(DefaultSpace.this, stats);
                statisticsChangedEventManager.spaceStatisticsChanged(event);
                ((InternalSpaceStatisticsChangedEventManager) spaces.getSpaceStatisticsChanged()).spaceStatisticsChanged(event);
            }
        }, 0, statisticsInterval, TimeUnit.MILLISECONDS);
    }

    private int numberOfInstances = -1;

    public int getNumberOfInstances() {
        if (numberOfInstances != -1) {
            return numberOfInstances;
        }
        numberOfInstances = doWithInstance(new InstanceCallback<Integer>() {
            public Integer doWithinstance(InternalSpaceInstance spaceInstance) {
                return spaceInstance.getNumberOfInstances();
            }
        }, -1);
        return numberOfInstances;
    }

    private int numberOfBackups = -1;

    public int getNumberOfBackups() {
        if (numberOfBackups != -1) {
            return numberOfBackups;
        }
        numberOfBackups = doWithInstance(new InstanceCallback<Integer>() {
            public Integer doWithinstance(InternalSpaceInstance spaceInstance) {
                return spaceInstance.getNumberOfBackups();
            }
        }, -1);
        return numberOfBackups;
    }

    public int getTotalNumberOfInstances() {
        return getNumberOfInstances() * (getNumberOfBackups() + 1);
    }

    public SpaceInstance[] getInstances() {
        return spaceInstancesByUID.values().toArray(new SpaceInstance[0]);
    }

    public Iterator<SpaceInstance> iterator() {
        return Collections.unmodifiableCollection(spaceInstancesByUID.values()).iterator();
    }

    public SpacePartition[] getPartitions() {
        return spacePartitions.values().toArray(new SpacePartition[0]);
    }

    public SpacePartition getPartition(int partitionId) {
        return spacePartitions.get(partitionId);
    }

    public SpaceModeChangedEventManager getSpaceModeChanged() {
        return this.spaceModeChangedEventManager;
    }

    public ReplicationStatusChangedEventManager getReplicationStatusChanged() {
        return this.replicationStatusChangedEventManager;
    }

    public SpaceStatisticsChangedEventManager getStatisticsChanged() {
        return this.statisticsChangedEventManager;
    }

    public SpaceInstanceStatisticsChangedEventManager getInstanceStatisticsChanged() {
        return this.instanceStatisticsChangedEventManager;
    }

    public synchronized SpaceStatistics getStatistics() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastStatisticsTimestamp) < statisticsInterval) {
            return lastStatistics;
        }
        lastStatisticsTimestamp = currentTime;
        List<SpaceInstanceStatistics> stats = new ArrayList<SpaceInstanceStatistics>();
        for (SpaceInstance spaceInstance : spaceInstancesByUID.values()) {
            stats.add(spaceInstance.getStatistics());
        }
        lastStatistics = new DefaultSpaceStatistics(stats.toArray(new SpaceInstanceStatistics[stats.size()]), lastStatistics, statisticsHistorySize);
        return lastStatistics;
    }

    public synchronized SpaceStatistics getPrimariesStatistics() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastPrimariesStatisticsTimestamp) < statisticsInterval) {
            return lastPrimariesStatistics;
        }
        lastPrimariesStatisticsTimestamp = currentTime;
        List<SpaceInstanceStatistics> stats = new ArrayList<SpaceInstanceStatistics>();
        for (SpaceInstance spaceInstance : spaceInstancesByUID.values()) {
            if (spaceInstance.getMode() == SpaceMode.PRIMARY) {
                stats.add(spaceInstance.getStatistics());
            }
        }
        lastPrimariesStatistics = new DefaultSpaceStatistics(stats.toArray(new SpaceInstanceStatistics[stats.size()]), lastPrimariesStatistics, statisticsHistorySize);
        return lastPrimariesStatistics;
    }

    public synchronized SpaceStatistics getBackupsStatistics() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastBackupsStatisticsTimestamp) < statisticsInterval) {
            return lastStatistics;
        }
        lastBackupsStatisticsTimestamp = currentTime;
        List<SpaceInstanceStatistics> stats = new ArrayList<SpaceInstanceStatistics>();
        for (SpaceInstance spaceInstance : spaceInstancesByUID.values()) {
            if (spaceInstance.getMode() == SpaceMode.BACKUP) {
                stats.add(spaceInstance.getStatistics());
            }
        }
        lastBackupStatistics = new DefaultSpaceStatistics(stats.toArray(new SpaceInstanceStatistics[stats.size()]), lastBackupStatistics, statisticsHistorySize);
        return lastBackupStatistics;
    }
    
    public SpaceRuntimeDetails getRuntimeDetails() {
        return spaceRuntimeDetails;
    }
    
    @Override
    public SpaceReplicationManager getReplicationManager() {
        return spaceReplicationManager;
    }

    public void addInstance(final SpaceInstance spaceInstance) {
        assertStateChangesPermitted();
        InternalSpaceInstance internalSpaceInstance = (InternalSpaceInstance) spaceInstance;
        // the first addition (which we make sure is added before the space becomes visible) will
        // cause the partition to initialize
        if (spaceInstancesByUID.isEmpty()) {
            // guess if its a partition (with no backup) since we can't tell if its replicated or just partitioned
            if (internalSpaceInstance.getClusterSchema() != null && internalSpaceInstance.getClusterSchema().contains("partition")) {
                for (int i = 0; i < internalSpaceInstance.getNumberOfInstances(); i++) {
                    spacePartitions.put(i, new DefaultSpacePartition(this, i));
                }
            } else {
                if (internalSpaceInstance.getNumberOfBackups() == 0) {
                    // single partition (replicated?) when there is no backup
                    spacePartitions.put(0, new DefaultSpacePartition(this, 0));
                } else {
                    for (int i = 0; i < internalSpaceInstance.getNumberOfInstances(); i++) {
                        spacePartitions.put(i, new DefaultSpacePartition(this, i));
                    }
                }
            }
        }
        SpaceInstance existing = spaceInstancesByUID.put(spaceInstance.getUid(), spaceInstance);
        String fullSpaceName = JSpaceUtilities.createFullSpaceName(spaceInstance.getSpaceUrl().getContainerName(), spaceInstance.getSpaceUrl().getSpaceName());
        spaceInstancesByMemberName.put(fullSpaceName, spaceInstance);
        InternalSpacePartition spacePartition = getPartition(internalSpaceInstance);
        internalSpaceInstance.setPartition(spacePartition);
        spacePartition.addSpaceInstance(spaceInstance);

        if (existing == null) {
            spaceInstanceAddedEventManager.spaceInstanceAdded(spaceInstance);
            ((InternalSpaceInstanceAddedEventManager) spaces.getSpaceInstanceAdded()).spaceInstanceAdded(spaceInstance);
        }

        spaceInstance.setStatisticsInterval(statisticsInterval, TimeUnit.MILLISECONDS);
        spaceInstance.setStatisticsHistorySize(statisticsHistorySize);
        if (isMonitoring()) {
            admin.raiseEvent(this, new Runnable() {
                public void run() {
                    spaceInstance.startStatisticsMonitor();
                }
            });
        }

        // start the scheduler
        Future fetcher = scheduledRuntimeFetchers.get(spaceInstance.getUid());
        if (fetcher == null) {
            fetcher = admin.scheduleWithFixedDelay(new ScheduledRuntimeFetcher(spaceInstance), admin.getScheduledSpaceMonitorInterval(), admin.getScheduledSpaceMonitorInterval(), TimeUnit.MILLISECONDS);
            scheduledRuntimeFetchers.put(spaceInstance.getUid(), fetcher);
        }
    }

    public InternalSpaceInstance removeInstance(String uid) {
        assertStateChangesPermitted();
        InternalSpaceInstance spaceInstance = (InternalSpaceInstance) spaceInstancesByUID.remove(uid);
        if (spaceInstance != null) {
            spaceInstance.stopStatisticsMonitor();
            getPartition(spaceInstance).removeSpaceInstance(uid);
            String fullSpaceName = JSpaceUtilities.createFullSpaceName(spaceInstance.getSpaceUrl().getContainerName(), spaceInstance.getSpaceUrl().getSpaceName());
            spaceInstancesByMemberName.remove(fullSpaceName);
            spaceInstanceRemovedEventManager.spaceInstanceRemoved(spaceInstance);
            ((InternalSpaceInstanceRemovedEventManager) spaces.getSpaceInstanceRemoved()).spaceInstanceRemoved(spaceInstance);
            Future fetcher = scheduledRuntimeFetchers.get(uid);
            if (fetcher != null) {
                fetcher.cancel(true);
            }
        }
        return spaceInstance;
    }

    private InternalSpacePartition getPartition(InternalSpaceInstance spaceInstance) {
        if (spaceInstance.getClusterSchema() != null && spaceInstance.getClusterSchema().contains("partition")) {
            return (InternalSpacePartition) spacePartitions.get(spaceInstance.getInstanceId() - 1);
        } else {
            if (spaceInstance.getNumberOfBackups() == 0) {
                return (InternalSpacePartition) spacePartitions.get(0);
            } else {
                return (InternalSpacePartition) spacePartitions.get(spaceInstance.getInstanceId() - 1);
            }
        }
    }

    // no need to sync since it is synced on Admin

    public void refreshScheduledSpaceMonitors() {
        for (Future fetcher : scheduledRuntimeFetchers.values()) {
            fetcher.cancel(false);
        }
        for (SpaceInstance spaceInstance : this) {
            Future fetcher = admin.scheduleWithFixedDelay(new ScheduledRuntimeFetcher(spaceInstance), admin.getScheduledSpaceMonitorInterval(), admin.getScheduledSpaceMonitorInterval(), TimeUnit.MILLISECONDS);
            scheduledRuntimeFetchers.put(spaceInstance.getUid(), fetcher);
        }
    }

    public int getSize() {
        return spaceInstancesByUID.size();
    }

    public boolean isEmpty() {
        return spaceInstancesByUID.size() == 0;
    }

    public IJSpace getIJSpace() {
        if (space == null) {
            
            SpaceInstance firstInstance = null;
            for (SpaceInstance instance : spaceInstancesByUID.values()) {
                firstInstance = instance;
                break;
            }
            
            if (firstInstance == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Space " + getName() + " cannot create IJSpace since it has no instances");
                }
                return null;
            }
            
            try {
                space = ((ISpaceProxy) ((InternalSpaceInstance) firstInstance).getIJSpace()).getClusteredSpace();
                if (space.isSecured()) {
                    ((ISpaceProxy)space).login(admin.getUserDetails());
                }
            } catch (AdminException e) {
                throw e;
            } catch (Exception e) {
                throw new AdminException("Failed to fetch space", e);
            }
        }
        return space;
    }

    public GigaSpace getGigaSpace() {
        if (gigaSpace == null) {
            
            IJSpace ijSpace = getIJSpace();
            if (ijSpace == null) {
                throw new AdminException("Cannot create GigaSpace object since no space instance is discovered");
            }
            this.gigaSpace = new GigaSpaceConfigurer(ijSpace).clustered(true).gigaSpace();
        }
        return this.gigaSpace;
    }

    public boolean waitFor(int numberOfSpaceInstances) {
        return waitFor(numberOfSpaceInstances, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public boolean waitFor(int numberOfSpaceInstances, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(numberOfSpaceInstances);
        SpaceInstanceAddedEventListener added = new SpaceInstanceAddedEventListener() {
            public void spaceInstanceAdded(SpaceInstance spaceInstance) {
                latch.countDown();
            }
        };
        getSpaceInstanceAdded().add(added);
        try {
            return latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            return false;
        } finally {
            getSpaceInstanceAdded().remove(added);
        }
    }

    public boolean waitFor(int numberOfSpaceInstances, SpaceMode spaceMode) {
        return waitFor(numberOfSpaceInstances, spaceMode, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public boolean waitFor(int numberOfSpaceInstances, final SpaceMode spaceMode, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(numberOfSpaceInstances);
        SpaceModeChangedEventListener modeChanged = new SpaceModeChangedEventListener() {
            public void spaceModeChanged(SpaceModeChangedEvent event) {
                if (event.getNewMode() == spaceMode) {
                    latch.countDown();
                }
            }
        };
        getSpaceModeChanged().add(modeChanged);
        boolean result;
        try {
            result = latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            return false;
        } finally {
            getSpaceModeChanged().remove(modeChanged);
        }
        if (result) {
            // double check again
            int sum = 0;
            for (SpaceInstance spaceInstance : this) {
                if (spaceInstance.getMode() == spaceMode) {
                    sum++;
                }
            }
            if (sum < numberOfSpaceInstances) {
                return waitFor(numberOfSpaceInstances, spaceMode, timeout, timeUnit);
            }
        }
        return result;
    }

    public SpaceInstance[] getSpaceInstances() {
        return getInstances();
    }

    public SpaceInstanceAddedEventManager getSpaceInstanceAdded() {
        return this.spaceInstanceAddedEventManager;
    }

    public SpaceInstanceRemovedEventManager getSpaceInstanceRemoved() {
        return this.spaceInstanceRemovedEventManager;
    }

    public void addLifecycleListener(SpaceInstanceLifecycleEventListener eventListener) {
        spaceInstanceAddedEventManager.add(eventListener);
        spaceInstanceRemovedEventManager.add(eventListener);
    }

    public void removeLifecycleListener(SpaceInstanceLifecycleEventListener eventListener) {
        spaceInstanceAddedEventManager.remove(eventListener);
        spaceInstanceRemovedEventManager.remove(eventListener);
    }
    
    public Admin getAdmin() {
        return this.admin;
    }

    private <T> T doWithInstance(InstanceCallback<T> callback, T naValue) {
        for (SpaceInstance spaceInstance : spaceInstancesByUID.values()) {
            return callback.doWithinstance((InternalSpaceInstance) spaceInstance);
        }
        return naValue;
    }

    private static interface InstanceCallback<T> {
        T doWithinstance(InternalSpaceInstance spaceInstance);
    }

    private class ScheduledRuntimeFetcher implements Runnable {

        private final InternalSpaceInstance spaceInstance;

        private ScheduledRuntimeFetcher(SpaceInstance spaceInstance) {
            this.spaceInstance = (InternalSpaceInstance) spaceInstance;
        }

        public void run() {
            try {
                final RuntimeHolder runtimeHolder = spaceInstance.getRuntimeHolder();
                final StatisticsHolder statisticsHolder = spaceInstance.getStatisticsHolder();
                final ReplicationStatistics replicationStatistics = (statisticsHolder == null) ? null : statisticsHolder.getReplicationStatistics();
                if (runtimeHolder.getSpaceState() != null &&
                        (runtimeHolder.getSpaceState() == ISpaceState.STOPPED ||
                        runtimeHolder.getSpaceState() == ISpaceState.STARTING)) {
                    // we don't want to update the space mode to until the space state is STARTED
                    // The space instance starts as STOPPED then changes to STARTING and only after recovery is complete it's state STARTED

                    if (logger.isTraceEnabled()) {
                        logger.trace("Ignoring runtimeHolder of space:" + spaceInstance.getSpaceName() + " serviceID:" + spaceInstance.getServiceID() + " state="+runtimeHolder.getSpaceState());
                    }
                    return;
                }
                DefaultSpace.this.admin.scheduleNonBlockingStateChange(new Runnable() {
                    public void run() {                        
                        spaceInstance.setMode(runtimeHolder.getSpaceMode());
                        if (spaceInstance.getPlatformLogicalVersion().greaterOrEquals(PlatformLogicalVersion.v8_0_5))
                        {
                            if (replicationStatistics != null) {
                                final List<OutgoingChannel> channels = replicationStatistics.getOutgoingReplication().getChannels();
                                ReplicationTarget[] replicationTargets = new ReplicationTarget[channels.size()];
                                int index = 0;
                                for (OutgoingChannel outgoingChannel : channels) {
                                    ReplicationTargetType replicationTargetType;
                                    switch (outgoingChannel.getReplicationMode()) {
                                    case MIRROR:
                                        replicationTargetType = ReplicationTargetType.MIRROR_SERVICE;
                                        break;
                                    case GATEWAY:
                                        replicationTargetType = ReplicationTargetType.GATEWAY;
                                        break;
                                    case LOCAL_VIEW:
                                        replicationTargetType = ReplicationTargetType.LOCAL_VIEW;
                                        break;
                                    case DURABLE_NOTIFICATION:
                                        replicationTargetType = ReplicationTargetType.DURABLE_NOTIFICATION;
                                        break;
                                    case ACTIVE_SPACE:
                                    case BACKUP_SPACE:
                                        replicationTargetType = ReplicationTargetType.SPACE_INSTANCE;
                                        break;
                                    default:
                                        throw new IllegalArgumentException("Unexpected replication mode: " + outgoingChannel.getReplicationMode());
                                    }
                                    ReplicationStatus replicationStatus;
                                    switch (outgoingChannel.getChannelState()) {
                                    case ACTIVE:
                                        replicationStatus = ReplicationStatus.ACTIVE;
                                        break;
                                    case CONNECTED:
                                    case DISCONNECTED:
                                        replicationStatus = ReplicationStatus.DISCONNECTED;
                                        break;
                                        default:
                                            throw new IllegalArgumentException("Unexpected channel state: " + outgoingChannel.getChannelState());
                                    }
                                    SpaceInstance targetSpaceInstance = spaceInstancesByMemberName.get(outgoingChannel.getTargetMemberName());
                                    if (targetSpaceInstance == null && replicationTargetType == ReplicationTargetType.MIRROR_SERVICE) {
                                        String mirrorServiceName = outgoingChannel.getTargetMemberName().split(":")[1];
                                        Space mirrorServiceSpace = spaceInstance.getAdmin().getSpaces().getSpaceByName(mirrorServiceName);
                                        if (mirrorServiceSpace != null) {
                                            SpaceInstance[] instances = mirrorServiceSpace.getInstances();
                                            if (instances.length > 0) {
                                                SpaceInstance mirrorInstance = instances[0];
                                                if (mirrorInstance != null && mirrorInstance.getSpaceUrl().getSchema().equals("mirror")) {
                                                    //note: don't cache it in spaceInstanceByMemberName map since we don't get a removal event on this instance
                                                    targetSpaceInstance = mirrorInstance;
                                                }
                                            }
                                        }
                                    }
                                    replicationTargets[index++] = new ReplicationTarget((InternalSpaceInstance) targetSpaceInstance, replicationStatus, outgoingChannel.getTargetMemberName(), replicationTargetType);
                                }
                                spaceInstance.setReplicationTargets(replicationTargets);
                            }
                        }
                        else
                        {
                            // Fall back to old path
                            if (runtimeHolder.getReplicationStatus() != null) {
                                Object[] memberNames = (Object[]) runtimeHolder.getReplicationStatus()[0];
                                int[] replicationStatus = (int[]) runtimeHolder.getReplicationStatus()[1];
                                ReplicationTarget[] replicationTargets = new ReplicationTarget[memberNames.length];
                                for (int i = 0; i < memberNames.length; i++) {
                                    if (memberNames[i] == null) {
                                        continue;
                                    }
                                    SpaceInstance targetSpaceInstance = spaceInstancesByMemberName.get(memberNames[i]);
                                    ReplicationTargetType replicationTargetType = ReplicationTargetType.SPACE_INSTANCE;
                                    //Check if target is a gateway, so we cannot locate a target space instance for it
                                    if (((String)memberNames[i]).startsWith(GatewayPolicy.GATEWAY_NAME_PREFIX)){
                                        replicationTargetType = ReplicationTargetType.GATEWAY;                            
                                    }else {
                                        /*
                                         * If mirror-service is one of the replication target names, find its SpaceInstance.
                                         */
                                        if (targetSpaceInstance == null) {                            
                                            //we don't know the name of the mirror-service space (it can be any name that is different from the cluster name)
                                            if (!((String)memberNames[i]).endsWith(":"+name)) {
                                                String mirrorServiceName = ((String)memberNames[i]).split(":")[1];
                                                Space mirrorServiceSpace = spaceInstance.getAdmin().getSpaces().getSpaceByName(mirrorServiceName);
                                                if (mirrorServiceSpace != null) {
                                                    SpaceInstance mirrorInstance = mirrorServiceSpace.getInstances()[0];
                                                    if (mirrorInstance != null && mirrorInstance.getSpaceUrl().getSchema().equals("mirror")) {
                                                        //note: don't cache it in spaceInstanceByMemberName map since we don't get a removal event on this instance
                                                        targetSpaceInstance = mirrorInstance;
                                                        replicationTargetType = ReplicationTargetType.MIRROR_SERVICE;
                                                    }
                                                } else {
                                                    replicationTargetType = ReplicationTargetType.MIRROR_SERVICE; //we guess this is a mirror (since 8.0.1)
                                                }
                                            }
                                        }else {
                                            if (targetSpaceInstance != null && targetSpaceInstance.getSpaceUrl().getSchema().equals("mirror")) {
                                                replicationTargetType = ReplicationTargetType.MIRROR_SERVICE;
                                            }
                                        }
                                    }
                                    ReplicationStatus replStatus = null;
                                    switch (replicationStatus[i]) {
                                    case IRemoteJSpaceAdmin.REPLICATION_STATUS_ACTIVE:
                                        replStatus = ReplicationStatus.ACTIVE;
                                        break;
                                    case IRemoteJSpaceAdmin.REPLICATION_STATUS_DISCONNECTED:
                                        replStatus = ReplicationStatus.DISCONNECTED;
                                        break;
                                    case IRemoteJSpaceAdmin.REPLICATION_STATUS_DISABLED:
                                        replStatus = ReplicationStatus.DISABLED;
                                        break;
                                    }
                                    replicationTargets[i] = new ReplicationTarget((InternalSpaceInstance) targetSpaceInstance, replStatus, (String)memberNames[i], replicationTargetType);
                                }
                                spaceInstance.setReplicationTargets(replicationTargets);
                            }
                        }
                    }});
            } catch (SpaceUnavailableException e) {
                // space is going shutdown or abort process
            } catch (InactiveSpaceException e) {
                // ignore this (maybe we should add it as a state to a Space instance?)
            } catch (InterruptedSpaceException e) {
                ProcessingUnit pu = admin.getProcessingUnits().getProcessingUnit(name);
                if (pu == null ||
                        pu.getStatus() == DeploymentStatus.UNDEPLOYED) {
                    logger.debug("Failed to get runtime information", e);
                } else {
                    logger.warn("Failed to get runtime information", e);
                }
            } catch (Exception e) {
                if (NetworkExceptionHelper.isConnectOrCloseException(e)) {
                    logger.debug("Failed to get runtime information", e);
                } else {
                    logger.warn("Failed to get runtime information", e);
                }
            }
        }
    }
    
    private void assertStateChangesPermitted() {
        admin.assertStateChangesPermitted();
    }

}
