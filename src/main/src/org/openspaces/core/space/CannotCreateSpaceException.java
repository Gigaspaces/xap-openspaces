package org.openspaces.core.space;

import org.springframework.dao.DataAccessResourceFailureException;

/**
 * Happens when {@link com.j_spaces.core.IJSpace} can not be created.
 *
 * @author kimchy
 */
public class CannotCreateSpaceException extends DataAccessResourceFailureException {

    private static final long serialVersionUID = -8026907614225627043L;

    public CannotCreateSpaceException(String message, Throwable cause) {
        super(message, cause);
    }
}
