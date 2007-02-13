package org.openspaces.core.transaction;

import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author kimchy
 */
public interface JiniPlatformTransactionManager extends PlatformTransactionManager {

    Object getTransactionalContext();
}
