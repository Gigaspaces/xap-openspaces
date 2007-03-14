package org.openspaces.core;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.LeaseContext;
import com.j_spaces.core.client.Query;

import net.jini.space.JavaSpace;

import org.openspaces.core.transaction.TransactionProvider;
import org.springframework.dao.DataAccessException;

/**
 * Provides a simpler inteface of both {@link JavaSpace} and GigaSpaces {@link IJSpace} extension
 * utilizing GigaSpaces extended and simplified programming model.
 * 
 * <p>
 * Most operations revolve around the use of Objects allowing to use GigaSpaces support for POJOs.
 * JavaSpaces Entries can also be used. Space operations (at the end) are delegated to the relevant
 * operation of {@link com.j_spaces.core.IJSpace}.
 * 
 * <p>
 * Though this interface has a single implementation it is still important to work against the
 * interface as it allows for simpler testing and mocking.
 * 
 * <p>
 * Transaction management is implicit and works in a declarative manner. Operations do not accept a
 * transaction object, and will automatically use the {@link TransactionProvider} in order to aquire
 * the current running transaction. If there is no current running transaction the operation will be
 * executed without a transaction.
 * 
 * <p>
 * {@link IJSpace} can be aquired using {@link #getSpace()} and in conjuction with
 * {@link #getTxProvider()} allows to work directly with {@link IJSpace} for low level API
 * execution or other low level GigaSpaces components requireing direct access to
 * {@link IJSpace}.
 * 
 * <p>
 * Operations throw a {@link org.openspaces.core.DataAccessException} allowing for simplified
 * development model as it is a runtime exception. The cause of the exception can be aquired from
 * the GigaSpace exception.
 * 
 * @author kimchy
 * @see com.j_spaces.core.IJSpace
 * @see com.j_spaces.core.client.Query
 * @see com.j_spaces.core.client.SQLQuery
 * @see org.openspaces.core.transaction.TransactionProvider
 * @see org.openspaces.core.DefaultGigaSpace
 * @see org.openspaces.core.DataAccessException
 */
public interface GigaSpace {

    /**
     * Returns the <code>IJSpace</code> used by this GigaSpace implementation to delegate
     * different space operations.
     * 
     * <p>
     * Allows to execute space operations that are not exposed by this interface, as well as using
     * it as a parmater to other low level GigaSpace components.
     * 
     * <p>
     * If a transaction object is required for low level operations (as low level operations do not
     * have declarative transaction ex) the {@link #getTxProvider()} should be used to aquire the
     * current running transaction.
     */
    IJSpace getSpace();

    /**
     * Returns the transaction provider allowing to access the current running transaction. Allows
     * to execute low level {@link com.j_spaces.core.IJSpace} operations that requires explicit
     * transaction object.
     */
    TransactionProvider getTxProvider();

    void clean() throws DataAccessException;

    void clear(Object template) throws DataAccessException;

    int count(Object template) throws DataAccessException;

    int count(Object template, int modifiers) throws DataAccessException;

    Object snapshot(Object entry) throws DataAccessException;

    <T> T read(T template) throws DataAccessException;

    <T> T read(T template, long timeout) throws DataAccessException;

    <T> T read(T template, long timeout, int modifiers) throws DataAccessException;

    <T> T read(Query<T> template) throws DataAccessException;

    <T> T read(Query<T> template, long timeout) throws DataAccessException;

    <T> T read(Query<T> template, long timeout, int modifiers) throws DataAccessException;

    <T> T readIfExists(T template) throws DataAccessException;

    <T> T readIfExists(T template, long timeout) throws DataAccessException;

    <T> T readIfExists(T template, long timeout, int modifiers) throws DataAccessException;

    <T> T readIfExists(Query<T> template) throws DataAccessException;

    <T> T readIfExists(Query<T> template, long timeout) throws DataAccessException;

    <T> T readIfExists(Query<T> template, long timeout, int modifiers) throws DataAccessException;

    Object[] readMultiple(Object template, int maxEntries) throws DataAccessException;

    Object[] readMultiple(Object template, int maxEntries, int modifiers) throws DataAccessException;

    <T> T take(T template) throws DataAccessException;

    <T> T take(T template, long timeout) throws DataAccessException;

    <T> T take(Query<T> template) throws DataAccessException;

    <T> T take(Query<T> template, long timeout) throws DataAccessException;

    <T> T takeIfExists(T template) throws DataAccessException;

    <T> T takeIfExists(T template, long timeout) throws DataAccessException;

    <T> T takeIfExists(Query<T> template) throws DataAccessException;

    <T> T takeIfExists(Query<T> template, long timeout) throws DataAccessException;

    Object[] takeMultiple(Object template, int maxEntries) throws DataAccessException;

    <T> LeaseContext<T> write(T entry) throws DataAccessException;

    <T> LeaseContext<T> write(T entry, long lease) throws DataAccessException;

    <T> LeaseContext<T> write(T entry, long lease, long timeout, int modifiers) throws DataAccessException;

    <T> LeaseContext<T>[] writeMultiple(T[] entries) throws DataAccessException;

    <T> LeaseContext<T>[] writeMultiple(T[] entries, long lease) throws DataAccessException;

    <T> T[] updateMultiple(T[] entries, long[] leases) throws DataAccessException;

    <T> T[] updateMultiple(T[] entries, long[] leases, int updateModifiers) throws DataAccessException;
}
