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

package org.openspaces.events.polling.receive;

import org.openspaces.core.GigaSpace;
import org.springframework.dao.DataAccessException;

/**
 * First tries and perform a {@link org.openspaces.core.GigaSpace#takeMultiple(Object,int)} using
 * the provided template and configured maxEntries (defaults to <code>50</code>). If no values
 * are returned, will perform a blocking take operation using
 * {@link org.openspaces.core.GigaSpace#take(Object,long)}.
 * 
 * @author kimchy
 */
public class MultiTakeReceiveOperationHandler implements ReceiveOperationHandler {

    private static final int DEFAULT_MAX_ENTRIES = 50;

    private int maxEntries = DEFAULT_MAX_ENTRIES;

    /**
     * Sets the max entries the inital take multiple operation will perform.
     */
    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    /**
     * First tries and perform a {@link org.openspaces.core.GigaSpace#takeMultiple(Object,int)}
     * using the provided template and configured maxEntries (defaults to <code>50</code>). If no
     * values are returned, will perform a blocking take operation using
     * {@link org.openspaces.core.GigaSpace#take(Object,long)}.
     */
    public Object receive(Object template, GigaSpace gigaSpace, long receiveTimeout) throws DataAccessException, InterruptedException {
        Object[] results = gigaSpace.takeMultiple(template, maxEntries);
        if (results != null && results.length > 0) {
            return results;
        }
        return gigaSpace.take(template, receiveTimeout);
    }
}
