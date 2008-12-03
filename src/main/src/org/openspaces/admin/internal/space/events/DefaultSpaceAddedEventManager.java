package org.openspaces.admin.internal.space.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.space.InternalSpaces;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.space.Space;
import org.openspaces.admin.space.events.SpaceAddedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultSpaceAddedEventManager implements InternalSpaceAddedEventManager {

    private final InternalSpaces spaces;

    private final InternalAdmin admin;

    private final List<SpaceAddedEventListener> listeners = new CopyOnWriteArrayList<SpaceAddedEventListener>();

    public DefaultSpaceAddedEventManager(InternalSpaces spaces) {
        this.spaces = spaces;
        this.admin = (InternalAdmin) spaces.getAdmin();
    }

    public void spaceAdded(final Space space) {
        for (final SpaceAddedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.spaceAdded(space);
                }
            });
        }
    }

    public void add(final SpaceAddedEventListener eventListener) {
        admin.raiseEvent(eventListener, new Runnable() {
            public void run() {
                for (Space space : spaces) {
                    eventListener.spaceAdded(space);
                }
            }
        });
        listeners.add(eventListener);
    }

    public void remove(SpaceAddedEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureSpaceAddedEventListener(eventListener));
        } else {
            add((SpaceAddedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureSpaceAddedEventListener(eventListener));
        } else {
            remove((SpaceAddedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}