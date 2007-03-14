package org.openspaces.core.exception;

import org.springframework.dao.DataAccessException;

/**
 * Translates a low level JavaSpaces/Jini exception into a {@link DataAccessException} runtime
 * exception.
 * 
 * @author kimchy
 */
public interface ExceptionTranslator {

    /**
     * Translates a low leval exception into a {@link DataAccessException} rutime exception.
     * 
     * @param e
     *            The low level exception to translate
     * @return The translated exception
     */
    DataAccessException translate(Exception e);
}
