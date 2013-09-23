package org.openspaces.extensions;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.exception.DefaultExceptionTranslator;
import org.openspaces.core.exception.ExceptionTranslator;
import org.springframework.util.StringUtils;

import com.gigaspaces.client.ChangeException;
import com.gigaspaces.client.ChangeModifiers;
import com.gigaspaces.client.ChangeOperationResult;
import com.gigaspaces.client.ChangeResult;
import com.gigaspaces.client.ChangeSet;
import com.gigaspaces.client.ChangedEntryDetails;
import com.gigaspaces.query.IdQuery;
import com.gigaspaces.sync.change.IncrementOperation;

/**
 * Extensions for the {@link GigaSpace#change(com.gigaspaces.query.ISpaceQuery, ChangeSet, ChangeModifiers, long)} API which simplify common usage patterns.
 * @author eitany
 * @since 9.7
 */
public class ChangeExtension
{

    private static final ExceptionTranslator exceptionTranslator = new DefaultExceptionTranslator();
    
    /**
     * Atomically adds the given value to the current value of an entry's property.
     * @param gigaSpace the gigaspace which stores the entry.
     * @param idQuery id the query which is used to locate the entry.
     * @param path the path to the number property which is being modified.
     * @param delta the value to add.
     * @return the updated value, null of no matching entry found for the given id query. Therefore you must used the 
     * primitive wrapper types as the result value (e.g. Integer/Long) otherwise you may get {@link NullPointerException} if no entry was found.
     */
    public static <T,D extends Number> D addAndGet(GigaSpace gigaSpace,
            IdQuery<T> idQuery, String path, D delta) {
        return addAndGet(gigaSpace, idQuery, path, delta, ChangeModifiers.NONE, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * Atomically adds the given value to the current value of an entry's property.
     * @param gigaSpace the gigaspace which stores the entry.
     * @param idQuery id the query which is used to locate the entry.
     * @param path the path to the number property which is being modified.
     * @param delta the value to add.
     * @param timeout time to wait of the entry is locked under a transaction.
     * @param timeUnit units for the timeout.
     * @return the updated value, null of no matching entry found for the given id query. Therefore you must use the 
     * primitive wrapper types as the result value (e.g. Integer/Long) otherwise you may get {@link NullPointerException} if no entry was found.
     */
    public static <T, D extends Number> D addAndGet(GigaSpace gigaSpace,
            IdQuery<T> idQuery, String path, D delta, long timeout, TimeUnit timeUnit) {
        return addAndGet(gigaSpace, idQuery, path, delta, ChangeModifiers.NONE, timeout, timeUnit);
    }
    
    /**
     * Atomically adds the given value to the current value of an entry's property.
     * @param gigaSpace the gigaspace which stores the entry.
     * @param idQuery id the query which is used to locate the entry.
     * @param path the path to the number property which is being modified.
     * @param delta the value to add.
     * @param modifiers the change modifiers to use.
     * @param timeout time to wait of the entry is locked under a transaction.
     * @param timeUnit units for the timeout.
     * @return the updated value, null of no matching entry found for the given id query. Therefore you must use the 
     * primitive wrapper types as the result value (e.g. Integer/Long) otherwise you may get {@link NullPointerException} if no entry was found.
     */
    public static <T, D extends Number> D addAndGet(GigaSpace gigaSpace,
            IdQuery<T> idQuery, String path, D delta, ChangeModifiers modifiers, long timeout, TimeUnit timeUnit) {
        if (idQuery == null)
            throw new IllegalArgumentException("query cannot be null");
        if (!StringUtils.hasLength(path))
            throw new IllegalArgumentException("path cannot be null or empty");
        
        ChangeResult<T> changeResult = gigaSpace.change(idQuery, new ChangeSet().increment(path, delta), modifiers.add(ChangeModifiers.RETURN_DETAILED_RESULTS), timeUnit.toMillis(timeout));
        
        ChangedEntryDetails<T> changedEntryDetails = getSingleChangedEntryDetails(changeResult);
        if (changedEntryDetails == null)
            return null;
            
        ChangeOperationResult changeOperationResult = changedEntryDetails.getChangeOperationsResults().get(0);
        return (D)IncrementOperation.getNewValue(changeOperationResult);
    }
    
    private static <T> ChangedEntryDetails<T> getSingleChangedEntryDetails(ChangeResult<T> changeResult) {
        if (changeResult.getNumberOfChangedEntries() == 0)
            return null;
        
        if (changeResult.getNumberOfChangedEntries() != 1) {
            String message = "ambigious result - change operation have modified more than one entry, " +
                    "this could point to having entry with the same id in multiple partitions and using an id query without a routing value";
            Collection<?> changeEntries = changeResult.getResults();
            throw exceptionTranslator.translate(new ChangeException(message, (Collection<ChangedEntryDetails<?>>) changeEntries, Collections.EMPTY_LIST, Collections.EMPTY_LIST));    
        }
        
        return changeResult.getResults().iterator().next();
    }
}
