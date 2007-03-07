package org.openspaces.core.transaction;

import net.jini.core.transaction.Transaction;

/**
 * <p>A transaction provider is used to support declarative transactions. It is usually
 * used with an {@link org.openspaces.core.GigaSpace} implementation to declarativly
 * provide on going transactions to JavaSpace APIs without the need to explicitly
 * provide them.
 *
 * <p>The transaction provider usually interacts with a transaction manager (for example, Spring's
 * {@link org.springframework.transaction.PlatformTransactionManager}) in order to get the current
 * running transactions. It allows a transactional context to be passed to the current transaction
 * method, though it doesn't have to be used.
 *
 * @author kimchy
 */
public interface TransactionProvider {

    /**
     * <p>Returns the currently running transaction (usually managed externally/declarative). A transactional
     * context can be passed and optionally used in order to fetch the current running transaction.
     *
     * <p>If no transaction is currently executing, <code>null</code> value will be returned. This usually
     * means that the operation will be performed without a transaction.
     *
     * @param transactionalContext Transactional context to (optionally) fetch the transcation by
     * @return The transaction object to be used with {@link com.j_spaces.core.IJSpace} operations. Can be <code>null</code>.
     */
    Transaction.Created getCurrentTransaction(Object transactionalContext);

    /**
     * <p>Returns the currently running transaction isolation level (mapping to Spring
     * {@link org.springframework.transaction.TransactionDefinition#getIsolationLevel()} values). A transactional
     * context can be passed and optionally used in order to fetch the current running transaction.
     *
     * @param transactionalContext Transactional context to (optionally) fetch the transcation by
     * @return The transaction isolation level mapping to Spring {@link org.springframework.transaction.TransactionDefinition#getIsolationLevel()}.
     */
    int getCurrentTransactionIsolationLevel(Object transactionalContext);
}
