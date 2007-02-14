package org.openspaces.core.transaction.manager;

import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author kimchy
 */
public interface JiniPlatformTransactionManager extends PlatformTransactionManager {

    Object getTransactionalContext();
}
