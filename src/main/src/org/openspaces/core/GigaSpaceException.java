package org.openspaces.core;

import org.springframework.core.NestedRuntimeException;

/**
 * A general rutime GigaSpace exception. This exception acts as a base class for all different
 * runtime exception.
 * 
 * @author kimchy
 */
public class GigaSpaceException extends NestedRuntimeException {

    private static final long serialVersionUID = 8285964671142218564L;

    /**
     * Creates a new exception using the message.
     * 
     * @param message
     *            The exception message
     */
    public GigaSpaceException(String message) {
        super(message);
    }

    /**
     * Creates a new exception using the message and its cause.
     * 
     * @param message
     *            The exception message
     * @param cause
     *            The exception cause
     */
    public GigaSpaceException(String message, Throwable cause) {
        super(message, cause);
    }
}
