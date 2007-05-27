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

package org.openspaces.core.transaction;

import net.jini.core.transaction.Transaction;
import org.openspaces.core.transaction.manager.JiniTransactionHolder;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Defaut transaction provider works in conjuction with
 * {@link org.openspaces.core.transaction.manager.JiniPlatformTransactionManager JiniPlatformTransactionManager}
 * and one of its derived classes. Uses Spring support for transactional resource binding (using
 * therad local) in order to get the current transaction. If no transaction is active, will return
 * <code>null</code> (which means the operation will be executed under no transaction).
 * 
 * <p>
 * As a transaction context it uses the one passed to its constructor, and not the runtime
 * transactional context provided to {@link #getCurrentTransaction(Object)}.
 * 
 * @author kimchy
 * @see org.openspaces.core.transaction.manager.AbstractJiniTransactionManager
 * @see org.openspaces.core.GigaSpaceFactoryBean
 * @see org.openspaces.core.transaction.manager.JiniTransactionHolder
 * @see org.springframework.transaction.support.TransactionSynchronizationManager
 */
public class DefaultTransactionProvider implements TransactionProvider {

    private Object actualTransactionalContext;

    /**
     * Creates a new transaction provider. Will use the provided transactional context in order to
     * fetch the current running transaction.
     * 
     * @param actualTransactionalContext
     *            The transactional context to fetch the transaction by
     */
    public DefaultTransactionProvider(Object actualTransactionalContext) {
        this.actualTransactionalContext = actualTransactionalContext;
    }

    /**
     * Returns the current running transaction basde on the constructor provided transactional
     * context (Note that the passed transactional context is not used).
     * 
     * <p>
     * Uses Spring support for transactional resource registration in order to fetch the current
     * running transaction (or the {@link JiniTransactionHolder}. An example of Spring platform
     * transaction managers that register it are ones derived form
     * {@link org.openspaces.core.transaction.manager.AbstractJiniTransactionManager}.
     * 
     * <p>
     * If no transaction is found bound the the transactional context (provided in the constructor),
     * <code>null</code> is returned. This means that operations will execute without a
     * transaction.
     * 
     * @param transactionalContext
     *            Not Used. The transactional context used is the one provided in the constructor.
     * @return The current running transaction or <code>null</code> if no transaction is running
     */
    public Transaction.Created getCurrentTransaction(Object transactionalContext) {
        if (actualTransactionalContext == null) {
            return null;
        }

        JiniTransactionHolder txObject = (JiniTransactionHolder) TransactionSynchronizationManager.getResource(actualTransactionalContext);
        if (txObject != null && txObject.hasTransaction()) {
            return txObject.getTxCreated();
        }
        return null;
    }

    public int getCurrentTransactionIsolationLevel(Object transactionalContext) {
        if (actualTransactionalContext == null) {
            return TransactionDefinition.ISOLATION_DEFAULT;
        }

        JiniTransactionHolder txObject = (JiniTransactionHolder) TransactionSynchronizationManager.getResource(actualTransactionalContext);
        if (txObject != null && txObject.hasTransaction()) {
            return txObject.getIsolationLevel();
        }
        return TransactionDefinition.ISOLATION_DEFAULT;
    }
}
