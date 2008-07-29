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

import com.gigaspaces.async.AsyncResult;
import com.gigaspaces.async.AsyncResultsModerator;
import com.gigaspaces.executor.DistributedSpaceTask;
import org.openspaces.core.executor.DistributedTask;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.List;

/**
 * @author kimchy
 */
public class InternalDistributedSpaceTaskWrapper<T, R> extends InternalSpaceTaskWrapper<T>
        implements DistributedSpaceTask<T, R>, AsyncResultsModerator<T> {

    public InternalDistributedSpaceTaskWrapper() {
    }

    public InternalDistributedSpaceTaskWrapper(DistributedTask<T, R> task) {
        super(task, null);
    }

    @SuppressWarnings("unchecked")
    public R reduce(List<AsyncResult<T>> asyncResults) throws Exception {
        return (R) ((DistributedTask) getTask()).reduce(asyncResults);
    }

    public Decision moderate(AsyncResult<T> currentResult, Collection<AsyncResult<T>> receivedResults, int totalExpextedResults) {
        if (getTask() instanceof AsyncResultsModerator) {
            return ((AsyncResultsModerator) getTask()).moderate(currentResult, receivedResults, totalExpextedResults);
        }
        return Decision.CONTINUE;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }
}