package org.openspaces.core.transaction;

import net.jini.core.transaction.Transaction;

/**
 * @author kimchy
 */
public interface TransactionProvider {

    Transaction.Created getCurrentTransaction(Object transactionalContext);
}
