package org.openspaces.core.transaction.manager;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.LocalTransactionManager;
import net.jini.core.transaction.server.TransactionManager;
import org.openspaces.core.GigaSpace;
import org.springframework.aop.framework.Advised;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.util.Assert;

/**
 * <p>Springs transaction manager ({@link org.springframework.transaction.PlatformTransactionManager} using
 * GigaSpaces {@link org.openspaces.core.transaction.manager.LocalJiniTransactionManager}.
 *
 * <p>Local transaction manager is high performance single space transaction manager and should be used in
 * most if not all space related operations.
 *
 * <p>The local transaction manager also allows for most transaction isolation levels excluding
 * <code>SERIALIZABLE</code>. This is automatically applied when using this transaction manager
 * in conjuction with {@link org.openspaces.core.GigaSpace} API.
 *
 * @author kimchy
 * @see com.j_spaces.core.client.LocalTransactionManager
 */
public class LocalJiniTransactionManager extends AbstractJiniTransactionManager {

    private GigaSpace gigaSpace;

    /**
     * Sets the the {@link com.j_spaces.core.client.LocalTransactionManager} will work with.
     * This is a required property.
     */
    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    /**
     * Returns a new {@link com.j_spaces.core.client.LocalTransactionManager} using the provided
     * {@link #setGigaSpace(org.openspaces.core.GigaSpace)}.
     */
    public TransactionManager doCreateTransactionManager() throws Exception {
        Assert.notNull(gigaSpace, "gigaSpace property must be set");

        IJSpace space = gigaSpace.getSpace();

        // use the space as the transactional context (and not the transaction manager) in case of Local transactions
        if (getTransactionalContext() == null) {
            setTransactionalContext(space);
        }

        while (space instanceof Advised) {
            space = (IJSpace) ((Advised) space).getTargetSource().getTarget();
        }

        return LocalTransactionManager.getInstance(space);
    }

    protected void applyIsolationLevel(JiniTransactionObject txObject, int isolationLevel) throws InvalidIsolationLevelException {
        if (isolationLevel == TransactionDefinition.ISOLATION_SERIALIZABLE) {
            throw new InvalidIsolationLevelException("Local TransactionManager does not support serializable isolation level");
        }
    }

}
