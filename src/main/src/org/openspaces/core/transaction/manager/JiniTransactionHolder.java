package org.openspaces.core.transaction.manager;

import net.jini.core.transaction.Transaction;
import org.springframework.transaction.support.ResourceHolderSupport;

/**
 * <p>A Jini transaction holder responsible for holding the current running transaction.
 *
 * @author kimchy
 * @see AbstractJiniTransactionManager
 */
public class JiniTransactionHolder extends ResourceHolderSupport {

    private Transaction.Created txCreated;

    public JiniTransactionHolder(Transaction.Created txCreated) {
        this.txCreated = txCreated;
    }

    public Transaction.Created getTxCreated() {
        return txCreated;
    }

    public boolean hasTransaction() {
        return (txCreated != null && txCreated.transaction != null);
    }

}
