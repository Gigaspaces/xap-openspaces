package org.openspaces.core.exception;

import org.openspaces.core.GigaSpaceException;
import org.openspaces.core.SpaceOperationException;

/**
 * The default exception translator, currently translating all exceptions into a
 * {@link SpaceOperationException}.
 * 
 * @author kimchy
 * @see org.openspaces.core.GigaSpace
 * @see org.openspaces.core.DefaultGigaSpace
 * @see org.openspaces.core.GigaSpaceFactoryBean
 */
public class DefaultExceptionTranslator implements ExceptionTranslator {

    /**
     * Translateds general JavaSpaces and Jini exceptions into a GigaSpace exception. Currently
     * translates all exceptions into a {@link SpaceOperationException}.
     */
    public GigaSpaceException translate(Exception e) {
        return new SpaceOperationException(e.getMessage(), e);
    }
}
