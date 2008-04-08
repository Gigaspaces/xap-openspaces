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

import com.j_spaces.core.multiple.write.IResult;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * This exception is thrown when write multiple is called and for
 * some reason the insertion of some of the entries fails.
 * It is wrapping the corresponding com.j_spaces.core.multiple.write.WriteMultipleOperationPartialFailException
 *
 * @see com.j_spaces.core.multiple.write.WriteMultipleOperationPartialFailException
 * @see com.j_spaces.core.multiple.write.IResult
 *
 * @author uri 
 */
public class WriteMultipleOperationPartialFailException extends DataRetrievalFailureException {
    private final IResult[] results;

    public WriteMultipleOperationPartialFailException(com.j_spaces.core.multiple.write.WriteMultipleOperationPartialFailException cause) {
        super("Partial failure of writeMultipule operation", cause);
        results = cause.getResults();
    }

    /**
     * Returns an array of IResult objects. Each result corresponds with the object in the input array
     * of the original writeMultiple operation. If successful, a lease for that object is returned, otherwise
     * an exception describing the failure specific to that object is returned
     * @return an array of IResult objects.
     */
    public IResult[] getResults() {
        return results;
    }
}
