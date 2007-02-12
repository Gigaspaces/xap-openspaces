package org.openspaces.core;

import org.springframework.core.NestedRuntimeException;

/**
 * A general rutime GigaSpace exception. This exception acts as a base class for all
 * different runtime exception.
 *
 * @author kimchy
 */
public class GigaSpaceException extends NestedRuntimeException {

    /**
     * Creates a new exception using the message.
     *
     * @param message The exception message
     */
    public GigaSpaceException(String message) {
        super(message);
    }

    /**
     * Creates a new exception using the message and its cause.
     *
     * @param message The exception message
     * @param cause The exception cause
     */
    public GigaSpaceException(String message, Throwable cause) {
        super(message, cause);
    }
}
