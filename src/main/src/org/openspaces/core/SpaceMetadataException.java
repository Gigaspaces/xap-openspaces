package org.openspaces.core;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * Thrown when a metadata error is detected.
 * 
 * @author Niv Ingberg
 * @since 8.0
 */
public class SpaceMetadataException extends InvalidDataAccessResourceUsageException {
    private static final long serialVersionUID = 1L;

    public SpaceMetadataException(String msg) {
        super(msg);
    }
    
    public SpaceMetadataException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
