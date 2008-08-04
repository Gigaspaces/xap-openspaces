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

package org.openspaces.core.executor.support;

import com.gigaspaces.async.AsyncFutureListener;
import com.gigaspaces.async.AsyncResult;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author kimchy
 */
public class WaitForAnyListener<T> implements AsyncFutureListener<T> {

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition resultArrived = lock.newCondition();

    private final int numberOfResults;

    private int numberOfResultsArrived;

    private T result;

    private final AsyncFutureListener<T> listener;

    public WaitForAnyListener(int numberOfResults) {
        this(numberOfResults, null);
    }

    public WaitForAnyListener(int numberOfResults, AsyncFutureListener<T> listener) {
        this.numberOfResults = numberOfResults;
        this.listener = listener;
    }

    public void onResult(AsyncResult<T> result) {
        if (listener != null) {
            listener.onResult(result);
        }
        lock.lock();
        try {
            if (result.getException() == null) {
                this.result = result.getResult();
                resultArrived.signalAll();
            }
            if (++numberOfResultsArrived == numberOfResults) {
                resultArrived.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    public T waitForResult() throws InterruptedException {
        try {
            return waitForResult(-1, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            // shoudl not happen
            throw new RuntimeException("Should not occur as we are waiting forever");
        }
    }

    public T waitForResult(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
        lock.lock();
        try {
            if (result != null) {
                return result;
            }
            if (timeout == -1) {
                resultArrived.await();
            } else {
                resultArrived.await(timeout, unit);
            }
            if (result != null) {
                return result;
            }
        } finally {
            lock.unlock();
        }
        throw new TimeoutException("Timeout waiting for result for [" + timeout + "]");
    }
}
