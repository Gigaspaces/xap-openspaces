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

package org.openspaces.core.transaction.internal;

import com.gigaspaces.async.AsyncFutureListener;
import com.gigaspaces.async.AsyncResult;
import net.jini.core.transaction.Transaction;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.transaction.DefaultTransactionProvider;
import org.openspaces.core.transaction.manager.AbstractJiniTransactionManager;
import org.openspaces.core.transaction.manager.ExistingJiniTransactionManager;
import org.openspaces.core.transaction.manager.JiniTransactionHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * @author kimchy
 */
public class TransactionalAsyncFutureListener<T> implements AsyncFutureListener<T> {

    public static <T> AsyncFutureListener<T> wrapIfNeeded(AsyncFutureListener<T> listener, GigaSpace gigaSpace) {
        DefaultTransactionProvider txProvider = (DefaultTransactionProvider) gigaSpace.getTxProvider();
        JiniTransactionHolder holder = txProvider.getHolder();
        PlatformTransactionManager transactionManager = txProvider.getTransactionManager();
        if (holder == null || transactionManager == null) {
            return listener;
        } else {
            // here, we create a dummy transaction status (with its new transction set to true, so the commit/roolback
            // process will be perfomed). We also increase the ref count of the transcation, so only the last one will
            // be performed
            AbstractJiniTransactionManager.JiniTransactionObject jiniTransactionObject = new AbstractJiniTransactionManager.JiniTransactionObject();
            jiniTransactionObject.setJiniHolder(holder, false);
            TransactionStatus txStatus = new DefaultTransactionStatus(jiniTransactionObject, true, false, false, false, null);
            holder.incRef();
            return new TransactionalAsyncFutureListener<T>(listener, txStatus, transactionManager, holder);
        }
    }

    private final AsyncFutureListener<T> listener;

    private final PlatformTransactionManager transactionManager;

    private final TransactionStatus txStatus;

    private final JiniTransactionHolder holder;

    private TransactionalAsyncFutureListener(AsyncFutureListener<T> listener, TransactionStatus txStatus,
                                            PlatformTransactionManager transactionManager, JiniTransactionHolder holder) {
        this.listener = listener;
        this.txStatus = txStatus;
        this.transactionManager = transactionManager;
        this.holder = holder;
    }

    public void onResult(AsyncResult<T> asyncResult) {
        Transaction tx = holder.getTransaction();
        if (tx != null) {
            try {
                ExistingJiniTransactionManager.bindExistingTransaction(tx, true, true);
                listener.onResult(asyncResult);
                ExistingJiniTransactionManager.unbindExistingTransaction();
                transactionManager.commit(txStatus);
            } catch (RuntimeException e) {
                transactionManager.rollback(txStatus);
                throw e;
            }
        }
    }
}
