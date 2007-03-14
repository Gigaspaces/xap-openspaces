package org.openspaces.core;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

import com.j_spaces.core.UnknownTypeException;

/**
 * Exception thrown when a space receives an entry/template of type that is not in his type table.
 * 
 * @author kimchy
 */
public class InvalidTypeDataAccessException extends InvalidDataAccessResourceUsageException {

    private static final long serialVersionUID = -2179178202057632276L;

    /**
     * Constructor for {@link InvalidTypeDataAccessException}.
     * 
     * @param msg
     *            the detail message
     * @param cause
     *            the root cause from the data access API in use
     */
    public InvalidTypeDataAccessException(UnknownTypeException e) {
        super(e.getMessage(), e);
    }
}
