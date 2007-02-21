package org.openspaces.core.transaction.manager;

import net.jini.core.transaction.server.TransactionManager;
import org.openspaces.core.jini.JiniServiceFactoryBean;

/**
 * <p>Springs transaction manager ({@link org.springframework.transaction.PlatformTransactionManager} using
 * Jini in order to lookup the transaction manager based on a name (can have <code>null</code> value).
 *
 * <p>Uses {@link org.openspaces.core.jini.JiniServiceFactoryBean} in order to perform the lookup based on
 * the specified {@link #setTransactionManagerName(String)} and {@link #setTimeout(Long)}.
 *
 * @author kimchy
 */
public class LookupJiniTransactionManager extends AbstractJiniTransactionManager {

    private String transactionManagerName;

    private Long timeout;

    /**
     * Sets the transaction manager name to perform the lookup by.
     */
    public void setTransactionManagerName(String transactionManagerName) {
        this.transactionManagerName = transactionManagerName;
    }

    /**
     * Sets the timeout for the transaction manager lookup operation.
     */
    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    /**
     * Returns a Jini {@link net.jini.core.transaction.server.TransactionManager} that is lookup
     * up using {@link org.openspaces.core.jini.JiniServiceFactoryBean}. The lookup can use a
     * specified {@link #setTransactionManagerName(String)} and a
     * {@link #setTimeout(Long)}.
     */
    protected TransactionManager doCreateTransactionManager() throws Exception {
        JiniServiceFactoryBean serviceFactory = new JiniServiceFactoryBean();
        serviceFactory.setServiceClass(TransactionManager.class);
        serviceFactory.setServiceName(transactionManagerName);
        if (timeout != null) {
            serviceFactory.setTimeout(timeout.longValue());
        }
        serviceFactory.afterPropertiesSet();
        return (TransactionManager) serviceFactory.getObject();
    }

}
