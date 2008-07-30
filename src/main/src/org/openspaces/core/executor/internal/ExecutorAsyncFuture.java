package org.openspaces.core.executor.internal;

import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.async.AsyncFutureListener;
import com.gigaspaces.async.AsyncResult;
import org.openspaces.core.internal.InternalGigaSpace;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author kimchy
 */
public class ExecutorAsyncFuture<T> implements AsyncFuture<T> {

    private InternalGigaSpace gigaSpace;

    private AsyncFuture<T> future;

    public ExecutorAsyncFuture(AsyncFuture<T> future, InternalGigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
        this.future = future;
    }

    public void setListener(AsyncFutureListener<T> listener) {
        future.setListener(new ExecutorAsyncListener<T>(listener));
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

    private class ExecutorAsyncListener<T> implements AsyncFutureListener<T> {

        private final AsyncFutureListener<T> listener;

        private ExecutorAsyncListener(AsyncFutureListener<T> listener) {
            this.listener = listener;
        }

        public void onResult(AsyncResult<T> result) {
            gigaSpace.getAsyncExecutorService().submit(new ListenerExecutionRunnable<T>(listener, result));
        }
    }

    private class ListenerExecutionRunnable<T> implements Runnable {

        private final AsyncFutureListener<T> listener;

        private final AsyncResult<T> result;

        private ListenerExecutionRunnable(AsyncFutureListener<T> listener, AsyncResult<T> result) {
            this.listener = listener;
            this.result = result;
        }

        public void run() {
            listener.onResult(result);
        }
    }
}
