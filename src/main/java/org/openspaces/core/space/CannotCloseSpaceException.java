package org.openspaces.core.space;

import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Happens when {@link com.j_spaces.core.IJSpace} can not be closed.
 *
 * @author Niv Ingberg
 * @since 10.0
 */
public class CannotCloseSpaceException extends DataAccessResourceFailureException {
    private static final long serialVersionUID = 1L;

    public CannotCloseSpaceException(String message, Throwable cause) {
        super(message, cause);
    }
}
