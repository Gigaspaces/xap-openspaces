package org.openspaces.core.transaction.manager;

import net.jini.core.transaction.Transaction;

import org.springframework.transaction.support.ResourceHolderSupport;

/**
 * A Jini transaction holder responsible for holding the current running transaction.
 * 
 * @author kimchy
 * @see AbstractJiniTransactionManager
 */
public class JiniTransactionHolder extends ResourceHolderSupport {

    private Transaction.Created txCreated;

    private int isolationLevel;

    /**
     * Constructs a new jini transaction holder.
     * 
     * @param txCreated
     *            The Jini transaction created object
     * @param isolationLevel
     *            The isolation level that transaction is executed under
     */
    public JiniTransactionHolder(Transaction.Created txCreated, int isolationLevel) {
        this.txCreated = txCreated;
        this.isolationLevel = isolationLevel;
    }

    /**
     * Returns <code>true</code> if there is an existing transaction held by this bean,
     * <code>false</code> if no tranasction is in progress.
     */
    public boolean hasTransaction() {
        return (txCreated != null && txCreated.transaction != null);
    }

    /**
     * Returns the Jini transaction created object. Can be <code>null</code>.
     */
    public Transaction.Created getTxCreated() {
        return txCreated;
    }

    /**
     * Returns the current transaction isolation level. Maps to Spring
     * {@link org.springframework.transaction.TransactionDefinition#getIsolationLevel()} values.
     */
    public int getIsolationLevel() {
        return this.isolationLevel;
    }
}
