package org.openspaces.core.exception;

import org.openspaces.core.UncategorizedGigaSpaceException;
import org.openspaces.core.UnusableEntryException;
import org.springframework.dao.DataAccessException;

/**
 * The default exception translator.
 * 
 * @author kimchy
 * @see org.openspaces.core.GigaSpace
 * @see org.openspaces.core.DefaultGigaSpace
 * @see org.openspaces.core.GigaSpaceFactoryBean
 */
public class DefaultExceptionTranslator implements ExceptionTranslator {

    /**
     * Translateds general JavaSpaces and Jini exceptions into a DataAccess exception.
     */
    public DataAccessException translate(Exception e) {
        if (e instanceof net.jini.core.entry.UnusableEntryException) {
            return new UnusableEntryException((net.jini.core.entry.UnusableEntryException) e);
        }
        return new UncategorizedGigaSpaceException(e.getMessage(), e);
    }
}
