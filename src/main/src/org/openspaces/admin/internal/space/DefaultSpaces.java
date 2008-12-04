package org.openspaces.admin.internal.space;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.Admin;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.space.events.*;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public class DefaultSpaces implements InternalSpaces {

    private final InternalAdmin admin;

    private final Map<String, Space> spacesByUID = new SizeConcurrentHashMap<String, Space>();

    private final Map<String, Space> spacesByName = new ConcurrentHashMap<String, Space>();

    private final Map<String, SpaceInstance> spacesInstances = new ConcurrentHashMap<String, SpaceInstance>();

    private final InternalSpaceAddedEventManager spaceAddedEventManager;

    private final InternalSpaceRemovedEventManager spaceRemovedEventManager;

    private final InternalSpaceInstanceAddedEventManager spaceInstanceAddedEventManager;

    private final InternalSpaceInstanceRemovedEventManager spaceInstanceRemovedEventManager;

    private final InternalSpaceModeChangedEventManager spaceModeChangedEventManager;

    private final InternalReplicationStatusChangedEventManager replicationStatusChangedEventManager;

    private final InternalSpaceStatisticsChangedEventManager spaceStatisticsChangedEventManager;

    private final InternalSpaceInstanceStatisticsChangedEventManager spaceInstanceStatisticsChangedEventManager;

    private volatile long statisticsInterval = StatisticsMonitor.DEFAULT_MONITOR_INTERVAL;

    private volatile boolean scheduledStatisticsMonitor = false;
    
    public DefaultSpaces(InternalAdmin admin) {
        this.admin = admin;
        this.spaceAddedEventManager = new DefaultSpaceAddedEventManager(this);
        this.spaceRemovedEventManager = new DefaultSpaceRemovedEventManager(this);
        this.spaceInstanceAddedEventManager = new DefaultSpaceInstanceAddedEventManager(admin, this);
        this.spaceInstanceRemovedEventManager = new DefaultSpaceInstanceRemovedEventManager(admin);
        this.spaceModeChangedEventManager =  new DefaultSpaceModeChangedEventManager(admin);
        this.replicationStatusChangedEventManager = new DefaultReplicationStatusChangedEventManager(admin);
        this.spaceStatisticsChangedEventManager = new DefaultSpaceStatisticsChangedEventManager(admin);
        this.spaceInstanceStatisticsChangedEventManager = new DefaultSpaceInstanceStatisticsChangedEventManager(admin);
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public void setStatisticsInterval(long interval, TimeUnit timeUnit) {
        statisticsInterval = timeUnit.toMillis(interval);
        for (Space space : spacesByUID.values()) {
            space.setStatisticsInterval(statisticsInterval, TimeUnit.MILLISECONDS);
        }
    }

    public void startStatisticsMonitor() {
        scheduledStatisticsMonitor = true;
        for (Space space : spacesByUID.values()) {
            space.startStatisticsMonitor();
        }
    }

    public void stopStatisticsMontior() {
        scheduledStatisticsMonitor = false;
        for (Space space : spacesByUID.values()) {
            space.stopStatisticsMontior();
        }
    }

    public boolean isMonitoring() {
        return scheduledStatisticsMonitor;
    }

    public Space[] getSpaces() {
        return spacesByUID.values().toArray(new Space[0]);
    }

    public Space getSpaceByUID(String uid) {
        return spacesByUID.get(uid);
    }

    public Space getSpaceByName(String name) {
        return spacesByName.get(name);
    }

    public Map<String, Space> getNames() {
        return Collections.unmodifiableMap(spacesByName);
    }

    public Iterator<Space> iterator() {
        return spacesByUID.values().iterator();
    }

    public SpaceAddedEventManager getSpaceAdded() {
        return this.spaceAddedEventManager;
    }

    public SpaceRemovedEventManager getSpaceRemoved() {
        return this.spaceRemovedEventManager;
    }

    public SpaceStatisticsChangedEventManager getSpaceStatisticsChanged() {
        return this.spaceStatisticsChangedEventManager;
    }

    public SpaceInstanceStatisticsChangedEventManager getSpaceInstanceStatisticsChanged() {
        return this.spaceInstanceStatisticsChangedEventManager;
    }

    public Space waitFor(String spaceName) {
        return waitFor(spaceName, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public Space waitFor(final String spaceName, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(1);
        getSpaceAdded().add(new SpaceAddedEventListener() {
            public void spaceAdded(Space space) {
                if (space.getName().equals(spaceName)) {
                    latch.countDown();
                }
            }
        });
        boolean result = false;
        try {
            result = latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            return null;
        }
        if (result) {
            Space space = getSpaceByName(spaceName);
            if (space == null) {
                return waitFor(spaceName, timeout, timeUnit);
            } else {
                return space;
            }
        }
        return null;
    }

    public void addLifecycleListener(SpaceLifecycleEventListener eventListener) {
        getSpaceAdded().add(eventListener);
        getSpaceRemoved().add(eventListener);
    }

    public void removeLifecycleListener(SpaceLifecycleEventListener eventListener) {
        getSpaceAdded().remove(eventListener);
        getSpaceRemoved().remove(eventListener);
    }

    public SpaceModeChangedEventManager getSpaceModeChanged() {
        return this.spaceModeChangedEventManager;
    }

    public ReplicationStatusChangedEventManager getReplicationStatusChanged() {
        return this.replicationStatusChangedEventManager;
    }

    public SpaceInstance[] getSpaceInstances() {
        List<SpaceInstance> spaceInstances = new ArrayList<SpaceInstance>();
        for (Space space : this) {
            for (SpaceInstance spaceInstance : space) {
                spaceInstances.add(spaceInstance);
            }
        }
        return spaceInstances.toArray(new SpaceInstance[spaceInstances.size()]);
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

    public synchronized void addSpace(Space space) {
        Space existingSpace = spacesByUID.put(space.getUid(), space);
        spacesByName.put(space.getName(), space);
        if (existingSpace == null) {
            spaceAddedEventManager.spaceAdded(space);
        }
        space.setStatisticsInterval(statisticsInterval, TimeUnit.MILLISECONDS);
        if (scheduledStatisticsMonitor) {
            space.startStatisticsMonitor();
        }
    }

    public synchronized InternalSpace removeSpace(String uid) {
        Space space = spacesByUID.remove(uid);
        if (space != null) {
            space.stopStatisticsMontior();
            spacesByName.remove(space.getName());
            spaceRemovedEventManager.spaceRemoved(space);
        }
        return (InternalSpace) space;
    }

    public void addSpaceInstance(SpaceInstance spaceInstance) {
        spacesInstances.put(spaceInstance.getUid(), spaceInstance);
    }

    public SpaceInstance removeSpaceInstance(String uid) {
        return spacesInstances.remove(uid);
    }

    public void refreshScheduledSpaceMonitors() {
        for (Space space : this) {
            ((InternalSpace) space).refreshScheduledSpaceMonitors();
        }
    }
}
