package org.openspaces.admin.internal.os;

import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.internal.os.OSStatistics;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public interface InternalOperatingSystemInfoProvider extends InternalOperatingSystemAware {

    long getCurrentTimeInMillis() throws RemoteException;

    OSDetails getOSDetails() throws RemoteException;

    OSStatistics getOSStatistics() throws RemoteException;
}
