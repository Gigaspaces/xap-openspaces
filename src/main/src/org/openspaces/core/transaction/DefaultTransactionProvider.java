package org.openspaces.core.transaction;

import net.jini.core.transaction.Transaction;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * <p>Defaut transaction provider works in conjuction with {@link org.openspaces.core.transaction.GigaSpaceTransactionManager}.
 * Uses Spring support for transactional resource binding (using therad local) in order to get
 * the current transaction. If no transaction is active, will return <code>null</code>
 * (which means the operation will be executed under no transaction).
 *
 * <p>As a transaction context it uses the one passed to its constructor, and not the runtime transactional
 * context provided to {@link #getCurrentTransaction(Object)}.
 *
 * @author kimchy
 * @see org.openspaces.core.transaction.GigaSpaceTransactionManager
 */
public class DefaultTransactionProvider implements TransactionProvider {

    private Object actualTransactionContext;

    public DefaultTransactionProvider(Object actualTransactionContext) {
        this.actualTransactionContext = actualTransactionContext;
    }

    public Transaction.Created getCurrentTransaction(Object transactionalContext) {
        if (actualTransactionContext == null)
            return null;

        GigaSpaceTransactionManager.JiniHolder txObject =
                (GigaSpaceTransactionManager.JiniHolder) TransactionSynchronizationManager.getResource(actualTransactionContext);
        if (txObject != null && txObject.hasTransaction()) {
            return txObject.getTxCreated();
        }
        return null;
    }
}
