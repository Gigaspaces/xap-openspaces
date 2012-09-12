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
import org.springframework.transaction.PlatformTransactionManager;
import org.openspaces.core.space.UrlSpaceConfigurer;

/**
 * A simple configurer for {@link org.openspaces.core.transaction.manager.LocalJiniTransactionManager}.
 *
 * @author kimchy
 * @deprecated since 8.0 - use {@link DistributedJiniTxManagerConfigurer} instead.
 */
@Deprecated
public class LocalJiniTxManagerConfigurer {

    final private DistributedJiniTxManagerConfigurer distConfigurer;

    public LocalJiniTxManagerConfigurer(UrlSpaceConfigurer urlSpaceConfigurer) {
        this(urlSpaceConfigurer.space());
    }

    public LocalJiniTxManagerConfigurer(IJSpace space) {
        distConfigurer = new DistributedJiniTxManagerConfigurer();
    }

    /**
     * @see org.openspaces.core.transaction.manager.LocalJiniTransactionManager#setClustered(Boolean)
     */
    public LocalJiniTxManagerConfigurer clustered(boolean clustered) {
        return this;
    }

    /**
     * @see org.openspaces.core.transaction.manager.LocalJiniTransactionManager#setDefaultTimeout(int)
     */
    public LocalJiniTxManagerConfigurer defaultTimeout(int defaultTimeout) {
        distConfigurer.defaultTimeout(defaultTimeout);
        return this;
    }

    /**
     * @see org.openspaces.core.transaction.manager.LocalJiniTransactionManager#setCommitTimeout(Long)
     */
    public LocalJiniTxManagerConfigurer commitTimeout(long commitTimeout) {
        distConfigurer.commitTimeout(commitTimeout);
        return this;
    }

    /**
     * @see org.openspaces.core.transaction.manager.LocalJiniTransactionManager#setRollbackTimeout(Long)
     */
    public LocalJiniTxManagerConfigurer rollbackTimeout(Long rollbackTimeout) {
        distConfigurer.rollbackTimeout(rollbackTimeout);
        return this;
    }

    /**
     * @see org.openspaces.core.transaction.manager.LocalJiniTransactionManager#setRollbackTimeout(Long)
     */
    public LocalJiniTxManagerConfigurer leaseRenewalConfig(TransactionLeaseRenewalConfig leaseRenewalConfig) {
        distConfigurer.leaseRenewalConfig(leaseRenewalConfig);
        return this;
    }

    public PlatformTransactionManager transactionManager() throws Exception {
        return distConfigurer.transactionManager();
    }

    public void destroy() throws Exception {
        distConfigurer.destroy();
    }
}