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
package org.openspaces.admin.internal.machine.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.machine.InternalMachines;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.machine.events.MachineAddedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultMachineAddedEventManager implements InternalMachineAddedEventManager {

    private final InternalMachines machines;

    private final InternalAdmin admin;

    private final List<MachineAddedEventListener> machineAddedEventListeners = new CopyOnWriteArrayList<MachineAddedEventListener>();

    public DefaultMachineAddedEventManager(InternalMachines machines) {
        this.machines = machines;
        this.admin = (InternalAdmin) machines.getAdmin();
    }

    @Override
    public void machineAdded(final Machine machine) {
        for (final MachineAddedEventListener listener : machineAddedEventListeners) {
            admin.pushEventAsFirst(listener, new Runnable() {
                @Override
                public void run() {
                    listener.machineAdded(machine);
                }
            });
        }
    }

    @Override
    public void add(final MachineAddedEventListener eventListener, boolean includeExisting) {
        if (includeExisting) {
            admin.raiseEvent(eventListener, new Runnable() {
                @Override
                public void run() {
                    for (Machine machine : machines.getMachines()) {
                        eventListener.machineAdded(machine);
                    }
                }
            });
        }
        machineAddedEventListeners.add(eventListener);
    }

    @Override
    public void add(final MachineAddedEventListener eventListener) {
        add(eventListener, true);
    }

    @Override
    public void remove(MachineAddedEventListener eventListener) {
        machineAddedEventListeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureMachineAddedEventListener(eventListener));
        } else {
            add((MachineAddedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureMachineAddedEventListener(eventListener));
        } else {
            remove((MachineAddedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
