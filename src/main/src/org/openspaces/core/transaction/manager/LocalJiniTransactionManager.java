package org.openspaces.core.transaction.manager;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.LocalTransactionManager;
import net.jini.core.transaction.server.TransactionManager;
import org.springframework.aop.framework.Advised;
import org.springframework.util.Assert;

/**
 * @author kimchy
 */
public class LocalJiniTransactionManager extends AbstractJiniTransactionManager {

    private IJSpace space;

    public void setSpace(IJSpace space) {
        this.space = space;
    }

    public TransactionManager doCreateTransactionManager() throws Exception {
        Assert.notNull(space, "space property must be set");

        while (space instanceof Advised) {
            space = (IJSpace) ((Advised) space).getTargetSource().getTarget();
        }
        return LocalTransactionManager.getInstance(space);
    }
}
