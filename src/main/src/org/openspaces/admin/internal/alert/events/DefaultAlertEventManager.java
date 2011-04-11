package org.openspaces.admin.internal.alert.events;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.events.AlertTriggeredEventListener;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.alert.InternalAlertManager;

public class DefaultAlertEventManager implements InternalAlertTriggeredEventManager {

    private final InternalAlertManager alerts;
    private final InternalAdmin admin;

    private final List<AlertTriggeredEventListener> listeners = new CopyOnWriteArrayList<AlertTriggeredEventListener>();

    public DefaultAlertEventManager(InternalAlertManager alerts) {
        this.alerts = alerts;
        this.admin = (InternalAdmin) alerts.getAdmin();
    }

    public void add(AlertTriggeredEventListener eventListener) {
        add(eventListener, true);
    }

    public void add(final AlertTriggeredEventListener eventListener, boolean includeExisting) {
        System.out.println("DefaultAlertEventManager:28 ---> eventListener= "+eventListener);
        if (includeExisting) {
            admin.raiseEvent(eventListener, new Runnable() {
                public void run() {
                    for (Alert alert : alerts.getAlertRepository().iterateFifo()) {
                        eventListener.alertTriggered(alert);
                        System.out.println("DefaultAlertEventManager:34 ---> alert= "+alert);
                    }
                }
            });
        }
        listeners.add(eventListener);
    }

    public void remove(AlertTriggeredEventListener eventListener) {
        listeners.remove(eventListener);
    }

    public void alertTriggered(final Alert alert) {
        for (final AlertTriggeredEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.alertTriggered(alert);
                }
            });
        }
    }
}
