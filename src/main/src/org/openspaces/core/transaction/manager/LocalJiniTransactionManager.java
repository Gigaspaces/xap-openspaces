/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * Springs transaction manager ({@link org.springframework.transaction.PlatformTransactionManager}
 * using GigaSpaces {@link LocalJiniTransactionManager}.
 * 
 * <p>Local transaction manager is high performance single space transaction manager and should be used
 * in most if not all space related operations.
 * 
 * <p>The local transaction manager also allows for most transaction isolation levels excluding
 * <code>SERIALIZABLE</code>. This is automatically applied when using this transaction manager
 * in conjunction with {@link org.openspaces.core.GigaSpace} API.
 * 
 * @author kimchy
 * @see com.j_spaces.core.client.LocalTransactionManager
 */
public class LocalJiniTransactionManager extends AbstractJiniTransactionManager {

    private static final long serialVersionUID = -2672383547433358975L;

    private GigaSpace gigaSpace;

    /**
     * Sets the the {@link LocalTransactionManager} will work with. This is a required property.
     */
    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    /**
     * Returns a new {@link LocalTransactionManager} using the provided
     * {@link #setGigaSpace(org.openspaces.core.GigaSpace)}.
     * 
     * <p>
     * The transactional context is automatically set to be the {@link IJSpace} associated with the
     * provided {@link GigaSpace}. This allows for zero conf when working with this local
     * transaction manager and {@link GigaSpace}.
     */
    public TransactionManager doCreateTransactionManager() throws Exception {
        Assert.notNull(gigaSpace, "gigaSpace property must be set");

        IJSpace space = gigaSpace.getSpace();

        // use the space as the transactional context (and not the transaction manager) in case of
        // Local transactions
        if (getTransactionalContext() == null) {
            setTransactionalContext(space);
        }

        while (space instanceof Advised) {
            space = (IJSpace) ((Advised) space).getTargetSource().getTarget();
        }

        return LocalTransactionManager.getInstance(space);
    }

    protected void applyIsolationLevel(JiniTransactionObject txObject, int isolationLevel)
            throws InvalidIsolationLevelException {
        if (isolationLevel == TransactionDefinition.ISOLATION_SERIALIZABLE) {
            throw new InvalidIsolationLevelException(
                    "Local TransactionManager does not support serializable isolation level");
        }
    }

}
