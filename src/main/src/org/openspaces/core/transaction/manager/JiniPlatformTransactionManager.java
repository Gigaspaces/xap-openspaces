package org.openspaces.core.transaction.manager;

import org.springframework.transaction.PlatformTransactionManager;

/**
 * An extension to Spring {@link org.springframework.transaction.PlatformTransactionManager} that
 * holds the Jini transactional context. The transactional context is the context the Jini
 * transaction is bounded under (usually using Spring synchronization which is based on thread
 * local).
 * 
 * @author kimchy
 */
public interface JiniPlatformTransactionManager extends PlatformTransactionManager {

    /**
     * Returns the transactional context the jini transaction is bounded under (usually using Spring
     * synchronization which is based on thread local).
     */
    Object getTransactionalContext();
}
