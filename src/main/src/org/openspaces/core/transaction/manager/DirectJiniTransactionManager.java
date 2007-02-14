package org.openspaces.core.transaction.manager;

import net.jini.core.transaction.server.TransactionManager;

/**
 * @author kimchy
 */
public class DirectJiniTransactionManager extends AbstractJiniTransactionManager {

    private TransactionManager transactionManager;

    protected TransactionManager doCreateTransactionManager() throws Exception {
        return this.transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
}
