package org.openspaces.core;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * Thrown when failed to serialize or deserialize Entry field. Wraps
 * {@link com.j_spaces.core.EntrySerializationException}.
 *
 * @author kimchy
 */
public class EntrySerializationException extends InvalidDataAccessResourceUsageException {

    public EntrySerializationException(com.j_spaces.core.EntrySerializationException e) {
        super(e.getMessage(), e);
    }
}
