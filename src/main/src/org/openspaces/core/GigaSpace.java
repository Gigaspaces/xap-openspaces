/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.core;

import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.async.AsyncResultsReducer;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.LeaseContext;
import com.j_spaces.core.client.Query;
import net.jini.core.transaction.Transaction;
import net.jini.space.JavaSpace;
import org.openspaces.core.exception.ExceptionTranslator;
import org.openspaces.core.executor.DistributedTask;
import org.openspaces.core.executor.Task;
import org.openspaces.core.transaction.TransactionProvider;
import org.springframework.dao.DataAccessException;

/**
 * Provides a simpler interface of both {@link JavaSpace} and GigaSpaces {@link IJSpace} extension
 * utilizing GigaSpaces extended and simplified programming model.
 *
 * <p>Most operations revolve around the use of Objects allowing to use GigaSpaces support for POJOs.
 * JavaSpaces Entries can also be used. Space operations (at the end) are delegated to the relevant
 * operation of {@link com.j_spaces.core.IJSpace}.
 *
 * <p>Though this interface has a single implementation it is still important to work against the
 * interface as it allows for simpler testing and mocking.
 *
 * <p>Transaction management is implicit and works in a declarative manner. Operations do not accept a
 * transaction object, and will automatically use the {@link TransactionProvider} in order to acquire
 * the current running transaction. If there is no current running transaction the operation will be
 * executed without a transaction.
 *
 * <p>{@link IJSpace} can be acquired using {@link #getSpace()} and in conjunction with
 * {@link #getTxProvider()} allows to work directly with {@link IJSpace} for low level API
 * execution or other low level GigaSpaces components requiring direct access to
 * {@link IJSpace}.
 *
 * <p>Operations throw a {@link org.springframework.dao.DataAccessException} allowing for simplified
 * development model as it is a runtime exception. The cause of the exception can be acquired from
 * the GigaSpace exception.
 *
 * @author kimchy
 * @see com.j_spaces.core.IJSpace
 * @see com.j_spaces.core.client.Query
 * @see com.j_spaces.core.client.SQLQuery
 * @see org.openspaces.core.transaction.TransactionProvider
 * @see org.openspaces.core.DefaultGigaSpace
 * @see org.springframework.dao.DataAccessException
 */
public interface GigaSpace {

    /**
     * Returns the <code>IJSpace</code> used by this GigaSpace implementation to delegate
     * different space operations.
     *
     * <p>Allows to execute space operations that are not exposed by this interface, as well as using
     * it as a parameter to other low level GigaSpace components.
     *
     * <p>If a transaction object is required for low level operations (as low level operations do not
     * have declarative transaction ex) the {@link #getTxProvider()} should be used to acquire the
     * current running transaction.
     */
    IJSpace getSpace();

    /**
     * Returns the transaction provider allowing to access the current running transaction. Allows
     * to execute low level {@link com.j_spaces.core.IJSpace} operations that requires explicit
     * transaction object.
     */
    TransactionProvider getTxProvider();

    /**
     * Returns the current running transaction. Can be <code>null</code> if no transaction is in progress.
     */
    Transaction getCurrentTransaction();

    /**
     * Returns the exception translator associated with this GigaSpace instance.
     */
    ExceptionTranslator getExceptionTranslator();

    /**
     * Gets the isolation level from the current running transaction (enabling the usage of Spring
     * declarative isolation level settings). If there is no transaction in progress or the
     * transaction isolation is
     * {@link org.springframework.transaction.TransactionDefinition#ISOLATION_DEFAULT} will use the
     * default isolation level associated with this class.
     */
    int getModifiersForIsolationLevel();

    /**
     * Cleans this space. The side-effects of cleaning the space are:
     * <ul>
     * <li>all entries and templates are deleted.</li>
     * <li>all storage adapter contexts are closed.</li>
     * <li>all engine threads are terminated.</li>
     * <li>the engine re-initializes itself.</li>
     * </ul>
     *
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#clean()
     */
    void clean() throws DataAccessException;

    /**
     * Removes the entries that match the specified template and the specified transaction from this space.
     *
     * <p>If the clear operation conducted without transaction (null as value) it will clear all entries that
     * are not under transaction. Therefore entries under transaction would not be removed from the space.
     *
     * <p>The clear operation supports inheritance, therefore template class matching objects
     * and its sub classes matching objects are part of the candidates population
     * to be removed from the space. You can in fact clean all space objects (that are not under
     * transaction) by calling: <code>gigaSpace.clear(null)</code>.
     *
     * <p>Notice: The clear operation does not remove notify templates i.e. registration for notifications.
     *
     * @param template the template to use for matching
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#clear(Object,net.jini.core.transaction.Transaction)
     */
    void clear(Object template) throws DataAccessException;

    /**
     * Count any matching entry from the space. If a running within a transaction
     * will count all the entries visible under the transaction.
     *
     * @param template The template used for matching. Matching is done against the
     *                 template with <code>null</code> fields being wildcards
     *                 ("match anything") other fields being values ("match
     *                 exactly on the serialized form").
     * @return The number of matching entries
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#count(Object,net.jini.core.transaction.Transaction)
     */
    int count(Object template) throws DataAccessException;

    /**
     * Count any matching entry from the space. If a running within a transaction
     * will count all the entries visible under the transaction.
     *
     * <p>Allows to specify modifiers using {@link com.j_spaces.core.client.ReadModifiers}
     * which allows to programmatically control the isolation level this count operation
     * will be performed under.
     *
     * @param template  The template used for matching. Matching is done against the
     *                  template with <code>null</code> fields being wildcards
     *                  ("match anything") other fields being values ("match
     *                  exactly on the serialized form").
     * @param modifiers one or a union of {@link com.j_spaces.core.client.ReadModifiers}.
     * @return The number of matching entries
     * @throws DataAccessException
     */
    int count(Object template, int modifiers) throws DataAccessException;

    /**
     * <p>The process of serializing an entry for transmission to a JavaSpaces service will
     * be identical if the same entry is used twice. This is most likely to be an issue with
     * templates that are used repeatedly to search for entries with read or take. The client-side
     * implementations of read and take cannot reasonably avoid this duplicated effort, since they
     * have no efficient way of checking whether the same template is being used without intervening
     * modification. The snapshot method gives the JavaSpaces service implementor a way to reduce
     * the impact of repeated use of the same entry. Invoking snapshot with an Entry will return
     * another Entry object that contains a snapshot of the original entry. Using the returned snapshot
     * entry is equivalent to using the unmodified original entry in all operations on the same JavaSpaces
     * service. Modifications to the original entry will not affect the snapshot. You can snapshot a null
     * template; snapshot may or may not return null given a null template. The entry returned from snapshot
     * will be guaranteed equivalent to the original unmodified object only when used with the space. Using
     * the snapshot with any other JavaSpaces service will generate an <code>IllegalArgumentException</code> unless the
     * other space can use it because of knowledge about the JavaSpaces service that generated the snapshot.
     * The snapshot will be a different object from the original, may or may not have the same hash code,
     * and equals may or may not return true when invoked with the original object, even if the original object
     * is unmodified. A snapshot is guaranteed to work only within the virtual machine in which it was generated.
     * If a snapshot is passed to another virtual machine (for example, in a parameter of an RMI call), using
     * it--even with the same JavaSpaces service--may generate an <code>IllegalArgumentException</code>.
     *
     * @param entry The entry to snapshot
     * @return The snapshot
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#snapshot(Object)
     */
    Object snapshot(Object entry) throws DataAccessException;

    /**
     * Read any matching object from the space, blocking until one exists. Return
     * <code>null</code> if the timeout expires.
     *
     * <p>Note, the timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * @param template The template used for matching. Matching is done against
     *                 template with <code>null</code> fields being
     *                 wildcards ("match anything") other fields being values ("match
     *                 exactly on the serialized form").
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#read(Object,net.jini.core.transaction.Transaction,long)
     */
    <T> T read(T template) throws DataAccessException;

    /**
     * Read any matching object from the space, blocking until one exists. Return
     * <code>null</code> if the timeout expires.
     *
     * @param template The template used for matching. Matching is done against
     *                 template with <code>null</code> fields being
     *                 wildcards ("match anything") other fields being values ("match
     *                 exactly on the serialized form").
     * @param timeout  How long the client is willing to wait for a
     *                 transactionally proper matching object. A timeout of
     *                 {@link JavaSpace#NO_WAIT} means to wait no time at all; this is
     *                 equivalent to a wait of zero.
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#read(Object,net.jini.core.transaction.Transaction,long)
     */
    <T> T read(T template, long timeout) throws DataAccessException;

    /**
     * Read any matching object from the space, blocking until one exists. Return
     * <code>null</code> if the timeout expires.
     *
     * <p>Overloads {@link #read(Object,long)} by adding a <code>modifiers</code> parameter.
     * Equivalent when called with the default modifier - {@link com.j_spaces.core.client.ReadModifiers#REPEATABLE_READ}.
     * Modifiers are used to define the behavior of a read operation.
     *
     * @param template  The template used for matching. Matching is done against
     *                  template with <code>null</code> fields being
     *                  wildcards ("match anything") other fields being values ("match
     *                  exactly on the serialized form").
     * @param timeout   How long the client is willing to wait for a
     *                  transactionally proper matching object. A timeout of
     *                  {@link JavaSpace#NO_WAIT} means to wait no time at all; this is
     *                  equivalent to a wait of zero.
     * @param modifiers one or a union of {@link com.j_spaces.core.client.ReadModifiers}.
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#read(Object,net.jini.core.transaction.Transaction,long,int)
     */
    <T> T read(T template, long timeout, int modifiers) throws DataAccessException;

    /**
     * Read any matching object from the space, blocking until one exists. Return
     * <code>null</code> if the timeout expires.
     *
     * <p>Note, the timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#read(Object,net.jini.core.transaction.Transaction,long)
     */
    <T> T read(Query<T> template) throws DataAccessException;

    /**
     * Read any matching object from the space, blocking until one exists. Return
     * <code>null</code> if the timeout expires.
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.
     * @param timeout  How long the client is willing to wait for a
     *                 transactionally proper matching object. A timeout of
     *                 {@link JavaSpace#NO_WAIT} means to wait no time at all; this is
     *                 equivalent to a wait of zero.
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#read(Object,net.jini.core.transaction.Transaction,long)
     */
    <T> T read(Query<T> template, long timeout) throws DataAccessException;

    /**
     * Read any matching object from the space, blocking until one exists. Return
     * <code>null</code> if the timeout expires.
     *
     * <p>Overloads {@link #read(Object,long)} by adding a <code>modifiers</code> parameter.
     * Equivalent when called with the default modifier - {@link com.j_spaces.core.client.ReadModifiers#REPEATABLE_READ}.
     * Modifiers are used to define the behavior of a read operation.
     *
     * @param template  A query to be executed against the space. Most common one is
     *                  {@link com.j_spaces.core.client.SQLQuery}.
     * @param timeout   How long the client is willing to wait for a
     *                  transactionally proper matching object. A timeout of
     *                  {@link JavaSpace#NO_WAIT} means to wait no time at all; this is
     *                  equivalent to a wait of zero.
     * @param modifiers one or a union of {@link com.j_spaces.core.client.ReadModifiers}.
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#read(Object,net.jini.core.transaction.Transaction,long,int)
     */
    <T> T read(Query<T> template, long timeout, int modifiers) throws DataAccessException;

    /**
     * Read any matching object from the space, returning <code>null</code> if
     * there currently is none. Matching and timeouts are done as in
     * <code>read</code>, except that blocking in this call is done only if
     * necessary to wait for transactional state to settle.
     *
     * <p>Note, the timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * @param template The template used for matching. Matching is done against
     *                 the template with <code>null</code> fields being
     *                 wildcards ("match anything") other fields being values ("match
     *                 exactly on the serialized form").
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#readIfExists(Object,net.jini.core.transaction.Transaction,long)
     */
    <T> T readIfExists(T template) throws DataAccessException;

    /**
     * Read any matching object from the space, returning <code>null</code> if
     * there currently is none. Matching and timeouts are done as in
     * <code>read</code>, except that blocking in this call is done only if
     * necessary to wait for transactional state to settle.
     *
     * @param template The template used for matching. Matching is done against
     *                 template with <code>null</code> fields being
     *                 wildcards ("match anything") other fields being values ("match
     *                 exactly on the serialized form").
     * @param timeout  How long the client is willing to wait for a
     *                 transactionally proper matching object. A timeout of
     *                 {@link JavaSpace#NO_WAIT} means to wait no time at all; this is
     *                 equivalent to a wait of zero.
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#readIfExists(Object,net.jini.core.transaction.Transaction,long)
     */
    <T> T readIfExists(T template, long timeout) throws DataAccessException;

    /**
     * Read any matching object from the space, returning <code>null</code> if
     * there currently is none. Matching and timeouts are done as in
     * <code>read</code>, except that blocking in this call is done only if
     * necessary to wait for transactional state to settle.
     *
     * <p>Overloads {@link #read(Object,long)} by adding a <code>modifiers</code> parameter.
     * Equivalent when called with the default modifier - {@link com.j_spaces.core.client.ReadModifiers#REPEATABLE_READ}.
     * Modifiers are used to define the behavior of a read operation.
     *
     * @param template  The template used for matching. Matching is done against
     *                  template with <code>null</code> fields being
     *                  wildcards ("match anything") other fields being values ("match
     *                  exactly on the serialized form").
     * @param timeout   How long the client is willing to wait for a
     *                  transactionally proper matching object. A timeout of
     *                  {@link JavaSpace#NO_WAIT} means to wait no time at all; this is
     *                  equivalent to a wait of zero.
     * @param modifiers one or a union of {@link com.j_spaces.core.client.ReadModifiers}.
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#readIfExists(net.jini.core.entry.Entry,net.jini.core.transaction.Transaction,long,int)
     */
    <T> T readIfExists(T template, long timeout, int modifiers) throws DataAccessException;

    /**
     * Read any matching object from the space, returning <code>null</code> if
     * there currently is none. Matching and timeouts are done as in
     * <code>read</code>, except that blocking in this call is done only if
     * necessary to wait for transactional state to settle.
     *
     * <p>Note, the timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#readIfExists(Object,net.jini.core.transaction.Transaction,long)
     */
    <T> T readIfExists(Query<T> template) throws DataAccessException;

    /**
     * Read any matching object from the space, returning <code>null</code> if
     * there currently is none. Matching and timeouts are done as in
     * <code>read</code>, except that blocking in this call is done only if
     * necessary to wait for transactional state to settle.
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.
     * @param timeout  How long the client is willing to wait for a
     *                 transactionally proper matching object. A timeout of
     *                 {@link JavaSpace#NO_WAIT} means to wait no time at all; this is
     *                 equivalent to a wait of zero.
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#readIfExists(Object,net.jini.core.transaction.Transaction,long)
     */
    <T> T readIfExists(Query<T> template, long timeout) throws DataAccessException;

    /**
     * Read any matching object from the space, returning <code>null</code> if
     * there currently is none. Matching and timeouts are done as in
     * <code>read</code>, except that blocking in this call is done only if
     * necessary to wait for transactional state to settle.
     *
     * <p>Overloads {@link #read(Object,long)} by adding a <code>modifiers</code> parameter.
     * Equivalent when called with the default modifier - {@link com.j_spaces.core.client.ReadModifiers#REPEATABLE_READ}.
     * Modifiers are used to define the behavior of a read operation.
     *
     * @param template  A query to be executed against the space. Most common one is
     *                  {@link com.j_spaces.core.client.SQLQuery}.
     * @param timeout   How long the client is willing to wait for a
     *                  transactionally proper matching object. A timeout of
     *                  {@link JavaSpace#NO_WAIT} means to wait no time at all; this is
     *                  equivalent to a wait of zero.
     * @param modifiers one or a union of {@link com.j_spaces.core.client.ReadModifiers}.
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#readIfExists(net.jini.core.entry.Entry,net.jini.core.transaction.Transaction,long,int)
     */
    <T> T readIfExists(Query<T> template, long timeout, int modifiers) throws DataAccessException;

    /**
     * Read any matching entries from the space. Matching is done as in
     * <code>read</code> without timeout ({@link JavaSpace#NO_WAIT}). Returns an
     * array with matches bound by <code>maxEntries</code>. Returns an
     * empty array if no match was found.
     *
     * @param template   The template used for matching. Matching is done against
     *                   the template with <code>null</code> fields being.
     *                   wildcards ("match anything") other fields being values ("match
     *                   exactly on the serialized form"). The template can also be one
     *                   of the different {@link com.j_spaces.core.client.Query} classes
     * @param maxEntries A limit on the number of entries to be returned. Use
     *                   {@link Integer#MAX_VALUE} for the uppermost limit.
     * @return A copy of the entries read from the space.
     * @see com.j_spaces.core.IJSpace#readMultiple(Object,net.jini.core.transaction.Transaction,int)
     */
    <T> T[] readMultiple(T template, int maxEntries) throws DataAccessException;

    /**
     * Read any matching entries from the space. Matching is done as in
     * <code>read</code> without timeout ({@link JavaSpace#NO_WAIT}). Returns an
     * array with matches bound by <code>maxEntries</code>. Returns an
     * empty array if no match was found.
     *
     * @param template   The template used for matching. Matching is done against
     *                   the template with <code>null</code> fields being.
     *                   wildcards ("match anything") other fields being values ("match
     *                   exactly on the serialized form"). The template can also be one
     *                   of the different {@link com.j_spaces.core.client.Query} classes
     * @param maxEntries A limit on the number of entries to be returned. Use
     *                   {@link Integer#MAX_VALUE} for the uppermost limit.
     * @return A copy of the entries read from the space.
     * @see com.j_spaces.core.IJSpace#readMultiple(Object,net.jini.core.transaction.Transaction,int)
     */
    <T> T[] readMultiple(Query<T> template, int maxEntries) throws DataAccessException;

    /**
     * Read any matching entries from the space. Matching is done as in
     * <code>read</code> without timeout ({@link JavaSpace#NO_WAIT}). Returns an
     * array with matches bound by <code>maxEntries</code>. Returns an
     * empty array if no match was found.
     *
     * <p>Overloads {@link #readMultiple(Object,int)} by adding a
     * <code>modifiers</code> parameter. Equivalent when called with the default
     * modifier - {@link com.j_spaces.core.client.ReadModifiers#REPEATABLE_READ}. Modifiers
     * are used to define the behavior of a read operation.
     *
     * @param template   The template used for matching. Matching is done against
     *                   the template with <code>null</code> fields being.
     *                   wildcards ("match anything") other fields being values ("match
     *                   exactly on the serialized form"). The template can also be one
     *                   of the different {@link com.j_spaces.core.client.Query} classes
     * @param maxEntries A limit on the number of entries to be returned. Use
     *                   {@link Integer#MAX_VALUE} for the uppermost limit.
     * @param modifiers  One or a union of {@link com.j_spaces.core.client.ReadModifiers}.
     * @return A copy of the entries read from the space.
     * @see com.j_spaces.core.IJSpace#readMultiple(Object,net.jini.core.transaction.Transaction,int)
     */
    <T> T[] readMultiple(T template, int maxEntries, int modifiers) throws DataAccessException;

    /**
     * Read any matching entries from the space. Matching is done as in
     * <code>read</code> without timeout ({@link JavaSpace#NO_WAIT}). Returns an
     * array with matches bound by <code>maxEntries</code>. Returns an
     * empty array if no match was found.
     *
     * <p>Overloads {@link #readMultiple(Object,int)} by adding a
     * <code>modifiers</code> parameter. Equivalent when called with the default
     * modifier - {@link com.j_spaces.core.client.ReadModifiers#REPEATABLE_READ}. Modifiers
     * are used to define the behavior of a read operation.
     *
     * @param template   The template used for matching. Matching is done against
     *                   the template with <code>null</code> fields being.
     *                   wildcards ("match anything") other fields being values ("match
     *                   exactly on the serialized form"). The template can also be one
     *                   of the different {@link com.j_spaces.core.client.Query} classes
     * @param maxEntries A limit on the number of entries to be returned. Use
     *                   {@link Integer#MAX_VALUE} for the uppermost limit.
     * @param modifiers  One or a union of {@link com.j_spaces.core.client.ReadModifiers}.
     * @return A copy of the entries read from the space.
     * @see com.j_spaces.core.IJSpace#readMultiple(Object,net.jini.core.transaction.Transaction,int)
     */
    <T> T[] readMultiple(Query<T> template, int maxEntries, int modifiers) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space, blocking until one exists.
     * Return <code>null</code> if the timeout expires.
     *
     * <p>Note, the timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * @param template The template used for matching. Matching is done against
     *                 the template with <code>null</code> fields being wildcards (
     *                 "match anything") other fields being values ("match exactly
     *                 on the serialized form").
     * @return A removed entry from the space
     * @throws DataAccessException
     */
    <T> T take(T template) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space, blocking until one exists.
     * Return <code>null</code> if the timeout expires.
     *
     * @param template The template used for matching. Matching is done against
     *                 the template with <code>null</code> fields being wildcards (
     *                 "match anything") other fields being values ("match exactly
     *                 on the serialized form").
     * @param timeout  How long the client is willing to wait for a
     *                 transactionally proper matching entry. A timeout of
     *                 {@link net.jini.space.JavaSpace#NO_WAIT} means to wait no
     *                 time at all; this is equivalent to a wait of zero.
     * @return A removed entry from the space
     * @throws DataAccessException
     */
    <T> T take(T template, long timeout) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space, blocking until one exists.
     * Return <code>null</code> if the timeout expires.
     *
     * <p>Note, the timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.
     * @return A removed entry from the space
     * @throws DataAccessException
     */
    <T> T take(Query<T> template) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space, blocking until one exists.
     * Return <code>null</code> if the timeout expires.
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.
     * @param timeout  How long the client is willing to wait for a
     *                 transactionally proper matching entry. A timeout of
     *                 {@link net.jini.space.JavaSpace#NO_WAIT} means to wait no
     *                 time at all; this is equivalent to a wait of zero.
     * @return A removed entry from the space
     * @throws DataAccessException
     */
    <T> T take(Query<T> template, long timeout) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space, blocking until one exists.
     * Return <code>null</code> if the timeout expires. Matching and timeouts are
     * done as in <code>take</code>, except that blocking in this call is done
     * only if necessary to wait for transactional state to settle.
     *
     * <p>Note, the timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * @param template The template used for matching. Matching is done against
     *                 the template with <code>null</code> fields being wildcards (
     *                 "match anything") other fields being values ("match exactly
     *                 on the serialized form").
     * @return A removed entry from the space
     * @throws DataAccessException
     */
    <T> T takeIfExists(T template) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space, blocking until one exists.
     * Return <code>null</code> if the timeout expires. Matching and timeouts are
     * done as in <code>take</code>, except that blocking in this call is done
     * only if necessary to wait for transactional state to settle.
     *
     * @param template The template used for matching. Matching is done against
     *                 the template with <code>null</code> fields being wildcards (
     *                 "match anything") other fields being values ("match exactly
     *                 on the serialized form").
     * @param timeout  How long the client is willing to wait for a
     *                 transactionally proper matching entry. A timeout of
     *                 {@link net.jini.space.JavaSpace#NO_WAIT} means to wait no
     *                 time at all; this is equivalent to a wait of zero.
     * @return A removed entry from the space
     * @throws DataAccessException
     */
    <T> T takeIfExists(T template, long timeout) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space, blocking until one exists.
     * Return <code>null</code> if the timeout expires. Matching and timeouts are
     * done as in <code>take</code>, except that blocking in this call is done
     * only if necessary to wait for transactional state to settle.
     *
     * <p>Note, the timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.
     * @return A removed entry from the space
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#takeIfExists(Object,net.jini.core.transaction.Transaction,long)
     */
    <T> T takeIfExists(Query<T> template) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space, blocking until one exists.
     * Return <code>null</code> if the timeout expires. Matching and timeouts are
     * done as in <code>take</code>, except that blocking in this call is done
     * only if necessary to wait for transactional state to settle.
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.
     * @param timeout  How long the client is willing to wait for a
     *                 transactionally proper matching entry. A timeout of
     *                 {@link net.jini.space.JavaSpace#NO_WAIT} means to wait no
     *                 time at all; this is equivalent to a wait of zero.
     * @return A removed entry from the space
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#takeIfExists(Object,net.jini.core.transaction.Transaction,long)
     */
    <T> T takeIfExists(Query<T> template, long timeout) throws DataAccessException;

    /**
     * Takes (removes) all the entries matching the specified template from this
     * space.
     *
     * @param template   The template used for matching. Matching is done against
     *                   the template with <code>null</code> fields being.
     *                   wildcards ("match anything") other fields being values ("match
     *                   exactly on the serialized form"). The template can also be one
     *                   of the different {@link com.j_spaces.core.client.Query} classes
     * @param maxEntries A limit on the number of entries to be returned. Use
     *                   {@link Integer#MAX_VALUE} for the uppermost limit.
     * @return Removed matched entries from the space
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#takeMultiple(Object,net.jini.core.transaction.Transaction,int)
     */
    <T> T[] takeMultiple(T template, int maxEntries) throws DataAccessException;

    /**
     * Takes (removes) all the entries matching the specified template from this
     * space.
     *
     * @param template   The template used for matching. Matching is done against
     *                   the template with <code>null</code> fields being.
     *                   wildcards ("match anything") other fields being values ("match
     *                   exactly on the serialized form"). The template can also be one
     *                   of the different {@link com.j_spaces.core.client.Query} classes
     * @param maxEntries A limit on the number of entries to be returned. Use
     *                   {@link Integer#MAX_VALUE} for the uppermost limit.
     * @return Removed matched entries from the space
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#takeMultiple(Object,net.jini.core.transaction.Transaction,int)
     */
    <T> T[] takeMultiple(Query<T> template, int maxEntries) throws DataAccessException;

    /**
     * Writes a new object to the space, returning its {@link com.j_spaces.core.LeaseContext}.
     *
     * <p>By default uses the {@link com.j_spaces.core.client.UpdateModifiers#UPDATE_OR_WRITE}
     * modifier (see {@link #write(Object,long,long,int)}. In order to force the operation
     * to perform "write" only (a bit more performant), the {@link com.j_spaces.core.client.UpdateModifiers#WRITE_ONLY}
     * modifier can be used resulting in an {@link org.openspaces.core.EntryAlreadyInSpaceException}
     * if the entry already exists in the space (it must have an id for this exception to be raised).
     *
     * <p>If the object has a primary key that isn't auto - generated, and the modifiers value is one of
     * {@link com.j_spaces.core.client.UpdateModifiers#UPDATE_OR_WRITE},
     * {@link com.j_spaces.core.client.UpdateModifiers#UPDATE_ONLY} or
     * {@link com.j_spaces.core.client.UpdateModifiers#PARTIAL_UPDATE}, it will call to the update method,
     * returning a LeaseContext holder. This lease is unknown to the grantor, unless an actual 'write' was performed.
     *
     * <p>The entry will be written using the default lease this interface is configured with
     * (using the its factory). In order to explicitly define the lease, please use
     * {@link #write(Object,long)}.
     *
     * @param entry The entry to write to the space
     * @return A usable <code>Lease</code> on a successful write, or <code>null</code> if performed
     *         with the proxy configured with NoWriteLease flag.
     *         <p>when {@link com.j_spaces.core.client.UpdateModifiers#UPDATE_OR_WRITE} modifier is applied,
     *         {@link LeaseContext#getObject()} returns <code>null</code> on a successful write
     *         or the previous value - on successful update.  {@link org.openspaces.core.UpdateOperationTimeoutException}
     *         is thrown if timeout occurred while trying to update the object.
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#write(Object,net.jini.core.transaction.Transaction,long)
     */
    <T> LeaseContext<T> write(T entry) throws DataAccessException;

    /**
     * Writes a new object to the space, returning its {@link com.j_spaces.core.LeaseContext}.
     *
     * <p>By default uses the {@link com.j_spaces.core.client.UpdateModifiers#UPDATE_OR_WRITE}
     * modifier (see {@link #write(Object,long,long,int)}. In order to force the operation
     * to perform "write" only (a bit more performant), the {@link com.j_spaces.core.client.UpdateModifiers#WRITE_ONLY}
     * modifier can be used resulting in an {@link org.openspaces.core.EntryAlreadyInSpaceException}
     * if the entry already exists in the space (it must have an id for this exception to be raised).
     *
     * <p>If the object has a primary key that isn't auto - generated, and the modifiers value is one of
     * {@link com.j_spaces.core.client.UpdateModifiers#UPDATE_OR_WRITE},
     * {@link com.j_spaces.core.client.UpdateModifiers#UPDATE_ONLY} or
     * {@link com.j_spaces.core.client.UpdateModifiers#PARTIAL_UPDATE}, it will call to the update method,
     * returning a LeaseContext holder. This lease is unknown to the grantor, unless an actual 'write' was performed.
     *
     * @param entry The entry to write to the space
     * @param lease The lease the entry will be written with, in <b>milliseconds</b>.
     * @return A usable <code>Lease</code> on a successful write, or <code>null</code> if performed
     *         with the proxy configured with NoWriteLease flag.
     *         <p>when {@link com.j_spaces.core.client.UpdateModifiers#UPDATE_OR_WRITE} modifier is applied,
     *         {@link LeaseContext#getObject()} returns <code>null</code> on a successful write
     *         or the previous value - on successful update.  {@link org.openspaces.core.UpdateOperationTimeoutException}
     *         is thrown if timeout occurred while trying to update the object.
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#write(Object,net.jini.core.transaction.Transaction,long)
     */
    <T> LeaseContext<T> write(T entry, long lease) throws DataAccessException;

    /**
     * Writes a new object to the space, returning its {@link com.j_spaces.core.LeaseContext}.
     *
     * <p>By default uses the {@link com.j_spaces.core.client.UpdateModifiers#UPDATE_OR_WRITE}
     * modifier (see {@link #write(Object,long,long,int)}. In order to force the operation
     * to perform "write" only (a bit more performant), the {@link com.j_spaces.core.client.UpdateModifiers#WRITE_ONLY}
     * modifier can be used resulting in an {@link org.openspaces.core.EntryAlreadyInSpaceException}
     * if the entry already exists in the space (it must have an id for this exception to be raised).
     *
     * <p>If the object has a primary key that isn't auto - generated, and the modifiers value is one of
     * {@link com.j_spaces.core.client.UpdateModifiers#UPDATE_OR_WRITE},
     * {@link com.j_spaces.core.client.UpdateModifiers#UPDATE_ONLY} or
     * {@link com.j_spaces.core.client.UpdateModifiers#PARTIAL_UPDATE}, it will call to the update method,
     * returning a LeaseContext holder. This lease is unknown to the grantor, unless an actual 'write' was performed.
     *
     * @param entry The entry to write to the space
     * @param lease The lease the entry will be written with, in <b>milliseconds</b>.
     * @return A usable <code>Lease</code> on a successful write, or <code>null</code> if performed
     *         with the proxy configured with NoWriteLease flag.
     *         <p>when {@link com.j_spaces.core.client.UpdateModifiers#UPDATE_OR_WRITE} modifier is applied,
     *         {@link LeaseContext#getObject()} returns <code>null</code> on a successful write
     *         or the previous value - on successful update.  {@link org.openspaces.core.UpdateOperationTimeoutException}
     *         is thrown if timeout occurred while trying to update the object.
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#write(Object,net.jini.core.transaction.Transaction,long)
     */
    <T> LeaseContext<T> write(T entry, long lease, long timeout, int modifiers) throws DataAccessException;

    /**
     * Writes the specified entries to this space.
     *
     * <p>The entry will be written using the default lease this interface is configured with
     * (using the its factory). In order to explicitly define the lease, please use
     * {@link #writeMultiple(Object[],long)}.
     *
     * @param entries The entries to write to the space.
     * @return Leases for the written entries
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#writeMultiple(Object[],net.jini.core.transaction.Transaction,long)
     */
    <T> LeaseContext<T>[] writeMultiple(T[] entries) throws DataAccessException;

    /**
     * Writes the specified entries to this space.
     *
     * @param entries The entries to write to the space.
     * @param lease   The lease the entry will be written with, in <b>milliseconds</b>.
     * @return Leases for the written entries
     * @throws DataAccessException
     * @see com.j_spaces.core.IJSpace#writeMultiple(Object[],net.jini.core.transaction.Transaction,long)
     */
    <T> LeaseContext<T>[] writeMultiple(T[] entries, long lease) throws DataAccessException;

    <T> Object[] updateMultiple(T[] entries, long[] leases) throws DataAccessException;

    /**
     * Same as a single update but for a group of entities sharing the same transaction (if any),
     * applied with the same operation modifier (or default {@link com.j_spaces.core.client.Modifiers#NONE}).
     * The semantics of a single update and a batch update are similar - the return
     * value for each corresponds to it's cell in the returned array.
     * see <code>'returns'</code> for possible return values.
     *
     * @param entries         the array of objects containing the new values,
     *                        each entry must contain its UID.
     * @param leases          The lease time of the updated objects, 0 means retain the original lease.
     * @param updateModifiers operation modifiers, values from {@link com.j_spaces.core.client.UpdateModifiers UpdateModifiers}.
     * @return array of objects which correspond to the input
     *         entries array.
     *         An object can be either one of:
     *         <ul>
     *         <li>an Entry, if the update was successful
     *         <li><code>null</code> - if timeout occurred after waiting for a transactional proper matching entry
     *         <li>an Exception object, in case of an exception
     *         <ul>
     *         <li>{@link org.openspaces.core.EntryNotInSpaceException} - in case the entry does not exist
     *         <li>{@link org.openspaces.core.SpaceOptimisticLockingFailureException} - in case updating with non-latest version
     *         </ul>
     *         <li> when {@link com.j_spaces.core.client.UpdateModifiers#UPDATE_OR_WRITE} modifier is applied,
     *         <ul>
     *         <li><code>null</code> - if write was successful,
     *         <li>previous value - on successful update, or
     *         <li>an Exception Object, including OperationTimeoutException - thrown if timeout occurred.
     *         </ul>
     *         </ul>
     * @throws DataAccessException
     */
    <T> Object[] updateMultiple(T[] entries, long[] leases, int updateModifiers) throws DataAccessException;

    /**
     * Returns an iterator builder allowing to configure and create a {@link com.j_spaces.core.client.GSIterator}
     * over the Space.
     */
    IteratorBuilder iterator();

    /**
     * Executes a task on a specific space node. The space node it will
     * execute on should be controlled by having a method that return the routing value
     * annotated with {@link com.gigaspaces.annotation.pojo.SpaceRouting}.
     *
     * <p>In order to control the routing externally, use {@link #execute(org.openspaces.core.executor.Task, Object)}.
     *
     * <p>Resources defined within processing unit (space node) the task is executed on are accessible by
     * using either the {@link org.springframework.beans.factory.annotation.Autowired} or
     * {@link javax.annotation.Resource} annotations (assuming they are enabled using <code>context:annotation-config</code>).
     * Bean lifecycle methods, such as {@link org.openspaces.core.cluster.ClusterInfoAware} and
     * {@link org.springframework.context.ApplicationContextAware} are also available. Note, make sure
     * this variables are defined as <b>transient</b>.
     *
     * @param task The task to execute
     * @return a Future representing pending completion of the task,
     *         and whose <code>get()</code> method will return the task value upon comletion.
     */
    <T> AsyncFuture<T> execute(Task<T> task);

    /**
     * Executes a task on a specific space node. The space node it will
     * execute on should is controlled by the routing value provided as a second paramter.
     *
     * <p>In order to control the using the Task itself, use {@link #execute(org.openspaces.core.executor.Task)}.
     *
     * <p>Resources defined within processing unit (space node) the task is executed on are accessible by
     * using either the {@link org.springframework.beans.factory.annotation.Autowired} or
     * {@link javax.annotation.Resource} annotations (assuming they are enabled using <code>context:annotation-config</code>).
     * Bean lifecycle methods, such as {@link org.openspaces.core.cluster.ClusterInfoAware} and
     * {@link org.springframework.context.ApplicationContextAware} are also available. Note, make sure
     * this variables are defined as <b>transient</b>.
     *
     * @param task    The task to execute
     * @param routing The routing value that will control on which node the task will be executed on
     * @return a Future representing pending completion of the task,
     *         and whose <code>get()</code> method will return the task value upon comletion.
     */
    <T> AsyncFuture<T> execute(Task<T> task, Object routing);

    /**
     * Executes a task on all the nodes that correspond to the list of routing values. The task is executed
     * on each space node with all the results reduced by the
     * {@link org.openspaces.core.executor.DistributedTask#reduce(java.util.List)} operation.
     *
     * <p>The task can optionally implement {@link com.gigaspaces.async.AsyncResultsModerator} that can control
     * if tasks should continue to accumelate or it should break and execute the reduce operation on the
     * results received so far.
     *
     * <p>The future actual result will be the reduced result of the execution, or the exception thrown during
     * during the reduce operation. The moderator can be used as a mechanism to listen for results as they arrive.
     *
     * <p>Resources defined within processing unit (space node) the task is executed on are accessible by
     * using either the {@link org.springframework.beans.factory.annotation.Autowired} or
     * {@link javax.annotation.Resource} annotations (assuming they are enabled using <code>context:annotation-config</code>).
     * Bean lifecycle methods, such as {@link org.openspaces.core.cluster.ClusterInfoAware} and
     * {@link org.springframework.context.ApplicationContextAware} are also available. Note, make sure
     * this variables are defined as <b>transient</b>.
     *
     * @param task    The task to execute
     * @param routing A list of routing values, each resulting in an execution of the task on the space node
     *                it corresponds to
     * @return a Future representing pending completion of the task,
     *         and whose <code>get()</code> method will return the task value upon comletion.
     */
    <T, R> AsyncFuture<R> execute(DistributedTask<T, R> task, Object... routing);

    /**
     * Executes the task on all the primary space nodes within the cluster (broadcast). The task is executed
     * on each space node with all the results reduced by the
     * {@link org.openspaces.core.executor.DistributedTask#reduce(java.util.List)} operation.
     *
     * <p>The task can optionally implement {@link com.gigaspaces.async.AsyncResultsModerator} that can control
     * if tasks should continue to accumelate or it should break and execute the reduce operation on the
     * results received so far.
     *
     * <p>The future actual result will be the reduced result of the execution, or the exception thrown during
     * during the reduce operation. The moderator can be used as a mechanism to listen for results as they arrive.
     *
     * <p>Resources defined within processing unit (space node) the task is executed on are accessible by
     * using either the {@link org.springframework.beans.factory.annotation.Autowired} or
     * {@link javax.annotation.Resource} annotations (assuming they are enabled using <code>context:annotation-config</code>).
     * Bean lifecycle methods, such as {@link org.openspaces.core.cluster.ClusterInfoAware} and
     * {@link org.springframework.context.ApplicationContextAware} are also available. Note, make sure
     * this variables are defined as <b>transient</b>.
     *
     * @param task The task to execute
     * @return a Future representing pending completion of the task,
     *         and whose <code>get()</code> method will return the task value upon comletion.
     */
    <T, R> AsyncFuture<R> execute(DistributedTask<T, R> task);

    /**
     * Constructs an executor builder allowing to accumlate different tasks required to be executed
     * into a single execute mechanism with all the results reduced by the reducer provided.
     *
     * <p>The reducer can optionally implement {@link com.gigaspaces.async.AsyncResultsModerator} that can control
     * if tasks should continue to accumelate or it should break and execute the reduce operation on the
     * results received so far.
     *
     * <p>Resources defined within processing unit (space node) the task is executed on are accessible by
     * using either the {@link org.springframework.beans.factory.annotation.Autowired} or
     * {@link javax.annotation.Resource} annotations (assuming they are enabled using <code>context:annotation-config</code>).
     * Bean lifecycle methods, such as {@link org.openspaces.core.cluster.ClusterInfoAware} and
     * {@link org.springframework.context.ApplicationContextAware} are also available. Note, make sure
     * this variables are defined as <b>transient</b>.
     * 
     * @param reducer The reducer to reduce the results of all the different tasks added.
     * @return The executor builer.
     */
    <T, R> ExecutorBuilder<T, R> executorBuilder(AsyncResultsReducer<T, R> reducer);
}
