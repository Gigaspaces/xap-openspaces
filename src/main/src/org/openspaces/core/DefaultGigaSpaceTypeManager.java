package org.openspaces.core;

import org.openspaces.core.exception.ExceptionTranslator;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
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
            return space.getSpaceTypeDescriptor(typeName);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }
    public SpaceTypeDescriptor getTypeDescriptor(Class<?> type)
    {
        try {
            if (type == null)
                throw new IllegalArgumentException("Argument cannot be null - 'type'.");
            return space.getSpaceTypeDescriptor(type.getName());
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }
    
    public void registerTypeDescriptor(SpaceTypeDescriptor typeDescriptor)
    {
        try {
            if (typeDescriptor == null)
                throw new IllegalArgumentException("Argument cannot be null - 'typeDescriptor'.");
            space.snapshot(new SpaceDocument(typeDescriptor));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    } 
}
