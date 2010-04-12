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

package org.openspaces.admin.pu;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.StatisticsMonitor;
import org.openspaces.admin.pu.events.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Holds one or more {@link org.openspaces.admin.pu.ProcessingUnit}s.
 *
 * @author kimchy
 */
public interface ProcessingUnits extends Iterable<ProcessingUnit>, AdminAware, StatisticsMonitor {

    /**
     * Returns the number of currently deployed {@link org.openspaces.admin.pu.ProcessingUnit}s.
     */
    int getSize();

    /**
     * Returns <code>true</code> if there are no currently deployed processing units.
     */
    boolean isEmpty();

    /**
     * Returns the {@link org.openspaces.admin.pu.ProcessingUnit}s currently deployed.
     */
    ProcessingUnit[] getProcessingUnits();

    /**
     * Returns the {@link org.openspaces.admin.pu.ProcessingUnit} for the given processing unit name.
     */
    ProcessingUnit getProcessingUnit(String name);

    /**
     * Returns a map of {@link org.openspaces.admin.pu.ProcessingUnit} keyed by their respective names.
     */
    Map<String, ProcessingUnit> getNames();

    /**
     * Waits indefinitely till the processing unit is identified as deployed. Returns the
     * {@link org.openspaces.admin.pu.ProcessingUnit}.
     */
    ProcessingUnit waitFor(String processingUnitName);

    /**
     * Waits for the specified timeout (in time interval) till the processing unit is identified as deployed. Returns the
     * {@link org.openspaces.admin.pu.ProcessingUnit}. Return <code>null</code> if the processing unit is not deployed
     * within the specified timeout.
     */
    ProcessingUnit waitFor(String processingUnitName, long timeout, TimeUnit timeUnit);

    /**
     * Returns an event manager allowing to register {@link org.openspaces.admin.pu.events.ProcessingUnitAddedEventListener}s.
     */
    ProcessingUnitAddedEventManager getProcessingUnitAdded();

    /**
     * Returns an event manager allowing to register {@link org.openspaces.admin.pu.events.ProcessingUnitRemovedEventListener}s.
     */
    ProcessingUnitRemovedEventManager getProcessingUnitRemoved();

    /**
     * Allows to add a {@link ProcessingUnitLifecycleEventListener}.
     */
    void addLifecycleListener(ProcessingUnitLifecycleEventListener eventListener);

    /**
     * Allows to remove a {@link ProcessingUnitLifecycleEventListener}.
     */
    void removeLifecycleListener(ProcessingUnitLifecycleEventListener eventListener);

    /**
     * Returns an event manager allowing to register {@link org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener}s.
     */
    ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded();

    /**
     * Returns an event manager allowing to register {@link org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventListener}s.
     */
    ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved();

    /**
     * Allows to add a {@link ProcessingUnitInstanceLifecycleEventListener}.
     */
    void addLifecycleListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    /**
     * Allows to remove a {@link ProcessingUnitInstanceLifecycleEventListener}.
     */
    void removeLifecycleListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    /**
     * Returns an event manager allowing to globally register for any {@link org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEvent}s
     * occurring on any processing unit.
     */
    ManagingGridServiceManagerChangedEventManager getManagingGridServiceManagerChanged();

    /**
     * Returns an event manager allowing to globally register for any {@link org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEvent}s
     * occurring on any processing unit.
     */
    BackupGridServiceManagerChangedEventManager getBackupGridServiceManagerChanged();

    /**
     * Returns an event manager allowing to globally register for any {@link org.openspaces.admin.pu.events.ProcessingUnitSpaceCorrelatedEvent}s
     * occurring on any processing unit.
     */
    ProcessingUnitStatusChangedEventManager getProcessingUnitStatusChanged();

    /**
     * Returns a processing unit instance statistics change event manger allowing to register for
     * events of {@link org.openspaces.admin.pu.events.ProcessingUnitInstanceStatisticsChangedEvent}.
     *
     * <p>Note, in order to receive events, the virtual machines need to be in a "statistics" monitored
     * state.
     */
    ProcessingUnitInstanceStatisticsChangedEventManager getProcessingUnitInstanceStatisticsChanged();
}
