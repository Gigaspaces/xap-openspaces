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

import com.gigaspaces.async.AsyncResult;
import com.gigaspaces.async.AsyncResultFilter;
import org.openspaces.core.executor.Task;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

/**
 * A max distrubuted task that accepts a {@link org.openspaces.core.executor.Task} to delegate
 * the actual execution to and implements the {@link #reduce(java.util.List)} operation.
 *
 * <p>By default, throws an exception if one of the execution fails. {@link #ignoreExceptions()}
 * can be called to only perform the operation on all the successful operations, ignoring the failed
 * ones.
 *
 * <p>Can accept an optional {@link com.gigaspaces.async.AsyncResultFilter}.
 *
 * @author kimchy
 * @see MaxReducer
 */
public class MaxTask<T extends Number> extends AbstractDelegatingDistributedTask<T, T> implements Externalizable {

    private static final long serialVersionUID = 8467204680463687573L;
    
    private transient MaxReducer<T> reducer;

    public MaxTask() {
        super();
    }

    /**
     * Constructs a new sum distributed task that delegates the actual execution to
     * th provided task.
     *
     * @param task The task to delegate the execution to.
     */
    public MaxTask(Class<T> reduceType, Task<T> task) throws IllegalArgumentException {
        super(task);
        this.reducer = new MaxReducer<T>(reduceType);
    }

    /**
     * Constructs a new sum distributed task that delegates the actual execution to
     * th provided task.
     *
     * @param task   The task to delegate the execution to.
     * @param filter A result filter to be called for each result
     */
    public MaxTask(Class<T> reduceType, Task<T> task, AsyncResultFilter<T> filter) throws IllegalArgumentException {
        super(task, filter);
        this.reducer = new MaxReducer<T>(reduceType);
    }

    /**
     * Sets the {@link #reduce(java.util.List)} to ignore failed invocations.
     */
    public MaxTask ignoreExceptions() {
        this.reducer.ignoreExceptions();
        return this;
    }

    /**
     * Performs the actual sum operation by delegating to its internal
     * {@link SumReducer}.
     */
    public T reduce(List<AsyncResult<T>> results) throws Exception {
        return reducer.reduce(results);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super._writeExternal(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super._readExternal(in);
    }
}