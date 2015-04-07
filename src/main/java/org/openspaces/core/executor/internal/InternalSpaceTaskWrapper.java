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

package org.openspaces.core.executor.internal;

import com.gigaspaces.annotation.pojo.SpaceRouting;
import com.gigaspaces.executor.SpaceTask;
import com.gigaspaces.executor.SpaceTaskWrapper;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.SpaceContext;
import com.j_spaces.kernel.ClassLoaderHelper;
import net.jini.core.transaction.Transaction;
import org.openspaces.core.executor.Task;
import org.openspaces.core.transaction.manager.ExistingJiniTransactionManager;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * An internal implementation of {@link SpaceTask} that wraps the actual {@link org.openspaces.core.executor.Task}
 * to be executed.
 *
 * @author kimchy
 */
public class InternalSpaceTaskWrapper<T extends Serializable> implements SpaceTask<T>, SpaceTaskWrapper, Externalizable {

    private static final long serialVersionUID = -7391977361461247102L;

    private Task<T> task;

    private Object routing;

    public InternalSpaceTaskWrapper() {
    }

    public InternalSpaceTaskWrapper(Task<T> task, Object routing) {
        this.task = task;
        this.routing = routing;
    }

    public T execute(IJSpace space, Transaction tx) throws Exception {
        if (tx != null) {
            try {
                ExistingJiniTransactionManager.bindExistingTransaction(tx);
                return task.execute();
            } finally {
                ExistingJiniTransactionManager.unbindExistingTransaction();
            }
        }
        return task.execute();
    }

    public Object getWrappedTask() {
        return getTask();
    }

    public Task<T> getTask() {
        return task;
    }

    @SpaceRouting
    public Object getRouting() {
        return routing;
    }

    public void setRouting(Object routing) {
        this.routing = routing;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(task);
        out.writeObject(routing);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        task = readTaskUsingFreshClassLoader(in);
        routing = in.readObject();
    }

    /**
     * Tasks are loaded with a fresh class loader.
     * When the task is done this fresh class loader is removed.
     * This will make it possible to load a modified version of this class
     * GS-12351- Running Distributed Task can throw ClassNotFoundException, if this task was loaded by a client that already shutdown and the class has more dependencies to load.
     * GS-12352 - Distributed Task class is not unloaded after the task finish.
     *
     * @throws ClassNotFoundException
     * @throws IOException
     * @see com.gigaspaces.internal.server.space.SpaceImpl#executeTask(SpaceTask, Transaction, SpaceContext, boolean)
     */
    private Task<T> readTaskUsingFreshClassLoader(ObjectInput in) throws ClassNotFoundException, IOException {
        ClassLoader old = ClassLoaderHelper.getContextClassLoader();
        try {
            ClassLoaderHelper.setContextClassLoader(new URLClassLoader(new URL[]{}, old), true);
            //noinspection unchecked
            return (Task<T>) in.readObject();
        } finally {
            ClassLoaderHelper.setContextClassLoader(old, true);

        }
    }
}
