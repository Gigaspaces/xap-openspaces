package org.openspaces.core.transaction.manager;

import net.jini.core.transaction.server.TransactionManager;
import org.springframework.util.Assert;

/**
 * <p>Springs transaction manager ({@link org.springframework.transaction.PlatformTransactionManager} using
 * directly injected Jini {@link TransactionManager}. This transaction manager is mostly used with applications
 * that obtain the Jini transaction maanger by other means than the ones provided by
 * {@link org.openspaces.core.transaction.manager.LocalJiniTransactionManager} and
 * {@link org.openspaces.core.transaction.manager.LookupJiniTransactionManager}.
 *
 * @author kimchy
 */
public class DirectJiniTransactionManager extends AbstractJiniTransactionManager {

    private TransactionManager transactionManager;

    /**
     * Sets the Jini {@link net.jini.core.transaction.server.TransactionManager} to be used.
     * This is a required property.
     */
    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * Returns the {@link net.jini.core.transaction.server.TransactionManager} provided
     * using the {@link #setTransactionManager(net.jini.core.transaction.server.TransactionManager)}.
     */
    protected TransactionManager doCreateTransactionManager() throws Exception {
        Assert.notNull(transactionManager, "transactionManager is required property");
        return this.transactionManager;
    }
}
