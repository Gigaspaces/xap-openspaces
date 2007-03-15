package org.openspaces.core;

import net.jini.core.transaction.TransactionException;
import org.springframework.dao.DataAccessException;

/**
 * An exception occured during a space operation that has to do with transactional
 * semantics. Wraps {@link net.jini.core.transaction.TransactionException}.
 *
 * @author kimchy
 */
public class TransactionDataAccessException extends DataAccessException {

    public TransactionDataAccessException(String msg) {
        super(msg);
    }

    public TransactionDataAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public TransactionDataAccessException(TransactionException e) {
        super(e.getMessage(), e);
    }
}
