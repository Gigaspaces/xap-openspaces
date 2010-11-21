package org.openspaces.core;

import com.gigaspaces.metadata.SpaceTypeDescriptor;

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
}
