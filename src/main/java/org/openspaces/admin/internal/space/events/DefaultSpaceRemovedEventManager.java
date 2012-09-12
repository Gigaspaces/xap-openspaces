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
package org.openspaces.admin.internal.space.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.space.InternalSpaces;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.events.SpaceRemovedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultSpaceRemovedEventManager implements InternalSpaceRemovedEventManager {

    private final InternalSpaces spaces;

    private final InternalAdmin admin;

    private final List<SpaceRemovedEventListener> listeners = new CopyOnWriteArrayList<SpaceRemovedEventListener>();

    public DefaultSpaceRemovedEventManager(InternalSpaces spaces) {
        this.spaces = spaces;
        this.admin = (InternalAdmin) spaces.getAdmin();
    }

    public void spaceRemoved(final Space space) {
        for (final SpaceRemovedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.spaceRemoved(space);
                }
            });
        }
    }

    public void add(final SpaceRemovedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(SpaceRemovedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureSpaceRemovedEventListener(eventListener));
        } else {
            add((SpaceRemovedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureSpaceRemovedEventListener(eventListener));
        } else {
            remove((SpaceRemovedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
