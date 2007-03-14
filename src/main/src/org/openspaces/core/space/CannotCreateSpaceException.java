package org.openspaces.core.space;

import org.openspaces.core.GigaSpaceException;

/**
 * Happens when {@link com.j_spaces.core.IJSpace} can not be created.
 *
 * @author kimchy
 */
public class CannotCreateSpaceException extends GigaSpaceException {

    private static final long serialVersionUID = -8026907614225627043L;

    public CannotCreateSpaceException(String message) {
        super(message);
    }

    public CannotCreateSpaceException(String message, Throwable cause) {
        super(message, cause);
    }
}
