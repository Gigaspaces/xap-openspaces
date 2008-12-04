package org.openspaces.admin.internal.pu;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.events.*;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public class DefaultProcessingUnits implements InternalProcessingUnits {

    private final InternalAdmin admin;

    private final Map<String, ProcessingUnit> processingUnits = new SizeConcurrentHashMap<String, ProcessingUnit>();

    private final InternalProcessingUnitAddedEventManager processingUnitAddedEventManager;

    private final InternalProcessingUnitRemovedEventManager processingUnitRemovedEventManager;

    private final InternalManagingGridServiceManagerChangedEventManager managingGridServiceManagerChangedEventManager;

    private final InternalBackupGridServiceManagerChangedEventManager backupGridServiceManagerChangedEventManager;

    private final InternalProcessingUnitStatusChangedEventManager processingUnitStatusChangedEventManager;

    private final InternalProcessingUnitInstanceAddedEventManager processingUnitInstanceAddedEventManager;

    private final InternalProcessingUnitInstanceRemovedEventManager processingUnitInstanceRemovedEventManager;

    public DefaultProcessingUnits(InternalAdmin admin) {
        this.admin = admin;
        this.processingUnitAddedEventManager = new DefaultProcessingUnitAddedEventManager(this);
        this.processingUnitRemovedEventManager = new DefaultProcessingUnitRemovedEventManager(this);

        this.managingGridServiceManagerChangedEventManager = new DefaultManagingGridServiceManagerChangedEventManager(admin);
        this.backupGridServiceManagerChangedEventManager = new DefaultBackupGridServiceManagerChangedEventManager(admin);
        this.processingUnitStatusChangedEventManager = new DefaultProcessingUnitStatusChangedEventManager(admin);

        this.processingUnitInstanceAddedEventManager = new DefaultProcessingUnitInstanceAddedEventManager(this, admin);
        this.processingUnitInstanceRemovedEventManager = new DefaultProcessingUnitInstanceRemovedEventManager(admin);
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public ProcessingUnitAddedEventManager getProcessingUnitAdded() {
        return this.processingUnitAddedEventManager;
    }

    public ProcessingUnitRemovedEventManager getProcessingUnitRemoved() {
        return this.processingUnitRemovedEventManager;
    }

    public ManagingGridServiceManagerChangedEventManager getManagingGridServiceManagerChanged() {
        return this.managingGridServiceManagerChangedEventManager;
    }

    public BackupGridServiceManagerChangedEventManager getBackupGridServiceManagerChanged() {
        return this.backupGridServiceManagerChangedEventManager;
    }

    public ProcessingUnitStatusChangedEventManager getProcessingUnitStatusChanged() {
        return this.processingUnitStatusChangedEventManager;
    }

    public ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded() {
        return this.processingUnitInstanceAddedEventManager;
    }

    public ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved() {
        return this.processingUnitInstanceRemovedEventManager;
    }

    public Iterator<ProcessingUnit> iterator() {
        return processingUnits.values().iterator();
    }

    public ProcessingUnit[] getProcessingUnits() {
        return processingUnits.values().toArray(new ProcessingUnit[0]);
    }

    public ProcessingUnitInstance[] getProcessingUnitInstances() {
        List<ProcessingUnitInstance> processingUnitInstances = new ArrayList<ProcessingUnitInstance>();
        for (ProcessingUnit processingUnit : this) {
            for (ProcessingUnitInstance processingUnitInstance : processingUnit) {
                processingUnitInstances.add(processingUnitInstance);
            }
        }
        return processingUnitInstances.toArray(new ProcessingUnitInstance[processingUnitInstances.size()]);
    }

    public ProcessingUnit getProcessingUnit(String name) {
        return processingUnits.get(name);
    }

    public Map<String, ProcessingUnit> getNames() {
        return Collections.unmodifiableMap(processingUnits);
    }

    public int getSize() {
        return processingUnits.size();
    }

    public boolean isEmpty() {
        return processingUnits.size() == 0;
    }

    public ProcessingUnit waitFor(String processingUnitName) {
        return waitFor(processingUnitName, Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }

    public ProcessingUnit waitFor(final String processingUnitName, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(1);
        ProcessingUnitAddedEventListener added = new ProcessingUnitAddedEventListener() {
            public void processingUnitAdded(ProcessingUnit processingUnit) {
                if (processingUnitName.equals(processingUnit.getName())) {
                    latch.countDown();
                }
            }
        };
        getProcessingUnitAdded().add(added);
        boolean result = false;
        try {
            result = latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            return null;
        } finally {
            getProcessingUnitAdded().remove(added);
        }

        if (result) {
            ProcessingUnit processingUnit = getProcessingUnit(processingUnitName);
            if (processingUnit == null) {
                return waitFor(processingUnitName, timeout, timeUnit);
            } else {
                return processingUnit;
            }
        }
        return null;
    }

    public void addLifecycleListener(ProcessingUnitLifecycleEventListener eventListener) {
        getProcessingUnitAdded().add(eventListener);
        getProcessingUnitRemoved().add(eventListener);
        getProcessingUnitStatusChanged().add(eventListener);
        getManagingGridServiceManagerChanged().add(eventListener);
        getBackupGridServiceManagerChanged().add(eventListener);
    }

    public void removeLifecycleListener(ProcessingUnitLifecycleEventListener eventListener) {
        getProcessingUnitAdded().remove(eventListener);
        getProcessingUnitRemoved().remove(eventListener);
        getProcessingUnitStatusChanged().remove(eventListener);
        getManagingGridServiceManagerChanged().remove(eventListener);
        getBackupGridServiceManagerChanged().remove(eventListener);
    }

    public void addLifecycleListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        getProcessingUnitInstanceAdded().add(eventListener);
        getProcessingUnitInstanceRemoved().add(eventListener);
    }

    public void removeLifecycleListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        getProcessingUnitInstanceAdded().remove(eventListener);
        getProcessingUnitInstanceRemoved().remove(eventListener);
    }

    public void addProcessingUnit(final ProcessingUnit processingUnit) {
        ProcessingUnit existingProcessingUnit = processingUnits.put(processingUnit.getName(), processingUnit);
        if (existingProcessingUnit == null) {
            processingUnitAddedEventManager.processingUnitAdded(processingUnit);
        }
    }

    public void removeProcessingUnit(String name) {
        final ProcessingUnit existingProcessingUnit = processingUnits.remove(name);
        if (existingProcessingUnit != null) {
            processingUnitRemovedEventManager.processingUnitRemoved(existingProcessingUnit);
        }
    }
}
