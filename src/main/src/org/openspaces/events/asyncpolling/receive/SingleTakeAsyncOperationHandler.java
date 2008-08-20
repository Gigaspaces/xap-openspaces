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

package org.openspaces.events.asyncpolling.receive;

import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.async.AsyncFutureListener;
import org.openspaces.core.GigaSpace;
import org.springframework.dao.DataAccessException;

/**
 * Performs a single take usign {@link org.openspaces.core.GigaSpace#asyncTake(Object)}.
 *
 * @author kimchy
 */
public class SingleTakeAsyncOperationHandler implements AsyncOperationHandler {

    /**
     * Performs a single take usign {@link org.openspaces.core.GigaSpace#asyncTake(Object)}.
     */
    public AsyncFuture asyncReceive(Object template, GigaSpace gigaSpace, long receiveTimeout, AsyncFutureListener listener) throws DataAccessException {
        return gigaSpace.asyncTake(template, receiveTimeout, listener);
    }
}
