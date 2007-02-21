package org.openspaces.core.transaction.manager;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.LocalTransactionManager;
import net.jini.core.transaction.server.TransactionManager;
import org.springframework.aop.framework.Advised;
import org.springframework.util.Assert;

/**
 * <p>Springs transaction manager ({@link org.springframework.transaction.PlatformTransactionManager} using
 * GigaSpaces {@link org.openspaces.core.transaction.manager.LocalJiniTransactionManager}.
 *
 * <p>Local transaction manager is high performance single space transaction manager and should be used in
 * most if not all space related operations.
 *
 * @author kimchy
 * @see com.j_spaces.core.client.LocalTransactionManager
 */
public class LocalJiniTransactionManager extends AbstractJiniTransactionManager {

    private IJSpace space;

    /**
     * Sets the the {@link com.j_spaces.core.client.LocalTransactionManager} will work with.
     * This is a required property.
     */
    public void setSpace(IJSpace space) {
        this.space = space;
    }

    /**
     * Returns a new {@link com.j_spaces.core.client.LocalTransactionManager} using the provided
     * {@link #setSpace(com.j_spaces.core.IJSpace)}.
     */
    public TransactionManager doCreateTransactionManager() throws Exception {
        Assert.notNull(space, "space property must be set");

        while (space instanceof Advised) {
            space = (IJSpace) ((Advised) space).getTargetSource().getTarget();
        }
        return LocalTransactionManager.getInstance(space);
    }
}
