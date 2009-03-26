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
import org.springframework.beans.factory.DisposableBean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * A simple configurer for {@link org.openspaces.core.transaction.manager.LocalJiniTransactionManager}.
 *
 * @author kimchy
 */
public class LocalJiniTxManagerConfigurer {

    private LocalJiniTransactionManager localJiniTransactionManager;

    private boolean initialized = false;

    public LocalJiniTxManagerConfigurer(IJSpace space) {
        localJiniTransactionManager = new LocalJiniTransactionManager();
        localJiniTransactionManager.setSpace(space);
    }

    /**
     * @see org.openspaces.core.transaction.manager.LocalJiniTransactionManager#setClustered(Boolean)
     */
    public LocalJiniTxManagerConfigurer clustered(boolean clustered) {
        localJiniTransactionManager.setClustered(clustered);
        return this;
    }

    /**
     * @see org.openspaces.core.transaction.manager.LocalJiniTransactionManager#setDefaultTimeout(int)
     */
    public LocalJiniTxManagerConfigurer defaultTimeout(int defaultTimeout) {
        localJiniTransactionManager.setDefaultTimeout(defaultTimeout);
        return this;
    }

    /**
     * @see org.openspaces.core.transaction.manager.LocalJiniTransactionManager#setCommitTimeout(Long)
     */
    public LocalJiniTxManagerConfigurer commitTimeout(long commitTimeout) {
        localJiniTransactionManager.setCommitTimeout(commitTimeout);
        return this;
    }

    /**
     * @see org.openspaces.core.transaction.manager.LocalJiniTransactionManager#setRollbackTimeout(Long)
     */
    public LocalJiniTxManagerConfigurer rollbackTimeout(Long rollbackTimeout) {
        localJiniTransactionManager.setRollbackTimeout(rollbackTimeout);
        return this;
    }

    /**
     * @see org.openspaces.core.transaction.manager.LocalJiniTransactionManager#setRollbackTimeout(Long)
     */
    public LocalJiniTxManagerConfigurer leaseRenewalConfig(TransactionLeaseRenewalConfig leaseRenewalConfig) {
        localJiniTransactionManager.setLeaseRenewalConfig(leaseRenewalConfig);
        return this;
    }

    public PlatformTransactionManager transactionManager() throws Exception {
        if (!initialized) {
            localJiniTransactionManager.afterPropertiesSet();
            initialized = true;
        }
        return localJiniTransactionManager;
    }

    public void destroy() throws Exception {
        if (localJiniTransactionManager instanceof DisposableBean) {
            ((DisposableBean) localJiniTransactionManager).destroy();
        }
    }
}