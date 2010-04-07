package org.openspaces.core;

import org.springframework.dao.DataAccessException;

/**
 * This exception indicates that a resource usage on the server is exceeding its capacity
 * 
 * {@link SpaceMemoryShortageException}
 * {@link RedoLogCapacityExceededException}
 * {@link com.gigaspaces.client.ResourceCapacityExceededException}
 * @author	eitany
 * @since	7.1
 */
public class ResourceCapacityExceededException extends DataAccessException {
    
    public ResourceCapacityExceededException(com.gigaspaces.client.ResourceCapacityExceededException e) {
        super(e.getMessage(), e);
    }
    
    public ResourceCapacityExceededException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
