package org.openspaces.core;

import net.jini.core.transaction.TransactionException;

/**
 * Thrown when an operation is perfomed on an inactive transaction.
 *
 * @author kimchy
 */
public class InactiveTransactionException extends TransactionDataAccessException {

    public InactiveTransactionException(com.j_spaces.core.TransactionNotActiveException e) {
        super(e.getMessage(), e);
    }

    public InactiveTransactionException(TransactionException e) {
        super(e);
    }
}
