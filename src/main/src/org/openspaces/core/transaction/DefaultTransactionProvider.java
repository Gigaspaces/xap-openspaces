package org.openspaces.core.transaction;

import net.jini.core.transaction.Transaction;
import org.openspaces.core.transaction.manager.JiniTransactionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * <p>Defaut transaction provider works in conjuction with {@link org.openspaces.core.transaction.manager.AbstractJiniTransactionManager}
 * and one of its derived classes. Uses Spring support for transactional resource binding (using therad local) in order to get
 * the current transaction. If no transaction is active, will return <code>null</code>
 * (which means the operation will be executed under no transaction).
 *
 * <p>As a transaction context it uses the one passed to its constructor, and not the runtime transactional
 * context provided to {@link #getCurrentTransaction(Object)}.
 *
 * @author kimchy
 * @see org.openspaces.core.transaction.manager.AbstractJiniTransactionManager
 * @see org.openspaces.core.GigaSpaceFactoryBean
 * @see org.openspaces.core.transaction.manager.JiniTransactionHolder
 * @see org.springframework.transaction.support.TransactionSynchronizationManager
 */
public class DefaultTransactionProvider implements TransactionProvider {

    private Object actualTransactionalContext;

    /**
     * Creates a new transaction provider. Will use the provided transactional context in order to
     * fetch the current running transaction.
     *
     * @param actualTransactionalContext The transactional context to fetch the transaction by
     */
    public DefaultTransactionProvider(Object actualTransactionalContext) {
        this.actualTransactionalContext = actualTransactionalContext;
    }

    /**
     * <p>Returns the current running transaction basde on the constructor provided transactional
     * context (Note that the passed transactional context is not used).
     *
     * <p>Uses Spring support for transactional resource registration in order to fetch the current
     * running transaction (or the {@link org.openspaces.core.transaction.manager.JiniTransactionHolder}. An example
     * of Spring platform transaction managers that register it are ones derived form
     * {@link org.openspaces.core.transaction.manager.AbstractJiniTransactionManager}.
     *
     * <p>If no transaction is found bound the the transactional context (provided in the constructor),
     * <code>null</code> is returned. This means that operations will execute without a transaction.
     *
     * @param transactionalContext Not Used. The transactional context used is the one provided in the constructor.
     * @return The current running transaction or <code>null</code> if no transaction is running
     */
    public Transaction.Created getCurrentTransaction(Object transactionalContext) {
        if (actualTransactionalContext == null) {
            return null;
        }

        JiniTransactionHolder txObject =
                (JiniTransactionHolder) TransactionSynchronizationManager.getResource(actualTransactionalContext);
        if (txObject != null && txObject.hasTransaction()) {
            return txObject.getTxCreated();
        }
        return null;
    }
}
