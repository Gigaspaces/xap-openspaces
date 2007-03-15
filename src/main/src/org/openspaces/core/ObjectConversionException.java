package org.openspaces.core;

import com.gigaspaces.converter.ConversionException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * Thrown when a conversion error occured. For example, when trying to convert a Pojo into an entry in the space.
 * A wrapper for {@link com.gigaspaces.converter.ConversionException}.
 *
 * @author kimchy
 */
public class ObjectConversionException extends InvalidDataAccessResourceUsageException {

    public ObjectConversionException(ConversionException e) {
        super(e.getMessage(), e);
    }
}
