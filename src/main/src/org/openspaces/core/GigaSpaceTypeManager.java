package org.openspaces.core;

import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.async.AsyncFutureListener;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.index.AddIndexesResult;
import com.gigaspaces.metadata.index.SpaceIndex;

/**
 * 
 * @author Niv Ingberg
 * @since 8.0
 */
public interface GigaSpaceTypeManager {

    /**
     * Gets the space type descriptor of the specified type. 
     * @param typeName Name of type.
     * @return Type descriptor of the type, if available (if not, returns null).
     */
    SpaceTypeDescriptor getTypeDescriptor(String typeName);

    /**
     * Gets the space type descriptor of the specified type. 
     * @param type Java class.
     * @return Type descriptor of the type, if available (if not, returns null).
     */
    SpaceTypeDescriptor getTypeDescriptor(Class<?> type);
    
    /**
     * Registers the specified space type descriptor in the space.
     * @param typeDescriptor
     */
    void registerTypeDescriptor(SpaceTypeDescriptor typeDescriptor);
    
    /**
     * Adds the specified index to the specified type.
     * 
     * @param typeName Name of type to enhance.
     * @param index Index to add.
     * @return A Future to monitor completion of the operation, whose <code>get()</code> method will return the add index result upon completion.
     */
    AsyncFuture<AddIndexesResult> asyncAddIndex(String typeName , SpaceIndex index);
    /**
     * Adds the specified index to the specified type.
     * 
     * @param typeName Name of type to enhance.
     * @param index Index to add.
     * @param listener A listener to be notified when a result arrives
     * @return A Future to monitor completion of the operation, whose <code>get()</code> method will return the add index result upon completion.
     */
    AsyncFuture<AddIndexesResult> asyncAddIndex(String typeName, SpaceIndex index, AsyncFutureListener<AddIndexesResult> listener);
    /**
     * Adds the specified indexes to the specified type.
     * 
     * @param typeName Name of type to enhance.
     * @param indexes Indexes to add.
     * @param listener A listener to be notified when a result arrives.
     * @return A Future to monitor completion of the operation, whose <code>get()</code> method will return the add index result upon completion.
     */
    AsyncFuture<AddIndexesResult> asyncAddIndexes(String typeName, SpaceIndex[] indexes, AsyncFutureListener<AddIndexesResult> listener);
}
