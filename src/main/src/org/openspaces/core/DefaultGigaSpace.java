package org.openspaces.core;

import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.space.JavaSpace;

import org.openspaces.core.exception.ExceptionTranslator;
import org.openspaces.core.transaction.TransactionProvider;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionDefinition;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.LeaseContext;
import com.j_spaces.core.client.Query;
import com.j_spaces.core.client.ReadModifiers;

/**
 * Default implementation of {@link GigaSpace}. Constructed with {@link com.j_spaces.core.IJSpace},
 * {@link org.openspaces.core.transaction.TransactionProvider} and
 * {@link org.openspaces.core.exception.ExceptionTranslator}.
 * 
 * <p>
 * Operations are delegated to {@link com.j_spaces.core.IJSpace} with transactions aquired using
 * {@link org.openspaces.core.transaction.TransactionProvider}. Any exceptions thrown during the
 * operations are translated using {@link org.openspaces.core.exception.ExceptionTranslator}.
 * 
 * <p>
 * Allows to set default timeouts for read and take operations and default lease for write
 * operation.
 * 
 * @author kimchy
 */
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
     * @param space
     *            The space implementation to delegate operations to
     * @param txProvider
     *            The transaction provider for declarative transaction ex.
     * @param exTranslator
     *            Exception translator to translate low level exceptions into GigaSpaces runtime
     *            exception
     * @param defaultIsolationLevel
     *            The default isolation level for read operations without modifiers. Maps to
     *            {@link org.springframework.transaction.TransactionDefinition#getIsolationLevel()}
     *            levels values.
     */
    public DefaultGigaSpace(IJSpace space, TransactionProvider txProvider, ExceptionTranslator exTranslator,
            int defaultIsolationLevel) {
        this.space = space;
        this.txProvider = txProvider;
        this.exTranslator = exTranslator;
        // set the default read take modifiers according to the default isolation level
        switch (defaultIsolationLevel) {
        case TransactionDefinition.ISOLATION_DEFAULT:
            break;
        case TransactionDefinition.ISOLATION_READ_UNCOMMITTED:
            space.setReadModifiers(ReadModifiers.DIRTY_READ);
            break;
        case TransactionDefinition.ISOLATION_READ_COMMITTED:
            space.setReadModifiers(ReadModifiers.READ_COMMITTED);
            break;
        case TransactionDefinition.ISOLATION_REPEATABLE_READ:
            space.setReadModifiers(ReadModifiers.REPEATABLE_READ);
            break;
        case TransactionDefinition.ISOLATION_SERIALIZABLE:
            throw new IllegalArgumentException("GigaSpace does not support serializable isolation level");
        }
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

    public void clean() throws DataAccessException {
        try {
            space.clean();
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public void clear(Object template) throws DataAccessException {
        try {
            space.clear(template, getCurrentTransaction());
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public int count(Object template) throws DataAccessException {
        return count(template, getModifiersForIsolationLevel());
    }

    public int count(Object template, int modifiers) throws DataAccessException {
        try {
            return space.count(template, getCurrentTransaction(), modifiers);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public Object snapshot(Object entry) throws DataAccessException {
        try {
            return space.snapshot(entry);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> T read(T template) throws DataAccessException {
        return read(template, defaultReadTimeout);
    }

    public <T> T read(T template, long timeout) throws DataAccessException {
        return read(template, timeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T read(T template, long timeout, int modifiers) throws DataAccessException {
        try {
            return (T) space.read(template, getCurrentTransaction(), timeout, modifiers);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> T read(Query<T> template) throws DataAccessException {
        return read(template, defaultReadTimeout);
    }

    public <T> T read(Query<T> template, long timeout) throws DataAccessException {
        return read(template, timeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T read(Query<T> template, long timeout, int modifiers) throws DataAccessException {
        try {
            return (T) space.read(template, getCurrentTransaction(), timeout, modifiers);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> T readIfExists(T template) throws DataAccessException {
        return readIfExists(template, defaultReadTimeout);
    }

    public <T> T readIfExists(T template, long timeout) throws DataAccessException {
        return readIfExists(template, timeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T readIfExists(T template, long timeout, int modifiers) throws DataAccessException {
        try {
            return (T) space.readIfExists(template, getCurrentTransaction(), timeout, modifiers);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> T readIfExists(Query<T> template) throws DataAccessException {
        return readIfExists(template, defaultReadTimeout);
    }

    public <T> T readIfExists(Query<T> template, long timeout) throws DataAccessException {
        return readIfExists(template, timeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T readIfExists(Query<T> template, long timeout, int modifiers) throws DataAccessException {
        try {
            return (T) space.readIfExists(template, getCurrentTransaction(), timeout, modifiers);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public Object[] readMultiple(Object template, int maxEntries) throws DataAccessException {
        return readMultiple(template, maxEntries, getModifiersForIsolationLevel());
    }

    public Object[] readMultiple(Object template, int maxEntries, int modifiers) throws DataAccessException {
        try {
            return space.readMultiple(template, getCurrentTransaction(), maxEntries, modifiers);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> T take(T template) throws DataAccessException {
        return take(template, defaultTakeTimeout);
    }

    @SuppressWarnings("unchecked")
    public <T> T take(T template, long timeout) throws DataAccessException {
        try {
            return (T) space.take(template, getCurrentTransaction(), timeout);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> T take(Query<T> template) throws DataAccessException {
        return take(template, defaultTakeTimeout);
    }

    @SuppressWarnings("unchecked")
    public <T> T take(Query<T> template, long timeout) throws DataAccessException {
        try {
            return (T) space.take(template, getCurrentTransaction(), timeout);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> T takeIfExists(T template) throws DataAccessException {
        return takeIfExists(template, defaultTakeTimeout);
    }

    @SuppressWarnings("unchecked")
    public <T> T takeIfExists(T template, long timeout) throws DataAccessException {
        try {
            return (T) space.takeIfExists(template, getCurrentTransaction(), timeout);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> T takeIfExists(Query<T> template) throws DataAccessException {
        return takeIfExists(template, defaultTakeTimeout);
    }

    @SuppressWarnings("unchecked")
    public <T> T takeIfExists(Query<T> template, long timeout) throws DataAccessException {
        try {
            return (T) space.takeIfExists(template, getCurrentTransaction(), timeout);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public Object[] takeMultiple(Object template, int maxEntries) throws DataAccessException {
        try {
            return space.takeMultiple(template, getCurrentTransaction(), maxEntries);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> LeaseContext<T> write(T entry) throws DataAccessException {
        return write(entry, defaultWriteLease);
    }

    @SuppressWarnings("unchecked")
    public <T> LeaseContext<T> write(T entry, long lease) throws DataAccessException {
        try {
            return space.write(entry, getCurrentTransaction(), lease);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> LeaseContext<T> write(T entry, long lease, long timeout, int modifiers) throws DataAccessException {
        try {
            return space.write(entry, getCurrentTransaction(), lease, timeout, modifiers);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> LeaseContext<T>[] writeMultiple(T[] entries) throws DataAccessException {
        return writeMultiple(entries, defaultWriteLease);
    }

    @SuppressWarnings("unchecked")
    public <T> LeaseContext<T>[] writeMultiple(T[] entries, long lease) throws DataAccessException {
        try {
            return space.writeMultiple(entries, getCurrentTransaction(), lease);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T[] updateMultiple(T[] entries, long[] leases) throws DataAccessException {
        try {
            return (T[]) space.updateMultiple(entries, getCurrentTransaction(), leases);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T[] updateMultiple(T[] entries, long[] leases, int updateModifiers) throws DataAccessException {
        try {
            return (T[]) space.updateMultiple(entries, getCurrentTransaction(), leases, updateModifiers);
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

    /**
     * Gets the isolation level from the current running transaction (enabling the usage of Spring
     * declarative isolation level settings). If there is no transaction in progress or the
     * transaction isolation is
     * {@link org.springframework.transaction.TransactionDefinition#ISOLATION_DEFAULT} will use the
     * default isolation level associated with this class.
     */
    private int getModifiersForIsolationLevel() {
        int isolationLevel = txProvider.getCurrentTransactionIsolationLevel(this);
        if (isolationLevel == TransactionDefinition.ISOLATION_DEFAULT) {
            return space.getReadModifiers();
        } else if (isolationLevel == TransactionDefinition.ISOLATION_READ_UNCOMMITTED) {
            return ReadModifiers.DIRTY_READ;
        } else if (isolationLevel == TransactionDefinition.ISOLATION_READ_COMMITTED) {
            return ReadModifiers.READ_COMMITTED;
        } else if (isolationLevel == TransactionDefinition.ISOLATION_REPEATABLE_READ) {
            return ReadModifiers.REPEATABLE_READ;
        } else {
            throw new IllegalArgumentException("GigaSpaces does not support isolation level [" + isolationLevel + "]");
        }
    }

    public String toString() {
        return space.toString();
    }
}
