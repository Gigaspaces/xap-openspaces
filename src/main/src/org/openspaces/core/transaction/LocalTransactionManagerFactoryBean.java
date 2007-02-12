package org.openspaces.core.transaction;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.LocalTransactionManager;
import net.jini.core.transaction.server.TransactionManager;
import org.springframework.aop.framework.Advised;
import org.springframework.util.Assert;

/**
 * @author kimchy
 */
public class LocalTransactionManagerFactoryBean extends AbstractTransactionManagerFactoryBean {

    private IJSpace space;

    public void setSpace(IJSpace space) {
        this.space = space;
    }

    public TransactionManager createTransactionManager() throws Exception {
        Assert.notNull(space, "space property must be set");

        while (space instanceof Advised) {
            space = (IJSpace) ((Advised) space).getTargetSource().getTarget();
        }
        return LocalTransactionManager.getInstance(space);
    }
}

