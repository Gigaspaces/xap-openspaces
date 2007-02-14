package org.openspaces.core.transaction.manager;

import net.jini.core.transaction.server.TransactionManager;
import org.openspaces.core.jini.JiniServiceFactoryBean;

/**
 * @author kimchy
 */
public class DefaultJiniTransactionManager extends AbstractJiniTransactionManager {

    private String transactionManagerName;

    public void setTransactionManagerName(String transactionManagerName) {
        this.transactionManagerName = transactionManagerName;
    }

    protected TransactionManager doCreateTransactionManager() throws Exception {
        JiniServiceFactoryBean serviceFactory = new JiniServiceFactoryBean();
        serviceFactory.setServiceClass(TransactionManager.class);
        serviceFactory.setServiceName(transactionManagerName);
        serviceFactory.afterPropertiesSet();
        return (TransactionManager) serviceFactory.getObject();
    }

}
