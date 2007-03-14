package org.openspaces.core;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * Thrown when one tries to get an Entry from a service, but the entry is unusable (due to
 * serialization or other errors).
 * 
 * @author kimchy
 */
public class UnusableEntryException extends DataRetrievalFailureException {

    private static final long serialVersionUID = -8652798659299131940L;

    public UnusableEntryException(String message, net.jini.core.entry.UnusableEntryException cause) {
        super(message, cause);
    }

    public UnusableEntryException(net.jini.core.entry.UnusableEntryException cause) {
        super(cause.getMessage(), cause);
    }

}
