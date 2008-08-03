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

package org.openspaces.core.executor.juc;

import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.async.AsyncFutureListener;
import com.gigaspaces.async.AsyncResult;
import com.gigaspaces.async.AsyncResultsReducer;
import org.openspaces.core.GigaSpace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author kimchy
 */
public class DefaultTaskExecutorService implements TaskExecutorService {

    private GigaSpace gigaSpace;

    private volatile boolean shutdown = false;

    public DefaultTaskExecutorService(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    public void execute(Runnable command) {
        if (command instanceof AsyncResultsReducer) {
            gigaSpace.execute(new RunnableDistributedTaskAdapter(command));
        } else {
            gigaSpace.execute(new RunnableTaskAdapter(command));
        }
    }

    public <T> AsyncFuture<T> submit(Callable<T> task) {
        AsyncFuture<T> result;
        if (task instanceof AsyncResultsReducer) {
            result = gigaSpace.execute(new CallableDistributedTaskAdapter(task));
        } else {
            result = gigaSpace.execute(new CallableTaskAdapter(task));
        }
        return result;
    }

    public AsyncFuture<?> submit(Runnable task) {
        AsyncFuture<?> result;
        if (task instanceof AsyncResultsReducer) {
            result = gigaSpace.execute(new RunnableDistributedTaskAdapter(task));
        } else {
            result = gigaSpace.execute(new RunnableTaskAdapter(task));
        }
        return result;
    }

    public <T> AsyncFuture<T> submit(Runnable task, T result) {
        AsyncFuture<T> future;
        if (task instanceof AsyncResultsReducer) {
            future = gigaSpace.execute(new RunnableDistributedTaskAdapter(task, (Serializable) result));
        } else {
            future = gigaSpace.execute(new RunnableTaskAdapter(task, (Serializable) result));
        }
        return future;
    }

    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks) throws InterruptedException {
        ArrayList<Future<T>> results = new ArrayList<Future<T>>(tasks.size());
        for (Callable<T> task : tasks) {
            results.add(submit(task));
        }
        for (Future<T> result : results) {
            try {
                result.get();
            } catch (ExecutionException e) {
                // ignore this exception, we reutrn all the completed tasks
            }
        }
        return results;
    }

    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        ArrayList<Future<T>> results = new ArrayList<Future<T>>(tasks.size());
        for (Callable<T> task : tasks) {
            results.add(submit(task));
        }
        for (Future<T> result : results) {
            try {
                result.get(timeout, unit);
            } catch (ExecutionException e) {
                // ignore this exception, we reutrn all the completed tasks
            } catch (TimeoutException e) {
                // ignore this exception, we reutrn all the completed tasks
            }
        }
        return results;
    }

    public <T> T invokeAny(Collection<Callable<T>> tasks) throws InterruptedException, ExecutionException {
        try {
            return invokeAny(tasks, -1, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            // should not get this
            throw new ExecutionException("Timeout waiting for result", e);
        }
    }

    public <T> T invokeAny(Collection<Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        WaitForAnyListener<T> listener = new WaitForAnyListener<T>(timeout == -1 ? timeout : unit.toMillis(timeout));
        ArrayList<AsyncFuture<T>> results = new ArrayList<AsyncFuture<T>>(tasks.size());
        for (Callable<T> task : tasks) {
            AsyncFuture<T> result;
            if (task instanceof AsyncResultsReducer) {
                result = gigaSpace.execute(new CallableDistributedTaskAdapter(task));
            } else {
                result = gigaSpace.execute(new CallableTaskAdapter(task));
            }
            result.setListener(listener);
            results.add(result);
        }
        T result = listener.waitForResult();
        for (AsyncFuture<T> future : results) {
            future.cancel(false);
        }
        return result;
    }

    public void shutdown() {
        this.shutdown = true;
    }

    public List<Runnable> shutdownNow() {
        this.shutdown = true;
        return new ArrayList<Runnable>();
    }

    public boolean isShutdown() {
        return this.shutdown;
    }

    public boolean isTerminated() {
        return this.shutdown;
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        // TODO can be implemented in a nicer way to comply with the interface on expecnse of overhead
        // of wrapping every returend async future
        return true;
    }

    private class WaitForAnyListener<T> implements AsyncFutureListener<T> {

        private final Object lock = new Object();

        private T result;

        private long timout = -1;

        public WaitForAnyListener() {
        }

        public WaitForAnyListener(long timout) {
            this.timout = timout;
        }

        public void onResult(AsyncResult<T> result) {
            synchronized (lock) {
                if (result.getException() == null) {
                    this.result = result.getResult();
                    lock.notifyAll();
                }
            }
        }

        public T waitForResult() throws InterruptedException {
            synchronized (lock) {
                if (result != null) {
                    return result;
                }
                if (timout == -1) {
                    lock.wait();
                } else {
                    lock.wait(timout);
                }
                if (result != null) {
                    return result;
                }
            }
        }
    }
}
