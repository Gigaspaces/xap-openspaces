/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openspaces.core;

import java.util.ArrayList;
import java.util.Collection;

import org.openspaces.core.exception.ExceptionTranslator;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import com.gigaspaces.client.ChangedEntryDetails;
import com.gigaspaces.client.FailedChangedEntryDetails;
import com.gigaspaces.internal.client.FailedChangeResultImpl;


/**
 * Thrown when an error occurred while performing a {@link GigaSpace#change(com.gigaspaces.query.IdQuery, com.gigaspaces.client.ChangeSet, com.gigaspaces.client.ChangeModifiers, long)} operation.
 * @author eitany
 * @since 9.1
 */
public class ChangeException extends InvalidDataAccessResourceUsageException {

    private static final long serialVersionUID = 1L;
    
    private final Collection<ChangedEntryDetails<?>> changedEntries;
    private final Collection<FailedChangedEntryDetails> translatedEntriesFailedToChange;
    private final Collection<Throwable> translatedErrors;
    
    public ChangeException(com.gigaspaces.client.ChangeException changeException, ExceptionTranslator exceptionTranslator) {
        super(changeException.getMessage(), changeException);
        this.changedEntries = changeException.getSuccesfullChanges();
        translatedEntriesFailedToChange = new ArrayList<FailedChangedEntryDetails>(changeException.getFailedChanges().size());
        for (FailedChangedEntryDetails failedChangeEntryResult : changeException.getFailedChanges()) {
            translatedEntriesFailedToChange.add(new FailedChangeResultImpl(failedChangeEntryResult.getTypeName(),
                    failedChangeEntryResult.getId(), failedChangeEntryResult.getVersion(), translateException(exceptionTranslator, failedChangeEntryResult.getCause())));
        }
        translatedErrors = new ArrayList<Throwable>(changeException.getErrors().size());
        for (Throwable error : changeException.getErrors()) {
            translatedErrors.add(translateException(exceptionTranslator, error));
        }
    }

    private static Exception translateException(ExceptionTranslator exceptionTranslator,
            Throwable error) {
        Exception translatedException;
        try {
            translatedException = exceptionTranslator.translate(error);
        } catch (Exception e) {
            translatedException = e;
        }
        return translatedException;
    }
    
    /**
     * Returns the successfully done changes.
     */
    public Collection<ChangedEntryDetails<?>> getSuccesfullChanges()
    {
        return changedEntries;
    }
    
    /**
     * Returns the entries that failed to change result.
     */
    public Collection<FailedChangedEntryDetails> getFailedChanges()
    {
        return translatedEntriesFailedToChange;
    }
    
    /**
     * Returns the failed changes.
     */
    public Collection<Throwable> getErrors()
    {
        return translatedErrors;
    }
}
