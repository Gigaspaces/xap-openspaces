package org.openspaces.core;

/**
 * An exception happened during a space operation execution.
 *
 * @author kimchy
 */
public class SpaceOperationException extends GigaSpaceException {

    public SpaceOperationException(String message) {
        super(message);
    }

    public SpaceOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
