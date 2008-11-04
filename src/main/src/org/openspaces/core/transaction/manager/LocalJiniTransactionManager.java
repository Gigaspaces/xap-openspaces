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
import com.j_spaces.core.client.cache.ISpaceLocalCache;
import net.jini.core.transaction.server.TransactionManager;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.util.SpaceUtils;
import org.openspaces.pu.container.servicegrid.PUServiceDetails;
import org.openspaces.pu.container.servicegrid.PlainPUServiceDetails;
import org.springframework.aop.framework.Advised;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.util.Assert;

/**
 * Spring transaction manager ({@link org.springframework.transaction.PlatformTransactionManager}
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

    private IJSpace space;

    private Boolean clustered;

    /**
     * Sets the Space that will be used when working with the local transaction manager.
     */
    public void setSpace(IJSpace space) {
        this.space = space;
    }

    /**
     * Sets if this local transaction manager will work on top of a clustered Space, or will work
     * directly with a cluster memeber.
     */
    public void setClustered(Boolean clustered) {
        this.clustered = clustered;
    }

    /**
     * Returns a new {@link LocalTransactionManager} using the provided
     * {@link #setSpace(com.j_spaces.core.IJSpace)}.
     *
     * <p>
     * The transactional context is automatically set to be the {@link IJSpace} associated with the
     * provided {@link GigaSpace}. This allows for zero conf when working with this local
     * transaction manager and {@link GigaSpace}.
     */
    public TransactionManager doCreateTransactionManager() throws Exception {
        Assert.notNull(space, "space property must be set");

        IJSpace space = this.space;
        if (clustered == null) {
            // in case the space is a local cache space, set the clustered flag to true since we do
            // not want to get the actual member (the cluster flag was set on the local cache already)
            if (space instanceof ISpaceLocalCache) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Clustered flag automatically set to [" + clustered + "] since the space is a local cache space");
                }
                clustered = true;
            } else {
                clustered = SpaceUtils.isRemoteProtocol(space);
                if (logger.isDebugEnabled()) {
                    logger.debug("Clustered flag automatically set to [" + clustered + "]");
                }
            }
        }
        if (!clustered) {
            space = SpaceUtils.getClusterMemberSpace(space);
        }

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

    public PUServiceDetails[] getServicesDetails() {
        return new PUServiceDetails[] {new PlainPUServiceDetails(SERVICE_TYPE, "local", space.getName(), space.toString())};
    }

    protected void applyIsolationLevel(JiniTransactionObject txObject, int isolationLevel)
            throws InvalidIsolationLevelException {
        if (isolationLevel == TransactionDefinition.ISOLATION_SERIALIZABLE) {
            throw new InvalidIsolationLevelException(
                    "Local TransactionManager does not support serializable isolation level");
        }
    }

}
