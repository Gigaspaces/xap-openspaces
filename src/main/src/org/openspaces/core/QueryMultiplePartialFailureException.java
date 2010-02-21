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

package org.openspaces.core;

import org.openspaces.core.exception.ExceptionTranslator;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * Thrown when not all of the requested number of entries are returned and one or more cluster members returned with exception.<br>
 * QueryMultiplePartialFailureException contains both the entries that were successfully returned and the exceptions that occurred during operation execution.
 * <p>In order to disable this behavior, the following modifier should be passed {@link com.j_spaces.core.client.ReadModifiers#IGNORE_PARTIAL_FAILURE}
 * to either {@link org.openspaces.core.GigaSpace#readMultiple(Object, int, int)} or.
 *
 * @author kimchy
 */
public class QueryMultiplePartialFailureException extends InvalidDataAccessResourceUsageException {

    private final Object[] results;

    private final Throwable[] causes;

    public QueryMultiplePartialFailureException(com.j_spaces.core.multiple.query.QueryMultiplePartialFailureException cause,
            ExceptionTranslator exceptionTranslator) {
        super(cause.getMessage(), cause);
        this.results = cause.getResults();
        if (cause.getCauses() == null) {
            this.causes = null;
        } else {
            this.causes = new Throwable[cause.getCauses().length];
            for (int i = 0; i < cause.getCauses().length; i++) {
                this.causes[i] = exceptionTranslator.translate(cause.getCauses()[i]);
            }
        }
    }

    /**
     * Returnst the results from the cluster members that were available.
     */
    public Object[] getResults() {
        return results;
    }

    /**
     * Returns the exceptions raised from the cluster members that were not available.
     */
    public Throwable[] getCauses() {
        return causes;
    }
}
