package org.openspaces.core.exception;

import org.openspaces.core.GigaSpaceException;

/**
 * Translates a low level JavaSpaces/Jini exception into a {@link GigaSpaceException} runtime
 * exception.
 * 
 * @author kimchy
 */
public interface ExceptionTranslator {

    /**
     * Translates a low leval exception into a {@link GigaSpaceException} rutime exception.
     * 
     * @param e
     *            The low level exception to translate
     * @return The translated exception
     */
    GigaSpaceException translate(Exception e);
}
