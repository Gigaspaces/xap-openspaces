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
import com.gigaspaces.async.AsyncResultsReducer;

import java.util.List;

/**
 * A default implementation of a reducer that sums all the integer reuslts into
 * a long result.
 *
 * @author kimchy
 */
public class SumIntegerReducer implements AsyncResultsReducer<Integer, Long> {

    private volatile boolean ignoreExceptions;

    /**
     * Causes the reducer to ignore exceptions and just sum the results that succeeded.
     */
    public SumIntegerReducer ignoreExceptions() {
        this.ignoreExceptions = true;
        return this;
    }

    /**
     * Sums all the integer values of the results into a long. Will throw an exception
     * by default. Will only sum the successful results if {@link #ignoreExceptions()} is
     * called.
     */
    public Long reduce(List<AsyncResult<Integer>> results) throws Exception {
        long sum = 0;
        for (AsyncResult<Integer> result : results) {
            if (result.getException() != null) {
                if (ignoreExceptions) {
                    continue;
                } else {
                    throw result.getException();
                }
            }
            sum += result.getResult();
        }
        return sum;
    }
}
