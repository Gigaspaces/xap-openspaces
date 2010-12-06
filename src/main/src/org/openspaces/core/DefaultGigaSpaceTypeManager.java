package org.openspaces.core;

import org.openspaces.core.exception.ExceptionTranslator;

import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import com.gigaspaces.internal.metadata.ITypeDesc;
import com.gigaspaces.metadata.SpaceTypeDescriptor;

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
}
