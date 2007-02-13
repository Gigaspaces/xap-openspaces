package org.openspaces.core.exception;

import org.openspaces.core.GigaSpaceException;
import org.openspaces.core.SpaceOperationException;

/**
 * The default exception translator, currently translating all exceptions
 * into a {@link org.openspaces.core.SpaceOperationException}.
 *
 * @author kimchy
 */
public class DefaultExceptionTranslator implements ExceptionTranslator {

    public GigaSpaceException translate(Exception e) {
        return new SpaceOperationException(e.getMessage(), e);
    }
}
