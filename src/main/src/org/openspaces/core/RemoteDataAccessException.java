package org.openspaces.core;

import org.springframework.dao.DataAccessException;

import java.rmi.RemoteException;

/**
 * Wraps {@link java.rmi.RemoteException}.
 *
 * @author kimchy
 */
public class RemoteDataAccessException extends DataAccessException {

    private RemoteException e;

    public RemoteDataAccessException(RemoteException e) {
        super(e.getMessage(), e);
        this.e = e;
    }

    public RemoteException getRemoteException() {
        return this.e;
    }
}
