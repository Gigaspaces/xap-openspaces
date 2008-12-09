package org.openspaces.admin.internal.gsc;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.Admin;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.gsc.events.GridServiceContainerAddedEventListener;
import org.openspaces.admin.gsc.events.GridServiceContainerAddedEventManager;
import org.openspaces.admin.gsc.events.GridServiceContainerLifecycleEventListener;
import org.openspaces.admin.gsc.events.GridServiceContainerRemovedEventManager;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsc.events.DefaultGridServiceContainerAddedEventManager;
import org.openspaces.admin.internal.gsc.events.DefaultGridServiceContainerRemovedEventManager;
import org.openspaces.admin.internal.gsc.events.InternalGridServiceContainerAddedEventManager;
import org.openspaces.admin.internal.gsc.events.InternalGridServiceContainerRemovedEventManager;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public class DefaultGridServiceContainers implements InternalGridServiceContainers {

    private final InternalAdmin admin;

    private final Map<String, GridServiceContainer> containers = new SizeConcurrentHashMap<String, GridServiceContainer>();

    private final InternalGridServiceContainerAddedEventManager gridServiceContainerAddedEventManager;

    private final InternalGridServiceContainerRemovedEventManager gridServiceContainerRemovedEventManager;

    public DefaultGridServiceContainers(InternalAdmin admin) {
        this.admin = admin;
        this.gridServiceContainerAddedEventManager = new DefaultGridServiceContainerAddedEventManager(this);
        this.gridServiceContainerRemovedEventManager = new DefaultGridServiceContainerRemovedEventManager(this);
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public GridServiceContainerAddedEventManager getGridServiceContainerAdded() {
        return this.gridServiceContainerAddedEventManager;
    }

    public GridServiceContainerRemovedEventManager getGridServiceContainerRemoved() {
        return this.gridServiceContainerRemovedEventManager;
    }

    public GridServiceContainer[] getContainers() {
        return containers.values().toArray(new GridServiceContainer[0]);
    }

    public GridServiceContainer getContainerByUID(String uid) {
        return containers.get(uid);
    }

    public Map<String, GridServiceContainer> getUids() {
        return Collections.unmodifiableMap(containers);
    }

    public int getSize() {
        return containers.size();
    }

    public boolean isEmpty() {
        return containers.size() == 0;
    }

    public boolean waitFor(int numberOfGridServiceContainers) {
        return waitFor(numberOfGridServiceContainers, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public boolean waitFor(int numberOfGridServiceContainers, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(numberOfGridServiceContainers);
        GridServiceContainerAddedEventListener added = new GridServiceContainerAddedEventListener() {
            public void gridServiceContainerAdded(GridServiceContainer gridServiceContainer) {
                latch.countDown();
            }
        };
        getGridServiceContainerAdded().add(added);
        try {
            return latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            return false;
        } finally {
            getGridServiceContainerAdded().remove(added);
        }
    }
    
    public void addLifecycleListener(GridServiceContainerLifecycleEventListener eventListener) {
        getGridServiceContainerAdded().add(eventListener);
        getGridServiceContainerRemoved().add(eventListener);
    }

    public void removeLifecycleListener(GridServiceContainerLifecycleEventListener eventListener) {
        getGridServiceContainerAdded().remove(eventListener);
        getGridServiceContainerRemoved().remove(eventListener);
    }

    public Iterator<GridServiceContainer> iterator() {
        return containers.values().iterator();
    }

    public void addGridServiceContainer(final InternalGridServiceContainer gridServiceContainer) {
        final GridServiceContainer existingGSC = containers.put(gridServiceContainer.getUid(), gridServiceContainer);
        if (existingGSC == null) {
            gridServiceContainerAddedEventManager.gridServiceContainerAdded(gridServiceContainer);
        }
    }

    public InternalGridServiceContainer removeGridServiceContainer(String uid) {
        final InternalGridServiceContainer existingGSC = (InternalGridServiceContainer) containers.remove(uid);
        if (existingGSC != null) {
            gridServiceContainerRemovedEventManager.gridServiceContainerRemoved(existingGSC);
        }
        return existingGSC;
    }
}