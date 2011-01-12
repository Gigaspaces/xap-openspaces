package org.openspaces.core;

import org.openspaces.core.exception.ExceptionTranslator;

import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.async.AsyncFutureListener;
import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import com.gigaspaces.internal.metadata.ITypeDesc;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.index.AddTypeIndexesResult;
import com.gigaspaces.metadata.index.ISpaceIndex;
import com.gigaspaces.metadata.index.SpaceIndex;

/**
 * Default implementation of {@link GigaSpaceTypeManager}.
 * This class is intended for internal usage only.
 * 
 * @author Niv Ingberg
 * @since 8.0
 */
public class DefaultGigaSpaceTypeManager implements GigaSpaceTypeManager {

    final private ISpaceProxy space;
    final private ExceptionTranslator exTranslator;

    public DefaultGigaSpaceTypeManager(ISpaceProxy space, ExceptionTranslator exTranslator) {
        this.space = space;
        this.exTranslator = exTranslator;
    }
    public SpaceTypeDescriptor getTypeDescriptor(String typeName)
    {
        try {
            return space.getTypeDescriptor(typeName);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }
    public SpaceTypeDescriptor getTypeDescriptor(Class<?> type)
    {
        try {
            if (type == null)
                throw new IllegalArgumentException("Argument cannot be null - 'type'.");
            return space.getTypeDescriptor(type.getName());
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }
    
    public void registerTypeDescriptor(SpaceTypeDescriptor typeDescriptor)
    {
        try {
            if (typeDescriptor == null)
                throw new IllegalArgumentException("Argument cannot be null - 'typeDescriptor'.");
            space.registerTypeDescriptor((ITypeDesc) typeDescriptor);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }
    
    public void registerTypeDescriptor(Class<?> type)
    {
        try {
            if (type == null)
                throw new IllegalArgumentException("Argument cannot be null - 'type'.");
            space.registerTypeDescriptor(type);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public AsyncFuture<AddTypeIndexesResult> asyncAddIndex(String typeName, SpaceIndex index) {
        return asyncAddIndexes(typeName, new SpaceIndex[] {index}, null);
    }
    public AsyncFuture<AddTypeIndexesResult> asyncAddIndex(String typeName, SpaceIndex index,
            AsyncFutureListener<AddTypeIndexesResult> listener) {
        return asyncAddIndexes(typeName, new SpaceIndex[] {index}, listener);
    }
    public AsyncFuture<AddTypeIndexesResult> asyncAddIndexes(String typeName, SpaceIndex[] indexes,
            AsyncFutureListener<AddTypeIndexesResult> listener) {
        try {
            // Validate:
            if (typeName == null || typeName.length() == 0)
                throw new IllegalArgumentException("Argument cannot be null or empty - 'typeName'.");
            if (indexes == null || indexes.length == 0)
                throw new IllegalArgumentException("Argument cannot be null or empty - 'indexes'.");
            // Convert indexes:
            ISpaceIndex[] internalIndexes = new ISpaceIndex[indexes.length];
            for (int i=0 ; i < indexes.length ; i++) {              
                if (indexes[i] == null)
                    throw new IllegalArgumentException("Index at position #" + i + " is null.");
                if (!(indexes[i] instanceof ISpaceIndex))
                    throw new IllegalArgumentException("Index at position #" + i + " is of an unsupported type - " + indexes[i].getClass().getName());
                internalIndexes[i] = (ISpaceIndex) indexes[i];
            }
            // Execute:
            return space.asyncAddIndexes(typeName, internalIndexes, listener);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    } 
}
