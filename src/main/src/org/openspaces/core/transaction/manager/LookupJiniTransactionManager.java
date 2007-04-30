package org.openspaces.core.transaction.manager;

import net.jini.core.transaction.server.TransactionManager;
import org.openspaces.core.jini.JiniServiceFactoryBean;

/**
 * Springs transaction manager ({@link org.springframework.transaction.PlatformTransactionManager}
 * using Jini in order to lookup the transaction manager based on a name (can have <code>null</code>
 * value).
 *
 * <p>Uses {@link JiniServiceFactoryBean} in order to perform the lookup based on the specified
 * {@link #setTransactionManagerName(String)} and {@link #setLookupTimeout(Long)}. This usually
 * works with Jini Mahalo transaction manager.
 *
 * @author kimchy
 */
public class LookupJiniTransactionManager extends AbstractJiniTransactionManager {

    private static final long serialVersionUID = -917940171952237730L;

    private String transactionManagerName;

    private Long lookupTimeout;

    /**
     * Sets the transaction manager name to perform the lookup by.
     */
    public void setTransactionManagerName(String transactionManagerName) {
        this.transactionManagerName = transactionManagerName;
    }

    /**
     * Sets the lookupTimeout for the transaction manager lookup operation.
     */
    public void setLookupTimeout(Long lookupTimeout) {
        this.lookupTimeout = lookupTimeout;
    }

    /**
     * Returns a Jini {@link TransactionManager} that is lookup up using
     * {@link JiniServiceFactoryBean}. The lookup can use a specified
     * {@link #setTransactionManagerName(String)} and a {@link #setLookupTimeout(Long)}.
     */
    protected TransactionManager doCreateTransactionManager() throws Exception {
        JiniServiceFactoryBean serviceFactory = new JiniServiceFactoryBean();
        serviceFactory.setServiceClass(TransactionManager.class);
        serviceFactory.setServiceName(transactionManagerName);
        if (lookupTimeout != null) {
            serviceFactory.setTimeout(lookupTimeout);
        }
        serviceFactory.afterPropertiesSet();
        return (TransactionManager) serviceFactory.getObject();
    }

}
