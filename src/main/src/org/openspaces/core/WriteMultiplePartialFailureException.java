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

import com.j_spaces.core.multiple.write.IWriteResult;
import net.jini.core.lease.Lease;
import org.openspaces.core.exception.ExceptionTranslator;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * This exception is thrown when write multiple is called and for some reason the
 * insertion of some of the entries fails. Wrpas {@link com.j_spaces.core.multiple.write.WriteMultiplePartialFailureException}.
 *
 * @author kimchy
 * @see com.j_spaces.core.multiple.write.WriteMultiplePartialFailureException
 * @see com.j_spaces.core.multiple.write.IWriteResult
 * @deprecated since 7.1. use {@link BatchWriteException}
 */
@Deprecated
public class WriteMultiplePartialFailureException extends InvalidDataAccessResourceUsageException {

    private static final long serialVersionUID = 1L;

    private final IWriteResult[] results;

    private final ExceptionTranslator exceptionTranslator;

    public WriteMultiplePartialFailureException(com.j_spaces.core.multiple.write.BatchWriteException cause, ExceptionTranslator exceptionTranslator) {
        super(cause.getMessage(), cause);
        this.exceptionTranslator = exceptionTranslator;
        results = new IWriteResult[cause.getResults().length];
        for (int i = 0; i < results.length; i++) {
            results[i] = new TranslatedWriteResult(cause.getResults()[i]);
        }
    }

    /**
     * @return an array of IResult objects.
     */
    public IWriteResult[] getResults() {
        return results;
    }

    private class TranslatedWriteResult implements IWriteResult {

        private static final long serialVersionUID = 1L;
        private final IWriteResult result;

        private TranslatedWriteResult(IWriteResult result) {
            this.result = result;
        }

        public ResultType getResultType() {
            return result.getResultType();
        }

        public Throwable getError() {
            Throwable t = result.getError();
            if (t == null) {
                return null;
            }
            return exceptionTranslator.translate(t);
        }

        public Lease getLease() {
            return result.getLease();
        }

        public boolean isError() {
            return result.isError();
        }
    }
}
