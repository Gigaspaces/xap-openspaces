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
 * A default implementation of a reducer that sums all the float reuslts into
 * a double result.
 *
 * @author kimchy
 */
public class SumFloatReducer implements AsyncResultsReducer<Float, Double> {

    private volatile boolean ignoreExceptions;

    /**
     * Causes the reducer to ignore exceptions and just sum the results that succeeded.
     */
    public SumFloatReducer ignoreExceptions() {
        this.ignoreExceptions = true;
        return this;
    }

    /**
     * Sums all the float values of the results into a double. Will throw an exception
     * by default. Will only sum the successful results if {@link #ignoreExceptions()} is
     * called.
     */
    public Double reduce(List<AsyncResult<Float>> results) throws Exception {
        double sum = 0;
        for (AsyncResult<Float> result : results) {
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