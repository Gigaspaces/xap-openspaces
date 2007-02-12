package org.openspaces.core.transaction;

import net.jini.core.transaction.server.TransactionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author kimchy
 */
public abstract class AbstractTransactionManagerFactoryBean implements FactoryBean, InitializingBean {

    protected final Log log = LogFactory.getLog(getClass());

    private TransactionManager tm;

    public Object getObject() throws Exception {
        return tm;
    }

    public Class getObjectType() {
        return (tm == null ? TransactionManager.class : tm.getClass());
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {
        // create the TM
        tm = createTransactionManager();
    }

    /**
     * Subclasses need to implement this method to create or lookup the Transaction Manager.
     */
    protected abstract TransactionManager createTransactionManager() throws Exception;

}
