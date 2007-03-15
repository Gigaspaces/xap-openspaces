package org.openspaces.core;

import org.springframework.dao.DataAccessException;

/**
 * A wrapper for {@link net.jini.space.InternalSpaceException}.
 *
 * @author kimchy
 */
public class InternalSpaceException extends DataAccessException {

    private net.jini.space.InternalSpaceException e;

    public InternalSpaceException(net.jini.space.InternalSpaceException e) {
        super(e.getMessage(), e);
        this.e = e;
    }

    /**
     * Returns the nested exception of the original Jini {@link net.jini.space.InternalSpaceException}.
     *
     * @see net.jini.space.InternalSpaceException#nestedException
     */
    public Throwable getNestedException() {
        return e.nestedException;
    }
}
