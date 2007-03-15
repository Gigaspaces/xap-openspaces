package org.openspaces.core.exception;

import com.gigaspaces.converter.ConversionException;
import com.j_spaces.core.UnknownTypeException;
import com.j_spaces.core.client.EntryVersionConflictException;
import org.openspaces.core.*;
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
    public DataAccessException translate(Throwable e) {
        DataAccessException dae = internalTranslate(e);
        if (dae != null) {
            return dae;
        }
        return new UncategorizedSpaceException(e.getMessage(), e);
    }

    private DataAccessException internalTranslate(Throwable e) {
        if (e instanceof net.jini.space.InternalSpaceException) {
            if (((net.jini.space.InternalSpaceException) e).nestedException != null) {
                DataAccessException dae = internalTranslate(((net.jini.space.InternalSpaceException) e).nestedException);
                if (dae != null) {
                    return dae;
                }
            }
            return new InternalSpaceException((net.jini.space.InternalSpaceException) e);
        }

        if (e instanceof ConversionException) {
            return new ObjectConversionException((ConversionException) e);
        }

        // UnusableEntryException and its subclasses
        if (e instanceof EntryVersionConflictException) {
            return new SpaceOptimisticLockingFailureException((EntryVersionConflictException) e);
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

        // UnknownTypeException
        if (e instanceof UnknownTypeException) {
            return new InvalidTypeDataAccessException((UnknownTypeException) e);
        }
        return null;
    }
}
