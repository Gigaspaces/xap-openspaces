package org.openspaces.core;

import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.async.AsyncFutureListener;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.index.AddTypeIndexesResult;
import com.gigaspaces.metadata.index.SpaceIndex;

/**
 * Interface encapsulating operations for getting and managing space type descriptors.
 * 
 * Use {@link GigaSpace#getTypeManager()} to retrieve the type manager of a <code>GigaSpace</code> instance.
 * 
 *  @see org.openspaces.core.GigaSpace
 *  @see com.gigaspaces.metadata.SpaceTypeDescriptor
 *  @see com.gigaspaces.metadata.SpaceTypeDescriptorBuilder
 *  @see com.gigaspaces.metadata.index.SpaceIndex
 *  @see com.gigaspaces.metadata.index.SpaceIndexFactory
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
     * Creates a type descriptor for the specified type and registers it in the space.
     * @param type
     */
    void registerTypeDescriptor(Class<?> type);

    /**
     * Adds the specified index to the specified type.
     * 
     * @param typeName Name of type to enhance.
     * @param index Index to add.
     * @return A Future to monitor completion of the operation, whose <code>get()</code> method will return the add index result upon completion.
     */
    AsyncFuture<AddTypeIndexesResult> asyncAddIndex(String typeName , SpaceIndex index);
    /**
     * Adds the specified index to the specified type.
     * 
     * @param typeName Name of type to enhance.
     * @param index Index to add.
     * @param listener A listener to be notified when a result arrives
     * @return A Future to monitor completion of the operation, whose <code>get()</code> method will return the add index result upon completion.
     */
    AsyncFuture<AddTypeIndexesResult> asyncAddIndex(String typeName, SpaceIndex index, AsyncFutureListener<AddTypeIndexesResult> listener);
    /**
     * Adds the specified indexes to the specified type.
     * 
     * @param typeName Name of type to enhance.
     * @param indexes Indexes to add.
     * @param listener A listener to be notified when a result arrives.
     * @return A Future to monitor completion of the operation, whose <code>get()</code> method will return the add index result upon completion.
     */
    AsyncFuture<AddTypeIndexesResult> asyncAddIndexes(String typeName, SpaceIndex[] indexes, AsyncFutureListener<AddTypeIndexesResult> listener);
}
