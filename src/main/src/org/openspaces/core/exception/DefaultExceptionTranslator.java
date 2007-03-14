package org.openspaces.core.exception;

import com.j_spaces.core.UnknownTypeException;
import com.j_spaces.core.client.EntryVersionConflictException;
import net.jini.space.InternalSpaceException;
import org.openspaces.core.EntryAlreadyInSpaceException;
import org.openspaces.core.EntryNotInSpaceException;
import org.openspaces.core.InvalidFifoClassException;
import org.openspaces.core.InvalidFifoTemplateException;
import org.openspaces.core.InvalidTypeDataAccessException;
import org.openspaces.core.UncategorizedSpaceException;
import org.openspaces.core.UnusableEntryException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;

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
    public DataAccessException translate(Throwable e) {
        if (e instanceof InternalSpaceException) {
            return translate(((InternalSpaceException) e).nestedException);
        }
        if (e instanceof EntryVersionConflictException) {
            return new OptimisticLockingFailureException(e.getMessage(), e);
        }
        if (e instanceof com.j_spaces.core.client.EntryNotInSpaceException) {
            return new EntryNotInSpaceException((com.j_spaces.core.client.EntryNotInSpaceException) e);
        }
        if (e instanceof com.j_spaces.core.client.EntryAlreadyInSpaceException) {
            return new EntryAlreadyInSpaceException((com.j_spaces.core.client.EntryAlreadyInSpaceException) e);
        }
        if (e instanceof com.j_spaces.core.InvalidFifoTemplateException) {
            return new InvalidFifoTemplateException((com.j_spaces.core.InvalidFifoTemplateException) e);
        }
        if (e instanceof com.j_spaces.core.InvalidFifoClassException) {
            return new InvalidFifoClassException((com.j_spaces.core.InvalidFifoClassException) e);
        }
        if (e instanceof net.jini.core.entry.UnusableEntryException) {
            return new UnusableEntryException((net.jini.core.entry.UnusableEntryException) e);
        }
        if (e instanceof UnknownTypeException) {
            return new InvalidTypeDataAccessException((UnknownTypeException) e);
        }
        return new UncategorizedSpaceException(e.getMessage(), e);
    }
}
