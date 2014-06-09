package org.openspaces.core;

import org.springframework.dao.DataAccessException;

/**
 * Thrown when an operation could not be completed because an entry was locked by another transaction.
 *
 * @author Niv Ingberg
 * @since 10.0
 */
public class EntryLockedException extends DataAccessException {
    public EntryLockedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
