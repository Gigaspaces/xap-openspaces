package org.openspaces.core;

import net.jini.core.transaction.TransactionException;

/**
 * Thrown when an invalid tranasction usage is performed. For example, when using a local
 * transaction manager with more than one space. Wraps a {@link net.jini.core.transaction.TransactionException}.
 *
 * @author kimchy
 */
public class InvalidTransactionUsageException extends TransactionDataAccessException {

    public InvalidTransactionUsageException(TransactionException e) {
        super(e);
    }
}
