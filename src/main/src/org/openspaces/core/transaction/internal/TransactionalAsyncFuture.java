package org.openspaces.core.transaction.internal;

import com.gigaspaces.async.AsyncFuture;
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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author kimchy
 */
public class TransactionalAsyncFuture<T> implements AsyncFuture<T> {

    private AsyncFuture<T> future;

    private GigaSpace gigaSpace;

    public TransactionalAsyncFuture(AsyncFuture<T> future, GigaSpace gigaSpace) {
        this.future = future;
        this.gigaSpace = gigaSpace;
    }

    public void setListener(AsyncFutureListener<T> listener) {
        DefaultTransactionProvider txProvider = (DefaultTransactionProvider) gigaSpace.getTxProvider();
        JiniTransactionHolder holder = txProvider.getHolder();
        PlatformTransactionManager transactionManager = txProvider.getTransactionManager();
        if (holder == null || transactionManager == null) {
            future.setListener(listener);
        } else {
            AbstractJiniTransactionManager.JiniTransactionObject jiniTransactionObject = new AbstractJiniTransactionManager.JiniTransactionObject();
            jiniTransactionObject.setJiniHolder(holder, false);
            TransactionStatus txStatus = new DefaultTransactionStatus(jiniTransactionObject, true, false, false, false, null);
            holder.incRef();
            future.setListener(new TransactionalAsyncFutureListener<T>(listener, txStatus, transactionManager, holder));
        }
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }

    public boolean isCancelled() {
        return future.isCancelled();
    }

    public boolean isDone() {
        return future.isDone();
    }

    public T get() throws InterruptedException, ExecutionException {
        return future.get();
    }

    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }

    private static class TransactionalAsyncFutureListener<T> implements AsyncFutureListener<T> {

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
}
