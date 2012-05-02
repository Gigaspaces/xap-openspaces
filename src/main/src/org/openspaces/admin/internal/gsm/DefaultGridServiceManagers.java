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
package org.openspaces.admin.internal.gsm;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.application.Application;
import org.openspaces.admin.application.ApplicationAlreadyDeployedException;
import org.openspaces.admin.application.ApplicationDeployment;
import org.openspaces.admin.dump.CompoundDumpResult;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.gsm.events.GridServiceManagerAddedEventListener;
import org.openspaces.admin.gsm.events.GridServiceManagerAddedEventManager;
import org.openspaces.admin.gsm.events.GridServiceManagerLifecycleEventListener;
import org.openspaces.admin.gsm.events.GridServiceManagerRemovedEventListener;
import org.openspaces.admin.gsm.events.GridServiceManagerRemovedEventManager;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.gsm.events.DefaultGridServiceManagerAddedEventManager;
import org.openspaces.admin.internal.gsm.events.DefaultGridServiceManagerRemovedEventManager;
import org.openspaces.admin.internal.gsm.events.InternalGridServiceManagerAddedEventManager;
import org.openspaces.admin.internal.gsm.events.InternalGridServiceManagerRemovedEventManager;
import org.openspaces.admin.memcached.MemcachedDeployment;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitAlreadyDeployedException;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.ElasticStatefulProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.ElasticStatelessProcessingUnitDeployment;
import org.openspaces.admin.pu.topology.ProcessingUnitDeploymentTopology;
import org.openspaces.admin.space.ElasticSpaceDeployment;
import org.openspaces.admin.space.SpaceDeployment;

import com.j_spaces.kernel.SizeConcurrentHashMap;

/**
 * @author kimchy
 */
public class DefaultGridServiceManagers implements InternalGridServiceManagers {

    private final InternalAdmin admin;

    private final Map<String, GridServiceManager> gridServiceManagersByUID = new SizeConcurrentHashMap<String, GridServiceManager>();

    private final InternalGridServiceManagerAddedEventManager gridServiceManagerAddedEventManager;

    private final InternalGridServiceManagerRemovedEventManager gridServiceManagerRemovedEventManager;

    public DefaultGridServiceManagers(InternalAdmin admin) {
        this.admin = admin;
        this.gridServiceManagerAddedEventManager = new DefaultGridServiceManagerAddedEventManager(this);
        this.gridServiceManagerRemovedEventManager = new DefaultGridServiceManagerRemovedEventManager(this);
    }

    @Override
    public Admin getAdmin() {
        return this.admin;
    }

    @Override
    public GridServiceManagerAddedEventManager getGridServiceManagerAdded() {
        return this.gridServiceManagerAddedEventManager;
    }

    @Override
    public GridServiceManagerRemovedEventManager getGridServiceManagerRemoved() {
        return this.gridServiceManagerRemovedEventManager;
    }

    @Override
    public GridServiceManager[] getManagers() {
        return gridServiceManagersByUID.values().toArray(new GridServiceManager[0]);
    }

    @Override
    public GridServiceManager getManagerByUID(String uid) {
        return gridServiceManagersByUID.get(uid);
    }

    @Override
    public Map<String, GridServiceManager> getUids() {
        return Collections.unmodifiableMap(gridServiceManagersByUID);
    }

    @Override
    public int getSize() {
        return gridServiceManagersByUID.size();
    }

    @Override
    public boolean isEmpty() {
        return gridServiceManagersByUID.isEmpty();
    }

    @Override
    public GridServiceManager waitForAtLeastOne() {
        return waitForAtLeastOne(admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    @Override
    public GridServiceManager waitForAtLeastOne(long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<GridServiceManager> ref = new AtomicReference<GridServiceManager>();
        GridServiceManagerAddedEventListener added = new GridServiceManagerAddedEventListener() {
            public void gridServiceManagerAdded(GridServiceManager gridServiceManager) {
                ref.set(gridServiceManager);
                latch.countDown();
            }
        };
        getGridServiceManagerAdded().add(added);
        try {
            latch.await(timeout, timeUnit);
            return ref.get();
        } catch (InterruptedException e) {
            return null;
        } finally {
            getGridServiceManagerAdded().remove(added);
        }
    }

    @Override
    public boolean waitFor(int numberOfGridServiceManagers) {
        return waitFor(numberOfGridServiceManagers, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    @Override
    public boolean waitFor(int numberOfGridServiceManagers, long timeout, TimeUnit timeUnit) {
        if (numberOfGridServiceManagers == 0) {
            final CountDownLatch latch = new CountDownLatch(getSize());
            GridServiceManagerRemovedEventListener removed = new GridServiceManagerRemovedEventListener() {
                public void gridServiceManagerRemoved(GridServiceManager gridServiceManager) {
                    latch.countDown();
                }
            };
            getGridServiceManagerRemoved().add(removed);
            try {
                return latch.await(timeout, timeUnit);
            } catch (InterruptedException e) {
                return false;
            } finally {
                getGridServiceManagerRemoved().remove(removed);
            }
        } else {
            final CountDownLatch latch = new CountDownLatch(numberOfGridServiceManagers);
            GridServiceManagerAddedEventListener added = new GridServiceManagerAddedEventListener() {
                public void gridServiceManagerAdded(GridServiceManager gridServiceManager) {
                    latch.countDown();
                }
            };
            getGridServiceManagerAdded().add(added);
            try {
                return latch.await(timeout, timeUnit);
            } catch (InterruptedException e) {
                return false;
            } finally {
                getGridServiceManagerAdded().remove(added);
            }
        }
    }

    @Override
    public ProcessingUnit deploy(ProcessingUnitDeployment deployment) {
        GridServiceManager gridServiceManager = getGridServiceManager();
        if (gridServiceManager == null) {
            throw new AdminException("No Grid Service Manager found to deploy [" + deployment.getProcessingUnit() + "]");
        }
        return gridServiceManager.deploy(deployment);
    }

    @Override
    public ProcessingUnit deploy(ProcessingUnitDeployment deployment, long timeout, TimeUnit timeUnit) {
        GridServiceManager gridServiceManager = getGridServiceManager();
        if (gridServiceManager == null) {
            throw new AdminException("No Grid Service Manager found to deploy [" + deployment.getProcessingUnit() + "]");
        }
        return gridServiceManager.deploy(deployment, timeout, timeUnit);
    }

    @Override
    public ProcessingUnit deploy(SpaceDeployment deployment) {
        GridServiceManager gridServiceManager = getGridServiceManager();
        if (gridServiceManager == null) {
            throw new AdminException("No Grid Service Manager found to deploy [" + deployment.getSpaceName() + "]");
        }
        return gridServiceManager.deploy(deployment);
    }

    @Override
    public ProcessingUnit deploy(SpaceDeployment deployment, long timeout, TimeUnit timeUnit) {
        GridServiceManager gridServiceManager = getGridServiceManager();
        if (gridServiceManager == null) {
            throw new AdminException("No Grid Service Manager found to deploy [" + deployment.getSpaceName() + "]");
        }
        return gridServiceManager.deploy(deployment, timeout, timeUnit);
    }

    @Override
    public ProcessingUnit deploy(MemcachedDeployment deployment, long timeout, TimeUnit timeUnit) {
        GridServiceManager gridServiceManager = getGridServiceManager();
        if (gridServiceManager == null) {
            throw new AdminException("No Grid Service Manager found to deploy [" + deployment.getSpaceUrl() + "]");
        }
        return gridServiceManager.deploy(deployment, timeout, timeUnit);
    }

    @Override
    public ProcessingUnit deploy(MemcachedDeployment deployment) {
        GridServiceManager gridServiceManager = getGridServiceManager();
        if (gridServiceManager == null) {
            throw new AdminException("No Grid Service Manager found to deploy [" + deployment.getSpaceUrl() + "]");
        }
        return gridServiceManager.deploy(deployment);
    }

    @Override
    public ProcessingUnit deploy(ElasticSpaceDeployment deployment) throws ProcessingUnitAlreadyDeployedException {
        GridServiceManager gridServiceManager = getGridServiceManager();
        if (gridServiceManager == null) {
            throw new AdminException("No Grid Service Manager found to deploy [" + deployment.create().getProcessingUnit() + "]");
        }
        return gridServiceManager.deploy(deployment);
    }

    @Override
    public ProcessingUnit deploy(ElasticSpaceDeployment deployment, long timeout, TimeUnit timeUnit)
            throws ProcessingUnitAlreadyDeployedException {
        GridServiceManager gridServiceManager = getGridServiceManager();
        if (gridServiceManager == null) {
            throw new AdminException("No Grid Service Manager found to deploy [" + deployment.create().getProcessingUnit() + "]");
        }
        return gridServiceManager.deploy(deployment, timeout, timeUnit);
    }

    @Override
    public ProcessingUnit deploy(ElasticStatefulProcessingUnitDeployment deployment)
            throws ProcessingUnitAlreadyDeployedException {
        return deploy(deployment,admin.getDefaultTimeout(),admin.getDefaultTimeoutTimeUnit());
    }

    @Override
    public ProcessingUnit deploy(ElasticStatefulProcessingUnitDeployment deployment, long timeout, TimeUnit timeUnit)
            throws ProcessingUnitAlreadyDeployedException {
        
        GridServiceManager gridServiceManager = getGridServiceManager();
        if (gridServiceManager == null) {
            throw new AdminException("No Grid Service Manager found to deploy [" + deployment.create().getProcessingUnit() + "]");
        }
        return gridServiceManager.deploy(deployment, timeout, timeUnit);
    }

    @Override
    public ProcessingUnit deploy(ElasticStatelessProcessingUnitDeployment deployment)
        throws ProcessingUnitAlreadyDeployedException {
    
        return deploy(deployment,admin.getDefaultTimeout(),admin.getDefaultTimeoutTimeUnit());
    }

    @Override
    public ProcessingUnit deploy(ElasticStatelessProcessingUnitDeployment deployment, long timeout, TimeUnit timeUnit)
        throws ProcessingUnitAlreadyDeployedException {

        return deploy(deployment.toProcessingUnitDeployment(admin),timeout,timeUnit);
    }

    @Override
    public Application deploy(ApplicationDeployment deployment) throws ApplicationAlreadyDeployedException, ProcessingUnitAlreadyDeployedException {
        return deploy(deployment, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    @Override
    public Application deploy(ApplicationDeployment deployment, long timeout, TimeUnit timeUnit) throws ApplicationAlreadyDeployedException, ProcessingUnitAlreadyDeployedException {
        GridServiceManager gridServiceManager = getGridServiceManager();
        if (gridServiceManager == null) {
            throw new AdminException("No Grid Service Manager found to deploy [" + deployment.create().getApplicationName() + "]");
        }
        return gridServiceManager.deploy(deployment, timeout, timeUnit);
    }
    
    private GridServiceManager getGridServiceManager() {
        Iterator<GridServiceManager> it = iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    @Override
    public void addLifecycleListener(GridServiceManagerLifecycleEventListener eventListener) {
        getGridServiceManagerAdded().add(eventListener);
        getGridServiceManagerRemoved().add(eventListener);
    }

    @Override
    public void removeLifecycleListener(GridServiceManagerLifecycleEventListener eventListener) {
        getGridServiceManagerAdded().remove(eventListener);
        getGridServiceManagerRemoved().remove(eventListener);
    }

    @Override
    public Iterator<GridServiceManager> iterator() {
        return Collections.unmodifiableCollection(gridServiceManagersByUID.values()).iterator();
    }

    @Override
    public void addGridServiceManager(final InternalGridServiceManager gridServiceManager) {
        assertStateChangePermitted();
        GridServiceManager existingGSM = gridServiceManagersByUID.put(gridServiceManager.getUid(), gridServiceManager);
        if (existingGSM == null) {
            gridServiceManagerAddedEventManager.gridServiceManagerAdded(gridServiceManager);
        }
    }

    @Override
    public InternalGridServiceManager removeGridServiceManager(String uid) {
        assertStateChangePermitted();
        final InternalGridServiceManager existingGSM = (InternalGridServiceManager) gridServiceManagersByUID.remove(uid);
        if (existingGSM != null) {
            gridServiceManagerRemovedEventManager.gridServiceManagerRemoved(existingGSM);
        }
        return existingGSM;
    }

    @Override
    public InternalGridServiceManager replaceGridServiceManager(InternalGridServiceManager gridServiceManager) {
        assertStateChangePermitted();
        return (InternalGridServiceManager) gridServiceManagersByUID.put(gridServiceManager.getUid(), gridServiceManager);
    }

    @Override
    public DumpResult generateDump(String cause, Map<String, Object> context) throws AdminException {
        return generateDump(cause, context, (String[]) null);
    }

    @Override
    public DumpResult generateDump(String cause, Map<String, Object> context, String... processor) throws AdminException {
        CompoundDumpResult dumpResult = new CompoundDumpResult();
        for (GridServiceManager gsm : this) {
            dumpResult.add(gsm.generateDump(cause, context, processor));
        }
        return dumpResult;
    }
    

    private void assertStateChangePermitted() {
        this.admin.assertStateChangesPermitted();
    }

    @Override
    public boolean undeployProcessingUnitsAndWait(ProcessingUnit[] processingUnits, long timeout, TimeUnit timeUnit) {
        if (processingUnits.length == 0) {
            return true;
        }

        long end = System.currentTimeMillis() + timeUnit.toMillis(timeout);
        InternalGridServiceManager gridServiceManager = (InternalGridServiceManager) waitForAtLeastOne(timeout, timeUnit);
        if (gridServiceManager == null) {
            throw new AdminException("No Grid Service Manager found to undeploy [" + processingUnits[0].getName() + "]");
        }
        long remaining = Math.max(0, end - System.currentTimeMillis());
        return gridServiceManager.undeployProcessingUnitsAndWait(processingUnits, remaining, TimeUnit.MILLISECONDS);
    }

    @Override
    public ProcessingUnit deploy(Application application, ProcessingUnitDeploymentTopology puDeploymentTopology, long timeout, TimeUnit timeUnit) {
        InternalGridServiceManager gridServiceManager = (InternalGridServiceManager)getGridServiceManager();
        if (gridServiceManager == null) {
            throw new AdminException("No Grid Service Manager found to deploy [" + puDeploymentTopology.create().toProcessingUnitConfig(admin).getProcessingUnit() + "] to application [" + application.getName() +"]");
        }
        return gridServiceManager.deploy(application, puDeploymentTopology, timeout, timeUnit);
    }
}
