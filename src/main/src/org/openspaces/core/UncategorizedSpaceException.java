package org.openspaces.core;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.UncategorizedDataAccessException;

/**
 * A GigaSpace based data accees exception that could not be translated to one of Spring
 * {@link DataAccessException} subclasses or one of our own subclasses.
 * 
 * @author kimchy
 */
public class UncategorizedSpaceException extends UncategorizedDataAccessException {

    private static final long serialVersionUID = -5799478121195480605L;

    public UncategorizedSpaceException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
