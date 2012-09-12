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

import net.jini.core.transaction.TransactionException;

/**
 * Thrown when an invalid transaction usage is performed. For example, when using a local
 * transaction manager with more than one space. Wraps a {@link net.jini.core.transaction.TransactionException}.
 *
 * @author kimchy
 */
public class InvalidTransactionUsageException extends TransactionDataAccessException {

    private static final long serialVersionUID = -2063417620859419314L;

    public InvalidTransactionUsageException(TransactionException e) {
        super(e);
    }
}
