package org.openspaces.core.transaction;

import net.jini.core.transaction.server.TransactionManager;
import org.openspaces.core.jini.JiniServiceFactoryBean;

/**
 * @author kimchy
 */
public class JiniTransactionManagerFactoryBean extends AbstractTransactionManagerFactoryBean {

    private String transactionManagerName;

    protected TransactionManager createTransactionManager() throws Exception {
        JiniServiceFactoryBean serviceFactory = new JiniServiceFactoryBean();
        serviceFactory.setServiceClass(TransactionManager.class);
        serviceFactory.setServiceName(transactionManagerName);
        serviceFactory.afterPropertiesSet();

        return (TransactionManager) serviceFactory.getObject();
    }

    public void setTransactionManagerName(String transactionManagerName) {
        this.transactionManagerName = transactionManagerName;
    }
}
