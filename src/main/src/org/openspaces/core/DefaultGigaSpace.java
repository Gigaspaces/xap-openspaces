package org.openspaces.core;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.LeaseContext;
import org.openspaces.core.transaction.TransactionProvider;
import org.openspaces.core.exception.ExceptionTranslator;
import net.jini.space.JavaSpace;
import net.jini.core.transaction.Transaction;
import net.jini.core.lease.Lease;

/**
 * <p>Default implementation of {@link GigaSpace}. Constructed with {@link com.j_spaces.core.IJSpace},
 * {@link org.openspaces.core.transaction.TransactionProvider} and {@link org.openspaces.core.exception.ExceptionTranslator}.
 *
 * <p>Operations are delegated to {@link com.j_spaces.core.IJSpace} with transactions aquired using
 * {@link org.openspaces.core.transaction.TransactionProvider}. Any exceptions thrown during the
 * operations are translated using {@link org.openspaces.core.exception.ExceptionTranslator}.
 *
 * <p>Allows to set default timeouts for read and take operations and default lease for write operation.
 *
 * @author kimchy
 */
// TODO Allow to set default modifiers
public class DefaultGigaSpace implements GigaSpace {

    private IJSpace space;

    private TransactionProvider txProvider;

    private ExceptionTranslator exTranslator;

    private long defaultReadTimeout = JavaSpace.NO_WAIT;

    private long defaultTakeTimeout = JavaSpace.NO_WAIT;

    private long defaultWriteLease = Lease.FOREVER;

    /**
     * Constructs a new DefaultGigaSpace implementation.
     *
     * @param space        The space implementation to delegate operations to
     * @param txProvider   The transaction provider for declarative transaction ex.
     * @param exTranslator Exception translator to translate low level exceptions into GigaSpaces runtime exception
     */
    public DefaultGigaSpace(IJSpace space, TransactionProvider txProvider, ExceptionTranslator exTranslator) {
        this.space = space;
        this.txProvider = txProvider;
        this.exTranslator = exTranslator;
    }

    /**
     * Sets the default read timeout when executing {@link #read(Object)} or
     * {@link #readIfExists(Object)} operations.
     */
    public void setDefaultReadTimeout(long defaultReadTimeout) {
        this.defaultReadTimeout = defaultReadTimeout;
    }

    /**
     * Sets the default take timeout when executing {@link #take(Object)} or
     * {@link #takeIfExists(Object)} operations.
     */
    public void setDefaultTakeTimeout(long defaultTakeTimeout) {
        this.defaultTakeTimeout = defaultTakeTimeout;
    }

    /**
     * Sets the default write lease when executing {@link #write(Object)}.
     */
    public void setDefaultWriteLease(long defaultWriteLease) {
        this.defaultWriteLease = defaultWriteLease;
    }

    // GigaSpace Inteface Methods

    public IJSpace getSpace() {
        return this.space;
    }

    public TransactionProvider getTxProvider() {
        return this.txProvider;
    }


    public void clean() throws GigaSpaceException {
        try {
            space.clean();
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public Object read(Object template) throws GigaSpaceException {
        return read(template, defaultReadTimeout);
    }

    public Object read(Object template, long timeout) throws GigaSpaceException {
        try {
            return space.read(template, getCurrentTransaction(), timeout);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public Object readIfExists(Object template) throws GigaSpaceException {
        return readIfExists(template, defaultReadTimeout);
    }

    public Object readIfExists(Object template, long timeout) throws GigaSpaceException {
        try {
            return space.readIfExists(template, getCurrentTransaction(), timeout);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public Object[] readMultiple(Object template, int maxEntries) throws GigaSpaceException {
        try {
            return space.readMultiple(template, getCurrentTransaction(), maxEntries);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public Object take(Object template) throws GigaSpaceException {
        return take(template, defaultTakeTimeout);
    }

    public Object take(Object template, long timeout) throws GigaSpaceException {
        try {
            return space.take(template, getCurrentTransaction(), timeout);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public Object takeIfExists(Object template) throws GigaSpaceException {
        return takeIfExists(template, defaultTakeTimeout);
    }

    public Object takeIfExists(Object template, long timeout) throws GigaSpaceException {
        try {
            return space.takeIfExists(template, getCurrentTransaction(), timeout);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public Object[] takeMultiple(Object template, int maxEntries) throws GigaSpaceException {
        try {
            return space.takeMultiple(template, getCurrentTransaction(), maxEntries);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public LeaseContext write(Object entry) throws GigaSpaceException {
        return write(entry, defaultWriteLease);
    }

    public LeaseContext write(Object entry, long lease) throws GigaSpaceException {
        try {
            return space.write(entry, getCurrentTransaction(), lease);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public LeaseContext write(Object entry, long lease, long timeout, int modifiers) throws GigaSpaceException {
        try {
            return space.write(entry, getCurrentTransaction(), lease, timeout, modifiers);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    // Support methods

    private Transaction getCurrentTransaction() {
        Transaction.Created txCreated = txProvider.getCurrentTransaction(this);
        if (txCreated == null) {
            return null;
        }
        return txCreated.transaction;
    }

}
