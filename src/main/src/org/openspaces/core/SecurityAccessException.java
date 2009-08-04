package org.openspaces.core;

import org.springframework.dao.PermissionDeniedDataAccessException;

import com.gigaspaces.security.SecurityException;

/**
 * Thrown for a failed operation on a secured service, due to either failed authentication or access
 * denial. Encapsulates any exception that is a subclass of {@link SecurityException}.
 * 
 * @author Moran Avigdor
 * @since 7.0.1
 */
public class SecurityAccessException extends PermissionDeniedDataAccessException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a security exception with a message and cause.
     */
    public SecurityAccessException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
