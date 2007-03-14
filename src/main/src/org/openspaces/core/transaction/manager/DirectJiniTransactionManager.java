package org.openspaces.core.transaction.manager;

import net.jini.core.transaction.server.TransactionManager;

import org.springframework.util.Assert;

/**
 * Springs transaction manager ({@link org.springframework.transaction.PlatformTransactionManager}
 * using directly injected Jini {@link TransactionManager}. This transaction manager is mostly used
 * with applications that obtain the Jini transaction maanger by other means than the ones provided
 * by {@link LocalJiniTransactionManager} and {@link DistributedJiniTransactionManager}.
 * 
 * @author kimchy
 */
public class DirectJiniTransactionManager extends AbstractJiniTransactionManager {

    private static final long serialVersionUID = -8773176073029897135L;

    private TransactionManager transactionManager;

    /**
     * Sets the Jini {@link TransactionManager} to be used. This is a required property.
     */
    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * Returns the {@link TransactionManager} provided using the
     * {@link #setTransactionManager(TransactionManager)}.
     */
    protected TransactionManager doCreateTransactionManager() throws Exception {
        Assert.notNull(transactionManager, "transactionManager is required property");
        return this.transactionManager;
    }
}
