package org.openspaces.admin.internal.application.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openspaces.admin.application.Application;
import org.openspaces.admin.application.events.ApplicationAddedEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.application.InternalApplications;
import org.openspaces.admin.internal.support.GroovyHelper;

public class DefaultApplicationAddedEventManager implements InternalApplicationAddedEventManager {

    private final InternalApplications applications;

    private final InternalAdmin admin;

    private final List<ApplicationAddedEventListener> applicationAddedEventListeners = new CopyOnWriteArrayList<ApplicationAddedEventListener>();

    public DefaultApplicationAddedEventManager(InternalApplications applications) {
        this.applications = applications;
        this.admin = (InternalAdmin) applications.getAdmin();
    }

    @Override
    public void applicationAdded(final Application application) {
        for (final ApplicationAddedEventListener listener : applicationAddedEventListeners) {
            admin.pushEvent(listener, new Runnable() {
                @Override
                public void run() {
                    listener.applicationAdded(application);
                }
            });
        }
    }

    public void add(final ApplicationAddedEventListener eventListener, boolean includeExisting) {
        if (includeExisting) {
            admin.raiseEvent(eventListener, new Runnable() {
                @Override
                public void run() {
                    for (Application application : applications) {
                        eventListener.applicationAdded(application);
                    }
                }
            });
        }
        applicationAddedEventListeners.add(eventListener);
    }

    @Override
    public void add(final ApplicationAddedEventListener  eventListener) {
        add(eventListener, true);
    }

    @Override
    public void remove(ApplicationAddedEventListener eventListener) {
        applicationAddedEventListeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureApplicationAddedEventListener(eventListener));
        } else {
            add((ApplicationAddedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureApplicationAddedEventListener(eventListener));
        } else {
            remove((ApplicationAddedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }

}
