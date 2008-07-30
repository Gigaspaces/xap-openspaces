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

import org.openspaces.core.executor.Task;

import java.io.Serializable;

/**
 * A simple impelemntation of delegating task that accepts the task to delegate to.
 *
 * @author kimchy
 */
public class SimpleDelegatingTask<T extends Serializable> implements DelegatingTask<T> {

    private Task<T> task;

    protected SimpleDelegatingTask() {
    }

    /**
     * Constructs a new simple delegating task with the task to delegate to.
     */
    public SimpleDelegatingTask(Task<T> task) {
        this.task = task;
    }

    /**
     * Returns the delegated task the task will execute to.
     */
    public Task<T> getDelegatedTask() {
        return this.task;
    }

    /**
     * Simply delegates the execution to the provided delegated task.
     */
    public T execute() throws Exception {
        return task.execute();
    }
}
