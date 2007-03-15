package org.openspaces.core;

import com.j_spaces.core.UnknownTypeException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * Exception thrown when a space receives an entry/template of type that is not in his type table.
 * It is a wrapper for {@link com.j_spaces.core.UnknownTypeException}.
 *
 * @author kimchy
 */
public class InvalidTypeDataAccessException extends InvalidDataAccessResourceUsageException {

    private static final long serialVersionUID = -2179178202057632276L;

    /**
     * Constructor for {@link InvalidTypeDataAccessException}.
     */
    public InvalidTypeDataAccessException(UnknownTypeException e) {
        super(e.getMessage(), e);
    }
}
