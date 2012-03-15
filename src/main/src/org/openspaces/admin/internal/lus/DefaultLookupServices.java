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
package org.openspaces.admin.internal.lus;

import com.j_spaces.kernel.SizeConcurrentHashMap;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.dump.CompoundDumpResult;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.lus.events.DefaultLookupServiceAddedEventManager;
import org.openspaces.admin.internal.lus.events.DefaultLookupServiceRemovedEventManager;
import org.openspaces.admin.internal.lus.events.InternalLookupServiceAddedEventManager;
import org.openspaces.admin.internal.lus.events.InternalLookupServiceRemovedEventManager;
import org.openspaces.admin.lus.LookupService;
import org.openspaces.admin.lus.events.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public class DefaultLookupServices implements InternalLookupServices {

    private final InternalAdmin admin;

    private final Map<String, LookupService> lookupServiceMap = new SizeConcurrentHashMap<String, LookupService>();

    private final InternalLookupServiceAddedEventManager lookupServiceAddedEventManager;

    private final InternalLookupServiceRemovedEventManager lookupServiceRemovedEventManager;

    public DefaultLookupServices(InternalAdmin admin) {
        this.admin = admin;
        this.lookupServiceAddedEventManager = new DefaultLookupServiceAddedEventManager(this);
        this.lookupServiceRemovedEventManager = new DefaultLookupServiceRemovedEventManager(this);
    }

    public Admin getAdmin() {
        return this.admin;
    }

    public LookupService[] getLookupServices() {
        return lookupServiceMap.values().toArray(new InternalLookupService[0]);
    }

    public Iterator<LookupService> iterator() {
        return Collections.unmodifiableCollection(lookupServiceMap.values()).iterator();
    }

    public LookupService getLookupServiceByUID(String id) {
        return lookupServiceMap.get(id);
    }

    public Map<String, LookupService> getUids() {
        return Collections.unmodifiableMap(lookupServiceMap);
    }

    public int getSize() {
        return lookupServiceMap.size();
    }

    public boolean isEmpty() {
        return lookupServiceMap.isEmpty();
    }

    public boolean waitFor(int numberOfLookupServices) {
        return waitFor(numberOfLookupServices, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public boolean waitFor(int numberOfLookupServices, long timeout, TimeUnit timeUnit) {
        if (numberOfLookupServices == 0) {
            final CountDownLatch latch = new CountDownLatch(getSize());
            LookupServiceRemovedEventListener removed = new LookupServiceRemovedEventListener() {
                public void lookupServiceRemoved(LookupService lookupService) {
                    latch.countDown();
                }
            };
            getLookupServiceRemoved().add(removed);
            try {
                return latch.await(timeout, timeUnit);
            } catch (InterruptedException e) {
                return false;
            } finally {
                getLookupServiceRemoved().remove(removed);
            }
        } else {
            final CountDownLatch latch = new CountDownLatch(numberOfLookupServices);
            LookupServiceAddedEventListener added = new LookupServiceAddedEventListener() {
                public void lookupServiceAdded(LookupService lookupService) {
                    latch.countDown();
                }
            };
            getLookupServiceAdded().add(added);
            try {
                return latch.await(timeout, timeUnit);
            } catch (InterruptedException e) {
                return false;
            } finally {
                getLookupServiceAdded().remove(added);
            }
        }
    }

    public void addLifecycleListener(LookupServiceLifecycleEventListener eventListener) {
        getLookupServiceAdded().add(eventListener);
        getLookupServiceRemoved().add(eventListener);
    }

    public void removeLifecycleListener(LookupServiceLifecycleEventListener eventListener) {
        getLookupServiceAdded().remove(eventListener);
        getLookupServiceRemoved().remove(eventListener);
    }

    public void addLookupService(final InternalLookupService lookupService) {
        assertStateChangesPermitted();
        LookupService existingLookupService = lookupServiceMap.put(lookupService.getUid(), lookupService);
        if (existingLookupService == null) {
            lookupServiceAddedEventManager.lookupServiceAdded(lookupService);
        }
    }

    public InternalLookupService removeLookupService(String UID) {
        assertStateChangesPermitted();
        final InternalLookupService existingLookupService = (InternalLookupService) lookupServiceMap.remove(UID);
        if (existingLookupService != null) {
            lookupServiceRemovedEventManager.lookupServiceRemoved(existingLookupService);
        }
        return existingLookupService;
    }

    public LookupServiceAddedEventManager getLookupServiceAdded() {
        return this.lookupServiceAddedEventManager;
    }

    public LookupServiceRemovedEventManager getLookupServiceRemoved() {
        return this.lookupServiceRemovedEventManager;
    }

    public DumpResult generateDump(String cause, Map<String, Object> context) throws AdminException {
        return generateDump(cause, context, (String[]) null);
    }

    public DumpResult generateDump(String cause, Map<String, Object> context, String... processor) throws AdminException {
        CompoundDumpResult dumpResult = new CompoundDumpResult();
        for (LookupService lus : this) {
            dumpResult.add(lus.generateDump(cause, context, processor));
        }
        return dumpResult;
    }
    
    private void assertStateChangesPermitted() {
        admin.assertStateChangesPermitted();
    }

}
