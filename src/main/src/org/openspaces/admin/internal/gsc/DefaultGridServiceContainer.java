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
package org.openspaces.admin.internal.gsc;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import net.jini.core.lookup.ServiceID;

import org.jini.rio.core.provision.ServiceRecord;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.dump.DumpResult;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.dump.InternalDumpResult;
import org.openspaces.admin.internal.pu.DefaultProcessingUnitInstances;
import org.openspaces.admin.internal.pu.InternalProcessingUnitInstances;
import org.openspaces.admin.internal.support.AbstractAgentGridComponent;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;

import com.gigaspaces.grid.gsc.GSC;
import com.gigaspaces.internal.jvm.JVMDetails;
import com.gigaspaces.internal.jvm.JVMStatistics;
import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.internal.os.OSStatistics;
import com.gigaspaces.log.LogEntries;
import com.gigaspaces.log.LogEntryMatcher;
import com.gigaspaces.log.LogProcessType;
import com.gigaspaces.lrmi.LRMIServiceMonitoringDetails;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import com.gigaspaces.security.SecurityException;

/**
 * @author kimchy
 */
public class DefaultGridServiceContainer extends AbstractAgentGridComponent implements InternalGridServiceContainer {

    private final ServiceID serviceID;

    private final GSC gsc;

    private final InternalProcessingUnitInstances processingUnitInstances;

    public DefaultGridServiceContainer(ServiceID serviceID, GSC gsc, InternalAdmin admin, int agentId, String agentUid) {
        super(admin, agentId, agentUid);
        this.serviceID = serviceID;
        this.gsc = gsc;
        this.processingUnitInstances = new DefaultProcessingUnitInstances(admin);
    }

    public String getUid() {
        return serviceID.toString();
    }

    public ServiceID getServiceID() {
        return this.serviceID;
    }

    public GSC getGSC() {
        return this.gsc;
    }

    public Iterator<ProcessingUnitInstance> iterator() {
        return processingUnitInstances.getInstancesIt();
    }

    public ProcessingUnitInstance[] getProcessingUnitInstances() {
        return processingUnitInstances.getInstances();
    }

    public ProcessingUnitInstance[] getProcessingUnitInstances(String processingUnitName) {
        return processingUnitInstances.getInstances(processingUnitName);
    }
    
    public ProcessingUnitInstance getProcessingUnitInstanceByUID(String processingUnitInstanceUid) {
        ProcessingUnitInstance processingUnitInstance = 
                      processingUnitInstances.getInstanceByUID(processingUnitInstanceUid);
        return processingUnitInstance;
    }

    public boolean contains(ProcessingUnitInstance processingUnitInstance) {
        return processingUnitInstances.contains(processingUnitInstance);
    }

    public boolean waitFor(int numberOfProcessingUnitInstances) {
        return waitFor(numberOfProcessingUnitInstances, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public boolean waitFor(int numberOfProcessingUnitInstances, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(numberOfProcessingUnitInstances);
        ProcessingUnitInstanceAddedEventListener added = new ProcessingUnitInstanceAddedEventListener() {
            public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
                latch.countDown();
            }
        };
        getProcessingUnitInstanceAdded().add(added);
        try {
            return latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            return false;
        } finally {
            getProcessingUnitInstanceAdded().remove(added);
        }
    }

    public boolean waitFor(String processingUnitName, int numberOfProcessingUnitInstances) {
        return waitFor(processingUnitName, numberOfProcessingUnitInstances, admin.getDefaultTimeout(), admin.getDefaultTimeoutTimeUnit());
    }

    public boolean waitFor(final String processingUnitName, int numberOfProcessingUnitInstances, long timeout, TimeUnit timeUnit) {
        final CountDownLatch latch = new CountDownLatch(numberOfProcessingUnitInstances);
        ProcessingUnitInstanceAddedEventListener added = new ProcessingUnitInstanceAddedEventListener() {
            public void processingUnitInstanceAdded(ProcessingUnitInstance processingUnitInstance) {
                if (processingUnitInstance.getName().equals(processingUnitName)) {
                    latch.countDown();
                }
            }
        };
        getProcessingUnitInstanceAdded().add(added);
        try {
            return latch.await(timeout, timeUnit);
        } catch (InterruptedException e) {
            return false;
        } finally {
            getProcessingUnitInstanceAdded().remove(added);
        }
    }

    public void addProcessingUnitInstance(ProcessingUnitInstance processingUnitInstance) {
        
        assertStateChangesPermitted();
        processingUnitInstances.addInstance(processingUnitInstance);
    }

    public void removeProcessingUnitInstance(String uid) {
        
        assertStateChangesPermitted();
        processingUnitInstances.removeInstance(uid);
    }

    public ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded() {
        return processingUnitInstances.getProcessingUnitInstanceAdded();
    }

    public ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved() {
        return processingUnitInstances.getProcessingUnitInstanceRemoved();
    }

    public void addProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        processingUnitInstances.addProcessingUnitInstanceLifecycleEventListener(eventListener);
    }

    public void removeProcessingUnitInstanceLifecycleEventListener(ProcessingUnitInstanceLifecycleEventListener eventListener) {
        processingUnitInstances.removeProcessingUnitInstanceLifecycleEventListener(eventListener);
    }

    public LogEntries logEntries(LogEntryMatcher matcher) throws AdminException {
        if (getGridServiceAgent() != null) {
            return getGridServiceAgent().logEntries(LogProcessType.GSC, getVirtualMachine().getDetails().getPid(), matcher);
        }
        return logEntriesDirect(matcher);
    }

    public LogEntries logEntriesDirect(LogEntryMatcher matcher) throws AdminException {
        try {
            return gsc.logEntriesDirect(matcher);
        } catch (IOException e) {
            throw new AdminException("Failed to get log", e);
        }
    }

    public DumpResult generateDump(String cause, Map<String, Object> context) throws AdminException {
        try {
            return new InternalDumpResult(this, gsc, gsc.generateDump(cause, context));
        } catch (Exception e) {
            throw new AdminException("Failed to generate dump", e);
        }
    }

    public DumpResult generateDump(String cause, Map<String, Object> context, String... processor) throws AdminException {
        try {
            return new InternalDumpResult(this, gsc, gsc.generateDump(cause, context, processor));
        } catch (Exception e) {
            throw new AdminException("Failed to generate dump", e);
        }
    }

    public NIODetails getNIODetails() throws RemoteException {
        return gsc.getNIODetails();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        return gsc.getNIOStatistics();
    }
    
    @Override
    public void enableLRMIMonitoring() throws RemoteException {
        gsc.enableLRMIMonitoring();
    }
    
    @Override
    public void disableLRMIMonitoring() throws RemoteException {
        gsc.disableLRMIMonitoring();
    }
    
    @Override
    public LRMIServiceMonitoringDetails[] fetchLRMIServicesMonitoringDetails() throws RemoteException {
        return gsc.fetchLRMIServicesMonitoringDetails();
    }

    public long getCurrentTimeInMillis() throws RemoteException {
        return gsc.getCurrentTimestamp();
    }

    public OSDetails getOSDetails() throws RemoteException {
        return gsc.getOSDetails();
    }

    public OSStatistics getOSStatistics() throws RemoteException {
        return gsc.getOSStatistics();
    }

    public JVMDetails getJVMDetails() throws RemoteException {
        return gsc.getJVMDetails();
    }

    public JVMStatistics getJVMStatistics() throws RemoteException {
        return gsc.getJVMStatistics();
    }

    public void runGc() throws RemoteException {
        gsc.runGc();
    }

    public boolean hasProcessingUnitInstances() {
        try {
            return gsc.getServiceRecords(ServiceRecord.ACTIVE_SERVICE_RECORD | ServiceRecord.INACTIVE_SERVICE_RECORD).length > 0;
        } catch (SecurityException se) {
            throw new AdminException("No privileges to check if container has processing unit instances", se);
        } catch (RemoteException e) {
            throw new AdminException("Failed to check if container has processing unit instances", e);
        }
    }
    
    public String[] getUnconfirmedRemovedProcessingUnitInstancesUid() {
        try {
            ServiceRecord[] serviceRecords = gsc.getServiceRecords(ServiceRecord.ACTIVE_SERVICE_RECORD | ServiceRecord.INACTIVE_SERVICE_RECORD);
            List<String> removedInstancesUid = new ArrayList<String>();
            for (ServiceRecord serviceRecord : serviceRecords) {
                String uuid = serviceRecord.getServiceID().toString();
                ProcessingUnitInstance instance = this.processingUnitInstances.getInstanceByUID(uuid);
                if (instance == null) {
                    removedInstancesUid.add(uuid);
                }
            }
            return removedInstancesUid.toArray(new String[removedInstancesUid.size()]);
        } catch (SecurityException se) {
            throw new AdminException("No privileges to check if container has removed processing unit instances", se);
        } catch (RemoteException e) {
            throw new AdminException("Failed to check if container has processing unit instances", e);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultGridServiceContainer that = (DefaultGridServiceContainer) o;
        return serviceID.equals(that.serviceID);
    }

    @Override
    public int hashCode() {
        return serviceID.hashCode();
    }
    
    
}
