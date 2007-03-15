package org.openspaces.core.space.filter;

import org.springframework.core.NestedRuntimeException;

/**
 * A nested runtime exception wrapping a filter execution exception.
 *
 * @author kimchy
 */
public class FilterExecutionException extends NestedRuntimeException {

    public FilterExecutionException(String msg) {
        super(msg);
    }

    public FilterExecutionException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
