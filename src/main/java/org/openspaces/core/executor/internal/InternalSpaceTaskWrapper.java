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
import net.jini.core.transaction.Transaction;
import org.openspaces.core.executor.Task;
import org.openspaces.core.transaction.manager.ExistingJiniTransactionManager;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 * An internal implemenation of {@link SpaceTask} that wraps the actual {@link org.openspaces.core.executor.Task}
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
        task = (Task<T>) in.readObject();
        routing = in.readObject();
    }
}
