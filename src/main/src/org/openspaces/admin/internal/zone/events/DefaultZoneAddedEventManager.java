package org.openspaces.admin.internal.zone.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.internal.zone.InternalZones;
import org.openspaces.admin.zone.events.ZoneAddedEventListener;
import org.openspaces.admin.zone.Zone;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultZoneAddedEventManager implements InternalZoneAddedEventManager {

    private final InternalZones zones;

    private final InternalAdmin admin;

    private final List<ZoneAddedEventListener> zoneAddedEventListeners = new CopyOnWriteArrayList<ZoneAddedEventListener>();

    public DefaultZoneAddedEventManager(InternalZones zones) {
        this.zones = zones;
        this.admin = (InternalAdmin) zones.getAdmin();
    }

    public void zoneAdded(final Zone zone) {
        for (final ZoneAddedEventListener listener : zoneAddedEventListeners) {
            admin.pushEventAsFirst(listener, new Runnable() {
                public void run() {
                    listener.zoneAdded(zone);
                }
            });
        }
    }

    public void add(final ZoneAddedEventListener eventListener) {
        admin.raiseEvent(eventListener, new Runnable() {
            public void run() {
                for (Zone zone : zones.getNames().values()) {
                    eventListener.zoneAdded(zone);
                }
            }
        });
        zoneAddedEventListeners.add(eventListener);
    }

    public void remove(ZoneAddedEventListener eventListener) {
        zoneAddedEventListeners.remove(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureZoneAddedEventListener(eventListener));
        } else {
            add((ZoneAddedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureZoneAddedEventListener(eventListener));
        } else {
            remove((ZoneAddedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}