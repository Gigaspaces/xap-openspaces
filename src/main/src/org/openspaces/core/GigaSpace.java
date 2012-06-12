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

import java.io.Serializable;

import net.jini.core.transaction.Transaction;
import net.jini.space.JavaSpace;

import org.openspaces.core.exception.ExceptionTranslator;
import org.openspaces.core.executor.DistributedTask;
import org.openspaces.core.executor.Task;
import org.openspaces.core.transaction.TransactionProvider;
import org.springframework.dao.DataAccessException;

import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.async.AsyncFutureListener;
import com.gigaspaces.async.AsyncResultsReducer;
import com.gigaspaces.client.ClearModifiers;
import com.gigaspaces.client.CountModifiers;
import com.gigaspaces.client.ReadByIdsResult;
import com.gigaspaces.client.ReadModifiers;
import com.gigaspaces.client.TakeByIdsResult;
import com.gigaspaces.client.TakeModifiers;
import com.gigaspaces.client.WriteModifiers;
import com.gigaspaces.query.ISpaceQuery;
import com.gigaspaces.query.IdQuery;
import com.gigaspaces.query.IdsQuery;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.LeaseContext;

/**
 * Provides a simpler interface of both {@link JavaSpace} and GigaSpaces {@link IJSpace} extension
 * utilizing GigaSpaces extended and simplified programming model.
 * Most operations revolve around the use of Objects allowing to use GigaSpaces support for POJOs.
 *
 * <p>Though this interface has a single implementation it is still important to work against the
 * interface as it allows for simpler testing and mocking.
 *
 * <p>Transaction management is implicit and works in a declarative manner. Operations do not accept a
 * transaction object, and will automatically use the {@link TransactionProvider} in order to acquire
 * the current running transaction. If there is no current running transaction the operation will be
 * executed without a transaction.
 *
 * <p>Operations throw a {@link org.springframework.dao.DataAccessException} allowing for simplified
 * development model as it is a runtime exception. The cause of the exception can be acquired from
 * the GigaSpace exception.
 *
 * @author kimchy
 * @see com.gigaspaces.query.ISpaceQuery
 * @see com.j_spaces.core.client.SQLQuery
 * @see org.openspaces.core.transaction.TransactionProvider
 * @see org.openspaces.core.DefaultGigaSpace
 * @see org.springframework.dao.DataAccessExceptionf
 */
public interface GigaSpace {

    /**
     * Returns the name of the GigaSpace. If it is configured with Spring for example, will return the
     * bean name, if not, will default to the space name.
     */
    String getName();

    /**
     * Returns the <code>IJSpace</code> used by this GigaSpace implementation to delegate
     * different space operations.
     *
     * <p>Allows the use of space operations that are not exposed by this interface, as well as use
     * as a parameter to other low level GigaSpace components.
     *
     * <p>If a transaction object is required for low level operations (as low level operations do not
     * have declarative transaction ex) the {@link #getTxProvider()} should be used to acquire the
     * current running transaction.
     */
    IJSpace getSpace();

    /**
     * Returns a clustered view of this {@link org.openspaces.core.GigaSpace} instance.
     * 
     * <pre>
     * GigaSpace gigaSpace = new GigaSpaceConfigurer(space).clustered(true).gigaSpace();
     * </pre>
     * 
     * <p>
     * If this instance is already a clustered view (was initially constructed using the clustered
     * flag), will return the same instance being used to issue the call.
     * <p>
     * If this is a GigaSpace that works directly with a cluster member, will return a clustered
     * view (as if it was constructed with the clustered flag set). Note that this method might
     * return different instances when being called by different threads.
     * <p>
     * 
     * <pre>
     * GigaSpace nonClusteredViewOfGigaSpace= ... // acquire non-clustered view of a clustered space
     * GigaSpace space=nonClusteredViewofGigaSpace.getClustered();
     * // space != nonClusteredViewOfSpace
     * </pre>
     * 
     * <pre>
     * GigaSpace clusteredViewOfGigaSpace= ... // acquire clustered view of a clustered space
     * GigaSpace space=clusteredViewofGigaSpace.getClustered();
     * // space == clusteredViewOfSpace
     * </pre>
     * 
     * @see GigaSpaceConfigurer#clustered(boolean)
     */
    GigaSpace getClustered();

    /**
     * Returns the transaction provider allowing to access the current running transaction.
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
     * <li>All entries and templates are deleted.</li>
     * <li>All storage adapter contexts are closed.</li>
     * <li>All engine threads are terminated.</li>
     * <li>The engine re-initializes itself.</li>
     * </ul>
     *
     * @throws DataAccessException
     * @deprecated Since 8.0.2. The processing unit instance that contain this space instance should be restarted instead, 
     * or if the entire space was meant to be cleaned, the entire processing unit should be undeployed and redeployed.
     * Using this method is strongly not recommended because it will not invoke any space mode change events registered components and
     * it is not a cluster wide operation.
     */
    @Deprecated
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
     * <p>Notice: The clear operation does not remove notify templates, i.e. registration for notifications.
     *
     * @param template the template to use for matching
     * @throws DataAccessException In the event of an error, DataAccessException will
     *         wrap a ClearException, accessible via DataAccessException.getRootCause().
     */
    void clear(Object template) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #clear(Object, ClearModifiers)} instead.
     */
    @Deprecated
    int clear(Object template, int modifiers);

    /**
     * Removes the entries that match the specified template and the specified transaction from this space.
     *
     * <p>If the clear operation conducted without transaction (null as value) it will clear all entries that
     * are not under transaction. Therefore entries under transaction would not be removed from the space.
     *
     * <p>The clear operation supports inheritance, therefore template class matching objects
     * and its sub classes matching objects are part of the candidates population
     * to be removed from the space. You can in fact remove all space objects (that are not under
     * transaction) by calling: <code>gigaSpace.clear(null)</code>.
     *
     * <p>Notice: The clear operation does not remove notify templates i.e. registration for notifications.
     *
     * @param template the template to use for matching
     * @param modifiers one or a union of {@link ClearModifiers}.
     * 
     * @throws DataAccessException In the event of an error, DataAccessException will
     *         wrap a ClearException, accessible via DataAccessException.getRootCause().
     * @since 9.0.1
     */
    int clear(Object template, ClearModifiers modifiers);

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
     */
    int count(Object template) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #count(Object, CountModifiers)} instead.
     */
    @Deprecated
    int count(Object template, int modifiers) throws DataAccessException;

    /**
     * Count any matching entries from the space. If this is running within a transaction
     * will count all the entries visible under the transaction.
     *
     * <p>Allows to specify modifiers using {@link ReadModifiers}
     * which allows to programmatically control the isolation level this count operation
     * will be performed under.
     *
     * @param template  The template used for matching. Matching is done against the
     *                  template with <code>null</code> fields being wildcards
     *                  ("match anything") other fields being values ("match
     *                  exactly on the serialized form").
     * @param modifiers one or a union of {@link CountModifiers}.
     * @return The number of matching entries
     * @throws DataAccessException
     * @since 9.0.1
     */
    int count(Object template, CountModifiers modifiers) throws DataAccessException;

    /**
     * <p>The process of serializing an entry for transmission to a JavaSpaces service will
     * be identical if the same entry is used twice. This is most likely to be an issue with
     * templates that are used repeatedly to search for entries with read or take. 
     *
     * <p>The client-side
     * implementations of read and take cannot reasonably avoid this duplicated effort, since they
     * have no efficient way of checking whether the same template is being used without intervening
     * modification. 
     *
     * <p>The snapshot method gives the JavaSpaces service implementor a way to reduce
     * the impact of repeated use of the same entry. Invoking snapshot with an Entry will return
     * another Entry object that contains a snapshot of the original entry. Using the returned snapshot
     * entry is equivalent to using the unmodified original entry in all operations on the same JavaSpaces
     * service. 
     *
     * <p>Modifications to the original entry will not affect the snapshot. You can snapshot a null
     * template; snapshot may or may not return null given a null template. The entry returned from snapshot
     * will be guaranteed equivalent to the original unmodified object only when used with the space. Using
     * the snapshot with any other JavaSpaces service will generate an <code>IllegalArgumentException</code> unless the
     * other space can use it because of knowledge about the JavaSpaces service that generated the snapshot.
     *
     * <p>The snapshot will be a different object from the original, may or may not have the same hash code,
     * and equals may or may not return true when invoked with the original object, even if the original object
     * is unmodified. A snapshot is guaranteed to work only within the virtual machine in which it was generated.
     * If a snapshot is passed to another virtual machine (for example, in a parameter of an RMI call), using
     * it--even with the same JavaSpaces service--may generate an <code>IllegalArgumentException</code>.
     *
     * @param entry The entry to snapshot
     * @return The snapshot
     * @throws DataAccessException
     */
    <T> ISpaceQuery<T> snapshot(Object entry) throws DataAccessException;

    /**
     * Read an object from the space matching its id and the class. Returns
     * <code>null</code> if there is no match.
     *
     * <p>The timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * <p>Note, if the space is partitioned, and the Entry has a specific property
     * for its routing value, the operation will broadcast to all partitions. The
     * {@link #readById(Class, Object, Object)} can be used to specify the routing.
     *
     * @param clazz The class of the entry
     * @param id    The id of the entry
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     */
    <T> T readById(Class<T> clazz, Object id) throws DataAccessException;

    /**
     * Read an object from the space matching its id, the class and the routing value. Returns
     * <code>null</code> if there is no match.
     *
     * <p>The timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * @param clazz   The class of the entry
     * @param id      The id of the entry
     * @param routing The routing value
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     */
    <T> T readById(Class<T> clazz, Object id, Object routing) throws DataAccessException;

    /**
     * Read an object from the space matching its id, the class and the routing value. Returns
     * <code>null</code> if there is no match within the specified timeout.
     *
     * @param clazz   The class of the entry
     * @param id      The id of the entry
     * @param routing The routing value
     * @param timeout The timeout value to wait for a matching entry if it does not exists within the space
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     */
    <T> T readById(Class<T> clazz, Object id, Object routing, long timeout) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #readById(Class, Object, Object, long, ReadModifiers)} instead.
     */
    @Deprecated
    <T> T readById(Class<T> clazz, Object id, Object routing, long timeout, int modifiers) throws DataAccessException;

    /**
     * Read an object from the space matching its id, the class and the routing value. Returns
     * <code>null</code> if there is no match within the specified timeout.
     *
     * @param clazz     The class of the entry
     * @param id        The id of the entry
     * @param routing   The routing value
     * @param timeout   The timeout value to wait for a matching entry if it does not exists within the space
     * @param modifiers one or a union of {@link ReadModifiers}.
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     * @since 9.0.1
     */
    <T> T readById(Class<T> clazz, Object id, Object routing, long timeout, ReadModifiers modifiers) throws DataAccessException;

    /**
     * Read an object from the space matching the specified id query. Returns
     * <code>null</code> if there is no match.
     *
     * <p>The timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * <p>Note, if the space is partitioned, and the Entry has a specific property
     * for its routing value, the operation will broadcast to all partitions. The
     * {@link #readById(Class, Object, Object)} can be used to specify the routing.
     * 
     * @param query Query to search by.
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     * @since 8.0
     */
    <T> T readById(IdQuery<T> query) throws DataAccessException;

    /**
     * Read an object from the space matching the specified id query. Returns
     * <code>null</code> if there is no match within the specified timeout.
     *
     * @param query Query to search by.
     * @param timeout The timeout value to wait for a matching entry if it does not exists within the space
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     * @since 8.0
     */
    <T> T readById(IdQuery<T> query, long timeout) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #readById(IdQuery, long, ReadModifiers)} instead.
     */
    @Deprecated
    <T> T readById(IdQuery<T> query, long timeout, int modifiers) throws DataAccessException;

    /**
     * Read an object from the space matching the specified id query. Returns
     * <code>null</code> if there is no match within the specified timeout.
     *
     * @param query Query to search by.
     * @param timeout The timeout value to wait for a matching entry if it does not exists within the space
     * @param modifiers one or a union of {@link ReadModifiers}.
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     * @since 9.0.1
     */
    <T> T readById(IdQuery<T> query, long timeout, ReadModifiers modifiers) throws DataAccessException;

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
     */
    <T> T read(T template, long timeout) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #read(Object, long, ReadModifiers)} instead.
     */
    @Deprecated
    <T> T read(T template, long timeout, int modifiers) throws DataAccessException;

    /**
     * Read any matching object from the space, blocking until one exists. Return
     * <code>null</code> if the timeout expires.
     *
     * <p>Overloads {@link #read(Object,long)} by adding a <code>modifiers</code> parameter.
     * Equivalent when called with the default modifier - {@link ReadModifiers#REPEATABLE_READ}.
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
     * @param modifiers one or a union of {@link ReadModifiers}.
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     * @since 9.0.1
     */
    <T> T read(T template, long timeout, ReadModifiers modifiers) throws DataAccessException;

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
     */
    <T> T read(ISpaceQuery<T> template) throws DataAccessException;

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
     */
    <T> T read(ISpaceQuery<T> template, long timeout) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #read(ISpaceQuery, long, ReadModifiers)} instead.
     */
    @Deprecated
    <T> T read(ISpaceQuery<T> template, long timeout, int modifiers) throws DataAccessException;

    /**
     * Read any matching object from the space, blocking until one exists. Return
     * <code>null</code> if the timeout expires.
     *
     * <p>Overloads {@link #read(Object,long)} by adding a <code>modifiers</code> parameter.
     * Equivalent when called with the default modifier - {@link ReadModifiers#REPEATABLE_READ}.
     * Modifiers are used to define the behavior of a read operation.
     *
     * @param template  A query to be executed against the space. Most common one is
     *                  {@link com.j_spaces.core.client.SQLQuery}.
     * @param timeout   How long the client is willing to wait for a
     *                  transactionally proper matching object. A timeout of
     *                  {@link JavaSpace#NO_WAIT} means to wait no time at all; this is
     *                  equivalent to a wait of zero.
     * @param modifiers one or a union of {@link ReadModifiers}.
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     * @since 9.0.1
     */
    <T> T read(ISpaceQuery<T> template, long timeout, ReadModifiers modifiers) throws DataAccessException;

    /**
     * Reads any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
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
     */
    <T> AsyncFuture<T> asyncRead(T template) throws DataAccessException;

    /**
     * Reads any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
     *
     * <p>Note, the timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * @param template The template used for matching. Matching is done against
     *                 template with <code>null</code> fields being
     *                 wildcards ("match anything") other fields being values ("match
     *                 exactly on the serialized form").
     * @param listener A listener to be notified when a result arrives
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     */
    <T> AsyncFuture<T> asyncRead(T template, AsyncFutureListener<T> listener) throws DataAccessException;

    /**
     * Reads any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
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
     */
    <T> AsyncFuture<T> asyncRead(T template, long timeout) throws DataAccessException;

    /**
     * Reads any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
     *
     * @param template The template used for matching. Matching is done against
     *                 template with <code>null</code> fields being
     *                 wildcards ("match anything") other fields being values ("match
     *                 exactly on the serialized form").
     * @param timeout  How long the client is willing to wait for a
     *                 transactionally proper matching object. A timeout of
     *                 {@link JavaSpace#NO_WAIT} means to wait no time at all; this is
     *                 equivalent to a wait of zero.
     * @param listener A listener to be notified when a result arrives
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     */
    <T> AsyncFuture<T> asyncRead(T template, long timeout, AsyncFutureListener<T> listener) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #asyncRead(Object, long, ReadModifiers)} instead.
     */
    @Deprecated
    <T> AsyncFuture<T> asyncRead(T template, long timeout, int modifiers) throws DataAccessException;

    /**
     * Reads any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
     *
     * <p>Overloads {@link #asyncRead(Object,long)} by adding a <code>modifiers</code> parameter.
     * Equivalent when called with the default modifier - {@link ReadModifiers#REPEATABLE_READ}.
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
     * @param modifiers one or a union of {@link ReadModifiers}.
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     * @since 9.0.1
     */
    <T> AsyncFuture<T> asyncRead(T template, long timeout, ReadModifiers modifiers) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #asyncRead(Object, long, ReadModifiers, AsyncFutureListener)} instead.
     */
    @Deprecated
    <T> AsyncFuture<T> asyncRead(T template, long timeout, int modifiers, AsyncFutureListener<T> listener) throws DataAccessException;

    /**
     * Reads any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
     *
     * <p>Overloads {@link #asyncRead(Object,long)} by adding a <code>modifiers</code> parameter.
     * Equivalent when called with the default modifier - {@link ReadModifiers#REPEATABLE_READ}.
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
     * @param modifiers one or a union of {@link ReadModifiers}.
     * @param listener  A listener to be notified when a result arrives
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     * @since 9.0.1
     */
    <T> AsyncFuture<T> asyncRead(T template, long timeout, ReadModifiers modifiers, AsyncFutureListener<T> listener) throws DataAccessException;

    /**
     * Reads any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
     *
     * <p>Note, the timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     */
    <T> AsyncFuture<T> asyncRead(ISpaceQuery<T> template) throws DataAccessException;

    /**
     * Reads any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
     *
     * <p>Note, the timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.
     * @param listener A listener to be notified when a result arrives
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     */
    <T> AsyncFuture<T> asyncRead(ISpaceQuery<T> template, AsyncFutureListener<T> listener) throws DataAccessException;

    /**
     * Reads any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.
     * @param timeout  How long the client is willing to wait for a
     *                 transactionally proper matching object. A timeout of
     *                 {@link JavaSpace#NO_WAIT} means to wait no time at all; this is
     *                 equivalent to a wait of zero.
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     */
    <T> AsyncFuture<T> asyncRead(ISpaceQuery<T> template, long timeout) throws DataAccessException;

    /**
     * Reads any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.
     * @param timeout  How long the client is willing to wait for a
     *                 transactionally proper matching object. A timeout of
     *                 {@link JavaSpace#NO_WAIT} means to wait no time at all; this is
     *                 equivalent to a wait of zero.
     * @param listener A listener to be notified when a result arrives
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     */
    <T> AsyncFuture<T> asyncRead(ISpaceQuery<T> template, long timeout, AsyncFutureListener<T> listener) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #asyncRead(ISpaceQuery, long, ReadModifiers)} instead.
     */
    @Deprecated
    <T> AsyncFuture<T> asyncRead(ISpaceQuery<T> template, long timeout, int modifiers) throws DataAccessException;

    /**
     * Reads any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
     *
     * <p>Overloads {@link #asyncRead(Object,long)} by adding a <code>modifiers</code> parameter.
     * Equivalent when called with the default modifier - {@link ReadModifiers#REPEATABLE_READ}.
     * Modifiers are used to define the behavior of a read operation.
     *
     * @param template  A query to be executed against the space. Most common one is
     *                  {@link com.j_spaces.core.client.SQLQuery}.
     * @param timeout   How long the client is willing to wait for a
     *                  transactionally proper matching object. A timeout of
     *                  {@link JavaSpace#NO_WAIT} means to wait no time at all; this is
     *                  equivalent to a wait of zero.
     * @param modifiers one or a union of {@link ReadModifiers}.
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     * @since 9.0.1
     */
    <T> AsyncFuture<T> asyncRead(ISpaceQuery<T> template, long timeout, ReadModifiers modifiers) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #asyncRead(ISpaceQuery, long, ReadModifiers, AsyncFutureListener)} instead.
     */
    @Deprecated
    <T> AsyncFuture<T> asyncRead(ISpaceQuery<T> template, long timeout, int modifiers, AsyncFutureListener<T> listener) throws DataAccessException;

    /**
     * Reads any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
     *
     * <p>Overloads {@link #asyncRead(Object,long)} by adding a <code>modifiers</code> parameter.
     * Equivalent when called with the default modifier - {@link ReadModifiers#REPEATABLE_READ}.
     * Modifiers are used to define the behavior of a read operation.
     *
     * @param template  A query to be executed against the space. Most common one is
     *                  {@link com.j_spaces.core.client.SQLQuery}.
     * @param timeout   How long the client is willing to wait for a
     *                  transactionally proper matching object. A timeout of
     *                  {@link JavaSpace#NO_WAIT} means to wait no time at all; this is
     *                  equivalent to a wait of zero.
     * @param modifiers one or a union of {@link ReadModifiers}.
     * @param listener  A listener to be notified when a result arrives
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     * @since 9.0.1
     */
    <T> AsyncFuture<T> asyncRead(ISpaceQuery<T> template, long timeout, ReadModifiers modifiers, AsyncFutureListener<T> listener) throws DataAccessException;

    /**
     * Read an object from the space matching its id and the class. Returns
     * <code>null</code> if there is no match.
     *
     * <p>Matching and timeouts are done as in
     * <code>readById</code>, except that blocking in this call is done only if
     * necessary to wait for transactional state to settle.
     *
     * <p>The timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * <p>Note, if the space is partitioned, and the Entry has a specific property
     * for its routing value, the operation will broadcast to all partitions. The
     * {@link #readById(Class, Object, Object)} can be used to specify the routing.
     *
     * @param clazz The class of the entry
     * @param id    The id of the entry
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     */
    <T> T readIfExistsById(Class<T> clazz, Object id) throws DataAccessException;

    /**
     * Read an object from the space matching its id, the class and the routing value. Returns
     * <code>null</code> if there is no match.
     *
     * <p>Matching and timeouts are done as in
     * <code>readById</code>, except that blocking in this call is done only if
     * necessary to wait for transactional state to settle.
     *
     * <p>The timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * @param clazz   The class of the entry
     * @param id      The id of the entry
     * @param routing The routing value
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     */
    <T> T readIfExistsById(Class<T> clazz, Object id, Object routing) throws DataAccessException;

    /**
     * Read an object from the space matching its id, the class and the routing value. Returns
     * <code>null</code> if there is no match within the specified timeout.
     *
     * <p>Matching and timeouts are done as in
     * <code>readById</code>, except that blocking in this call is done only if
     * necessary to wait for transactional state to settle.
     *
     * @param clazz   The class of the entry
     * @param id      The id of the entry
     * @param routing The routing value
     * @param timeout The timeout value to wait for a matching entry if it does not exists within the space
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     */
    <T> T readIfExistsById(Class<T> clazz, Object id, Object routing, long timeout) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #readIfExistsById(Class, Object, Object, long, ReadModifiers)} instead.
     */
    @Deprecated
    <T> T readIfExistsById(Class<T> clazz, Object id, Object routing, long timeout, int modifiers) throws DataAccessException;

    /**
     * Read an object from the space matching its id, the class and the routing value. Returns
     * <code>null</code> if there is no match within the specified timeout.
     *
     * <p>Matching and timeouts are done as in
     * <code>readById</code>, except that blocking in this call is done only if
     * necessary to wait for transactional state to settle.
     *
     * @param clazz     The class of the entry
     * @param id        The id of the entry
     * @param routing   The routing value
     * @param timeout   The timeout value to wait for a matching entry if it does not exists within the space
     * @param modifiers one or a union of {@link ReadModifiers}.
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     * @since 9.0.1
     */
    <T> T readIfExistsById(Class<T> clazz, Object id, Object routing, long timeout, ReadModifiers modifiers) throws DataAccessException;

    /**
     * Read an object from the space matching the specified id query. Returns
     * <code>null</code> if there is no match.
     *
     * <p>The timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * <p>Note, if the space is partitioned, and the Entry has a specific property
     * for its routing value, the operation will broadcast to all partitions. The
     * {@link #readById(Class, Object, Object)} can be used to specify the routing.
     *
     * <p>Matching and timeouts are done as in
     * <code>readById</code>, except that blocking in this call is done only if
     * necessary to wait for transactional state to settle.
     * 
     * @param query Query to search by.
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     * @since 8.0
     */
    <T> T readIfExistsById(IdQuery<T> query) throws DataAccessException;
    
    /**
     * Read an object from the space matching the specified id query. Returns
     * <code>null</code> if there is no match within the specified timeout.
     *
     * <p>Matching and timeouts are done as in
     * <code>readById</code>, except that blocking in this call is done only if
     * necessary to wait for transactional state to settle.
     *
     * @param query Query to search by.
     * @param timeout The timeout value to wait for a matching entry if it does not exists within the space
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     * @since 8.0
     */
    <T> T readIfExistsById(IdQuery<T> query, long timeout) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #readIfExistsById(IdQuery, long, ReadModifiers)} instead.
     */
    @Deprecated
    <T> T readIfExistsById(IdQuery<T> query, long timeout, int modifiers) throws DataAccessException;

    /**
     * Read an object from the space matching the specified id query. Returns
     * <code>null</code> if there is no match within the specified timeout.
     *
     * <p>Matching and timeouts are done as in
     * <code>readById</code>, except that blocking in this call is done only if
     * necessary to wait for transactional state to settle.
     *
     * @param query Query to search by.
     * @param timeout The timeout value to wait for a matching entry if it does not exists within the space
     * @param modifiers one or a union of {@link ReadModifiers}.
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     * @since 9.0.1
     */
    <T> T readIfExistsById(IdQuery<T> query, long timeout, ReadModifiers modifiers) throws DataAccessException;

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
     */
    <T> T readIfExists(T template, long timeout) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #readIfExists(Object, long, ReadModifiers)} instead.
     */
    @Deprecated
    <T> T readIfExists(T template, long timeout, int modifiers) throws DataAccessException;

    /**
     * Read any matching object from the space, returning <code>null</code> if
     * there currently is none. Matching and timeouts are done as in
     * <code>read</code>, except that blocking in this call is done only if
     * necessary to wait for transactional state to settle.
     *
     * <p>Overloads {@link #read(Object,long)} by adding a <code>modifiers</code> parameter.
     * Equivalent when called with the default modifier - {@link ReadModifiers#REPEATABLE_READ}.
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
     * @param modifiers one or a union of {@link ReadModifiers}.
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     * @since 9.0.1
     */
    <T> T readIfExists(T template, long timeout, ReadModifiers modifiers) throws DataAccessException;

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
     */
    <T> T readIfExists(ISpaceQuery<T> template) throws DataAccessException;

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
     */
    <T> T readIfExists(ISpaceQuery<T> template, long timeout) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #readIfExists(ISpaceQuery, long, ReadModifiers)} instead.
     */
    @Deprecated
    <T> T readIfExists(ISpaceQuery<T> template, long timeout, int modifiers) throws DataAccessException;

    /**
     * Read any matching object from the space, returning <code>null</code> if
     * there currently is none. Matching and timeouts are done as in
     * <code>read</code>, except that blocking in this call is done only if
     * necessary to wait for transactional state to settle.
     *
     * <p>Overloads {@link #read(Object,long)} by adding a <code>modifiers</code> parameter.
     * Equivalent when called with the default modifier - {@link ReadModifiers#REPEATABLE_READ}.
     * Modifiers are used to define the behavior of a read operation.
     *
     * @param template  A query to be executed against the space. Most common one is
     *                  {@link com.j_spaces.core.client.SQLQuery}.
     * @param timeout   How long the client is willing to wait for a
     *                  transactionally proper matching object. A timeout of
     *                  {@link JavaSpace#NO_WAIT} means to wait no time at all; this is
     *                  equivalent to a wait of zero.
     * @param modifiers one or a union of {@link ReadModifiers}.
     * @return A copy of the object read from the space.
     * @throws DataAccessException
     * @since 9.0.1
     */
    <T> T readIfExists(ISpaceQuery<T> template, long timeout, ReadModifiers modifiers) throws DataAccessException;
    
    /**
     * Read any matching entries from the space. Matching is done as in
     * <code>read</code> without timeout ({@link JavaSpace#NO_WAIT}). Returns an
     * unbounded array of matches. Returns an empty array if no match was found.
     * Same as calling {@link #readMultiple(Object, int) readMultiple(template, Integer.MAX_VALUE)}.
     *
     * @param template   The template used for matching. Matching is done against
     *                   the template with <code>null</code> fields being.
     *                   wildcards ("match anything") other fields being values ("match
     *                   exactly on the serialized form"). The template can also be one
     *                   of the different {@link com.gigaspaces.query.ISpaceQuery} classes
     * @return A copy of the entries read from the space.
     * @throws DataAccessException In the event of a read error, DataAccessException will
     *         wrap a ReadMultipleException, accessible via DataAccessException.getRootCause().
     */
    <T> T[] readMultiple(T template) throws DataAccessException;
        
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
     *                   of the different {@link com.gigaspaces.query.ISpaceQuery} classes
     * @param maxEntries A limit on the number of entries to be returned. Use
     *                   {@link Integer#MAX_VALUE} for the uppermost limit.
     * @return A copy of the entries read from the space.
     * @throws DataAccessException In the event of a read error, DataAccessException will
     *         wrap a ReadMultipleException, accessible via DataAccessException.getRootCause().
     */
    <T> T[] readMultiple(T template, int maxEntries) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #readMultiple(Object, int, ReadModifiers)} instead.
     */
    @Deprecated
    <T> T[] readMultiple(T template, int maxEntries, int modifiers) throws DataAccessException;

    /**
     * Read any matching entries from the space. Matching is done as in
     * <code>read</code> without timeout ({@link JavaSpace#NO_WAIT}). Returns an
     * array with matches bound by <code>maxEntries</code>. Returns an
     * empty array if no match was found.
     *
     * <p>Overloads {@link #readMultiple(Object,int)} by adding a
     * <code>modifiers</code> parameter. Equivalent when called with the default
     * modifier - {@link ReadModifiers#REPEATABLE_READ}. Modifiers
     * are used to define the behavior of a read operation.
     *
     * 
     * @param template   The template used for matching. Matching is done against
     *                   the template with <code>null</code> fields being.
     *                   wildcards ("match anything") other fields being values ("match
     *                   exactly on the serialized form"). The template can also be one
     *                   of the different {@link com.gigaspaces.query.ISpaceQuery} classes
     * @param maxEntries A limit on the number of entries to be returned. Use
     *                   {@link Integer#MAX_VALUE} for the uppermost limit.
     * @param modifiers one or a union of {@link ReadModifiers}.
     * @return A copy of the entries read from the space.
     * @throws DataAccessException In the event of a read error, DataAccessException will
     *         wrap a ReadMultipleException, accessible via DataAccessException.getRootCause().
     * @since 9.0.1
     */
    <T> T[] readMultiple(T template, int maxEntries, ReadModifiers modifiers) throws DataAccessException;

    /**
     * Read any matching entries from the space. Matching is done as in
     * <code>read</code> without timeout ({@link JavaSpace#NO_WAIT}). Returns an
     * unbounded array of matches. Returns an
     * empty array if no match was found.
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.     
     * @throws DataAccessException In the event of a read error, DataAccessException will
     *         wrap a ReadMultipleException, accessible via DataAccessException.getRootCause().
     * @return A copy of the entries read from the space.
     */
    <T> T[] readMultiple(ISpaceQuery<T> template) throws DataAccessException;
    
    /**
     * Read any matching entries from the space. Matching is done as in
     * <code>read</code> without timeout ({@link JavaSpace#NO_WAIT}). Returns an
     * array with matches bound by <code>maxEntries</code>. Returns an
     * empty array if no match was found.
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.
     * @param maxEntries A limit on the number of entries to be returned. Use
     *                   {@link Integer#MAX_VALUE} for the uppermost limit.
     * @throws DataAccessException In the event of a read error, DataAccessException will
     *         wrap a ReadMultipleException, accessible via DataAccessException.getRootCause().
     * @return A copy of the entries read from the space.
     */
    <T> T[] readMultiple(ISpaceQuery<T> template, int maxEntries) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #readMultiple(ISpaceQuery, int, ReadModifiers)} instead.
     */
    @Deprecated
    <T> T[] readMultiple(ISpaceQuery<T> template, int maxEntries, int modifiers) throws DataAccessException;

    /**
     * Read any matching entries from the space. Matching is done as in
     * <code>read</code> without timeout ({@link JavaSpace#NO_WAIT}). Returns an
     * array with matches bound by <code>maxEntries</code>. Returns an
     * empty array if no match was found.
     *
     * <p>Overloads {@link #readMultiple(Object,int)} by adding a
     * <code>modifiers</code> parameter. Equivalent when called with the default
     * modifier - {@link ReadModifiers#REPEATABLE_READ}. Modifiers
     * are used to define the behavior of a read operation.
     *
     * 
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.  
     * @param maxEntries A limit on the number of entries to be returned. Use
     *                   {@link Integer#MAX_VALUE} for the uppermost limit.
     * @param modifiers one or a union of {@link ReadModifiers}.
     * @return A copy of the entries read from the space.
     * @throws DataAccessException In the event of a read error, DataAccessException will
     *         wrap a ReadMultipleException, accessible via DataAccessException.getRootCause().
     * @since 9.0.1
     */
    <T> T[] readMultiple(ISpaceQuery<T> template, int maxEntries, ReadModifiers modifiers) throws DataAccessException;

    /**
     * Read objects from the space matching their IDs and the specified class.
     *
     * <p>Note, if the space is partitioned and the Class defines a specific property
     * for its routing value (which means that the ID property is not used for routing),
     * the operation will broadcast to all partitions. The {@link #readByIds(Class, Object[], Object)} overload
     * can be used to specify the routing explicitly.
     *
     * @param clazz The class.
     * @param ids   The object IDs array.
     * @return a ReadByIdsResult containing the matched results.
     */
    <T> ReadByIdsResult<T> readByIds(Class<T> clazz, Object[] ids) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #readByIds(Class, Object[], Object, ReadModifiers)} instead.
     */
    @Deprecated
    <T> ReadByIdsResult<T> readByIds(Class<T> clazz, Object[] ids, int modifiers) throws DataAccessException;

    /**
     * Read objects from the space matching their IDs, the specified class and routing key.
     * 
     * <p>Note, if routing key is null and the cluster is partitioned, the operation will broadcast
     * to all of the partitions.
     * 
     * @param clazz         The class.
     * @param ids           The object IDs array.
     * @param routingKey    The routing of the provided object IDs.
     * @return a ReadByIdsResult containing the matched results.
     */
    <T> ReadByIdsResult<T> readByIds(Class<T> clazz, Object[] ids, Object routingKey) throws DataAccessException;
    
    /**
     * @deprecated since 9.0.1 - use {@link #readByIds(Class, Object[], Object, ReadModifiers)} instead.
     */
    @Deprecated
    <T> ReadByIdsResult<T> readByIds(Class<T> clazz, Object[] ids, Object routingKey, int modifiers) throws DataAccessException;

    /**
     * Read objects from the space matching their IDs, the specified class type and routing key, with the
     * provided {@link ReadModifiers}.
     * 
     * <p>Note, if routing key is null and the cluster is partitioned, the operation will broadcast
     * to all of the partitions.
     * 
     * <p>{@link ReadModifiers#FIFO} is not supported by this operation -
     * the results are always ordered in correlation with the input IDs array.
     * 
     * @param clazz         The class.
     * @param ids           The object IDs array.
     * @param routingKey    The routing of the provided object IDs.
     * @param modifiers     The read modifier to use (One or several of {@link ReadModifiers}).
     * @return a ReadByIdsResult containing the matched results.
     * @since 9.0.1
     */
    <T> ReadByIdsResult<T> readByIds(Class<T> clazz, Object[] ids, Object routingKey, ReadModifiers modifiers) throws DataAccessException;

    /**
     * Read objects from the space matching their IDs, the specified class and the routing keys.
     * 
     * <p>Note, the IDs array and routing keys array are correlated and should be of the same size.
     * The routing key of ID i in the IDs array is the element at position i in the routing keys array.
     * If routingKeys is <code>null</code> and the cluster is partitioned, the operation will broadcast to
     * all of the partitions. 
     *
     * @param clazz         The class.
     * @param ids           The object IDs array.
     * @param routingKeys   The object routing keys array.
     * @return a ReadByIdsResult containing the matched results.
     */
    <T> ReadByIdsResult<T> readByIds(Class<T> clazz, Object[] ids, Object[] routingKeys) throws DataAccessException;       
    
    /**
     * @deprecated since 9.0.1 - use {@link #readByIds(Class, Object[], Object[], ReadModifiers)} instead.
     */
    @Deprecated
    <T> ReadByIdsResult<T> readByIds(Class<T> clazz, Object[] ids, Object[] routingKeys, int modifiers) throws DataAccessException;

    /**
     * Read objects from the space matching their IDs, the specified class and the routing keys, with the
     * provided {@link ReadModifiers}.
     * 
     * <p>Note, the IDs array and routing keys array are correlated and should be of the same size.
     * The routing key of ID i in the IDs array is the element at position i in the routing keys array.
     * If routingKeys is <code>null</code> and the cluster is partitioned, the operation will broadcast to
     * all of the partitions. 
     * 
     * <p>{@link ReadModifiers#FIFO} is not supported by this operation -
     * the results are always ordered in correlation with the input IDs array.
     * 
     * @param clazz         The class type.
     * @param ids           The objects\ IDs array.
     * @param routingKeys   The object routing keys array.
     * @param modifiers The read modifier to use (One or several of {@link ReadModifiers}).
     * @return a ReadByIdsResult containing the matched results.
     * @since 9.0.1
     */
    <T> ReadByIdsResult<T> readByIds(Class<T> clazz, Object[] ids, Object[] routingKeys, ReadModifiers modifiers) throws DataAccessException;

    /**
     * Read objects from the space matching the specified IDs query.
     * 
     * @param query Query to search by.
     * @return a ReadByIdsResult containing the matched results.
     * @since 8.0
     */
    <T> ReadByIdsResult<T> readByIds(IdsQuery<T> query) throws DataAccessException;
    
    /**
     * @deprecated since 9.0.1 - use {@link #readByIds(IdQuery, ReadModifiers)} instead.
     */
    @Deprecated
    <T> ReadByIdsResult<T> readByIds(IdsQuery<T> query, int modifiers) throws DataAccessException;

    /**
     * Read objects from the space matching the specified IDs query, with the
     * provided {@link ReadModifiers}. 
     * 
     * @param query Query to search by.
     * @param modifiers The read modifier to use (One or several of {@link ReadModifiers}).
     * @return a ReadByIdsResult containing the matched results.
     * @since 9.0.1
     */
    <T> ReadByIdsResult<T> readByIds(IdsQuery<T> query, ReadModifiers modifiers) throws DataAccessException;

    /**
     * Take (remove) objects from the space matching their IDs and the specified class.
     * 
     * <p>Note, if the space is partitioned, or the Class defines a specific property
     * for its routing value (which means that the ID property is not used for routing),
     * the operation will broadcast to all partitions. The {@link #takeByIds(Class, Object[], Object)} overload
     * can be used to specify the routing explicitly.
     *
     * @param clazz The class.
     * @param ids   The object IDs array.
     * @return a TakeByIdsResult containing the matched results.
     */
    <T> TakeByIdsResult<T> takeByIds(Class<T> clazz, Object[] ids) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #takeByIds(Class, Object[], Object, TakeModifiers)} instead.
     */
    @Deprecated
    <T> TakeByIdsResult<T> takeByIds(Class<T> clazz, Object[] ids, int modifiers) throws DataAccessException;

    /**
     * Take (remove) objects from the space matching their IDs, the specified class and routing key.
     * 
     * <p>Note, if routing key is null and the cluster is partitioned, the operation will broadcast
     * to all of the partitions.
     * 
     * @param clazz         The class.
     * @param ids           The object IDs array.
     * @param routingKey    The routing of the provided object IDs.
     * @return a TakeByIdsResult containing the matched results.
     */
    <T> TakeByIdsResult<T> takeByIds(Class<T> clazz, Object[] ids, Object routingKey) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #takeByIds(Class, Object[], Object, TakeModifiers)} instead.
     */
    @Deprecated
    <T> TakeByIdsResult<T> takeByIds(Class<T> clazz, Object[] ids, Object routingKey, int modifiers) throws DataAccessException;

    /**
     * Take (remove) objects from the space matching their IDs, the specified class type and routing key, with the
     * provided {@link ReadModifiers}.
     * 
     * <p>Note, if routing key is null and the cluster is partitioned, the operation will broadcast
     * to all of the partitions.
     *
     * <p>{@link ReadModifiers#FIFO} is not supported by this operation -
     * the results are always ordered in correlation with the input IDs array.
     * 
     * @param clazz         The class.
     * @param ids           The object IDs array.
     * @param routingKey    The routing of the provided object IDs.
     * @param modifiers The read modifier to use (One or several of {@link ReadModifiers}).
     * @return a TakeByIdsResult containing the matched results.
     */
    <T> TakeByIdsResult<T> takeByIds(Class<T> clazz, Object[] ids, Object routingKey, TakeModifiers modifiers) throws DataAccessException;

    /**
     * Take (remove) objects from the space matching their IDs, the specified class and the routing keys.
     * 
     * <p>Note, the IDs array and routing keys array are correlated and should be of the same size.
     * The routing key of ID i in the IDs array is the element at position i in the routing keys array.
     * If routingKeys is <code>null</code> and the cluster is partitioned, the operation will broadcast to
     * all of the partitions. 
     * 
     * @param clazz         The class.
     * @param ids           The object IDs array.
     * @param routingKeys   The object routing keys array.
     * @return a TakeByIdsResult containing the matched results.
     */
    <T> TakeByIdsResult<T> takeByIds(Class<T> clazz, Object[] ids, Object[] routingKeys) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #takeByIds(Class, Object[], Object[], TakeModifiers)} instead.
     */
    @Deprecated
    <T> TakeByIdsResult<T> takeByIds(Class<T> clazz, Object[] ids, Object[] routingKeys, int modifiers) throws DataAccessException;   

    /**
     * Take (remove) objects from the space matching their IDs, the specified class and the routing keys, with the
     * provided {@link ReadModifiers}.
     * 
     * <p>Note, the IDs array and routing keys array are correlated and should be of the same size.
     * The routing key of ID i in the IDs array is the element at position i in the routing keys array.
     * If routingKeys is <code>null</code> and the cluster is partitioned, the operation will broadcast to
     * all of the partitions. 
     *
     * <p>{@link ReadModifiers#FIFO} is not supported by this operation -
     * the results are always ordered in correlation with the input IDs array.
     * 
     * @param clazz     The class type.
     * @param ids           The objects\ IDs array.
     * @param routingKeys   The object routing keys array.
     * @param modifiers The read modifier to use (One or several of {@link ReadModifiers}).
     * @return a TakeByIdsResult containing the matched results.
     * @since 9.0.1
     */
    <T> TakeByIdsResult<T> takeByIds(Class<T> clazz, Object[] ids, Object[] routingKeys, TakeModifiers modifiers) throws DataAccessException;   

    /**
     * Take (remove) objects from the space matching the specified IDs query.
     * 
     * @param query Query to search by.
     * @return a ReadByIdsResult containing the matched results.
     * @since 8.0
     */
    <T> TakeByIdsResult<T> takeByIds(IdsQuery<T> query) throws DataAccessException;
    
    /**
     * @deprecated since 9.0.1 - use {@link #takeByIds(IdQuery, TakeModifiers)} instead.
     */
    @Deprecated
    <T> TakeByIdsResult<T> takeByIds(IdsQuery<T> query, int modifiers) throws DataAccessException;

    /**
     * Take (remove) objects from the space matching the specified IDs query, with the
     * provided {@link ReadModifiers}. 
     * 
     * @param query Query to search by.
     * @param modifiers The read modifier to use (One or several of {@link ReadModifiers}).
     * @return a ReadByIdsResult containing the matched results.
     * @since 9.0.1
     */
    <T> TakeByIdsResult<T> takeByIds(IdsQuery<T> query, TakeModifiers modifiers) throws DataAccessException;

    /**
     * Take (remove) an object from the space matching its id and the class. Returns
     * <code>null</code> if there is no match.
     *
     * <p>The timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * <p>Note, if the space is partitioned, and the Entry has a specific property
     * for its routing value, the operation will broadcast to all partitions. The
     * {@link #takeById(Class, Object, Object)} can be used to specify the routing.
     *
     * @param clazz The class of the entry
     * @param id    The id of the entry
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     */
    <T> T takeById(Class<T> clazz, Object id) throws DataAccessException;

    /**
     * Take (remove) an object from the space matching its id, the class and the routing value. Returns
     * <code>null</code> if there is no match.
     *
     * <p>The timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * @param clazz   The class of the entry
     * @param id      The id of the entry
     * @param routing The routing value
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     */
    <T> T takeById(Class<T> clazz, Object id, Object routing) throws DataAccessException;

    /**
     * Take (remove) an object from the space matching its id, the class and the routing value. Returns
     * <code>null</code> if there is no match within the specified timeout.
     *
     * @param clazz   The class of the entry
     * @param id      The id of the entry
     * @param routing The routing value
     * @param timeout The timeout value to wait for a matching entry if it does not exists within the space
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     */
    <T> T takeById(Class<T> clazz, Object id, Object routing, long timeout) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #takeById(Class, Object, Object, long, ReadModifiers)} instead.
     */
    @Deprecated
    <T> T takeById(Class<T> clazz, Object id, Object routing, long timeout, int modifiers) throws DataAccessException;

    /**
     * Take (remove) an object from the space matching its id, the class and the routing value. Returns
     * <code>null</code> if there is no match within the specified timeout.
     *
     * @param clazz     The class of the entry
     * @param id        The id of the entry
     * @param routing   The routing value
     * @param timeout   The timeout value to wait for a matching entry if it does not exists within the space
     * @param modifiers one or a union of {@link TakeModifiers}.
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     * @since 9.0.1
     */
    <T> T takeById(Class<T> clazz, Object id, Object routing, long timeout, TakeModifiers modifiers) throws DataAccessException;

    /**
     * Take (remove) an object from the space matching the specified id query. Returns
     * <code>null</code> if there is no match.
     *
     * <p>The timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * <p>Note, if the space is partitioned, and the Entry has a specific property
     * for its routing value, the operation will broadcast to all partitions. The
     * {@link #readById(Class, Object, Object)} can be used to specify the routing.
     * 
     * @param query Query to search by.
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     * @since 8.0
     */
    <T> T takeById(IdQuery<T> query) throws DataAccessException;
    
    /**
     * Take (remove) an object from the space matching the specified id query. Returns
     * <code>null</code> if there is no match within the specified timeout.
     *
     * @param query Query to search by.
     * @param timeout The timeout value to wait for a matching entry if it does not exists within the space
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     * @since 8.0
     */
    <T> T takeById(IdQuery<T> query, long timeout) throws DataAccessException;
    
    /**
     * @deprecated since 9.0.1 - use {@link #takeById(IdQuery, long, TakeModifiers)} instead.
     */
    @Deprecated
    <T> T takeById(IdQuery<T> query, long timeout, int modifiers) throws DataAccessException;

    /**
     * Take (remove) an object from the space matching the specified id query. Returns
     * <code>null</code> if there is no match within the specified timeout.
     *
     * @param query Query to search by.
     * @param timeout The timeout value to wait for a matching entry if it does not exists within the space
     * @param modifiers one or a union of {@link TakeModifiers}.
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     * @since 9.0.1
     */
    <T> T takeById(IdQuery<T> query, long timeout, TakeModifiers modifiers) throws DataAccessException;

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
     * @deprecated since 9.0.1 - use {@link #take(Object, long, TakeModifiers)} instead.
     */
    @Deprecated
    <T> T take(T template, long timeout, int modifiers) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space, blocking until one exists.
     * Return <code>null</code> if the timeout expires.
     *
     * @param template  The template used for matching. Matching is done against
     *                  the template with <code>null</code> fields being wildcards (
     *                  "match anything") other fields being values ("match exactly
     *                  on the serialized form").
     * @param timeout   How long the client is willing to wait for a
     *                  transactionally proper matching entry. A timeout of
     *                  {@link net.jini.space.JavaSpace#NO_WAIT} means to wait no
     *                  time at all; this is equivalent to a wait of zero.
     * @param modifiers one or a union of {@link TakeModifiers}.
     * @return A removed entry from the space
     * @throws DataAccessException
     * @since 9.0.1
     */
    <T> T take(T template, long timeout, TakeModifiers modifiers) throws DataAccessException;

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
    <T> T take(ISpaceQuery<T> template) throws DataAccessException;

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
    <T> T take(ISpaceQuery<T> template, long timeout) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #take(ISpaceQuery, long, TakeModifiers)} instead.
     */
    @Deprecated
    <T> T take(ISpaceQuery<T> template, long timeout, int modifiers) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space, blocking until one exists.
     * Return <code>null</code> if the timeout expires.
     *
     * @param template  A query to be executed against the space. Most common one is
     *                  {@link com.j_spaces.core.client.SQLQuery}.
     * @param timeout   How long the client is willing to wait for a
     *                  transactionally proper matching entry. A timeout of
     *                  {@link net.jini.space.JavaSpace#NO_WAIT} means to wait no
     *                  time at all; this is equivalent to a wait of zero.
     * @param modifiers one or a union of {@link TakeModifiers}.
     * @return A removed entry from the space
     * @throws DataAccessException
     * @since 9.0.1
     */
    <T> T take(ISpaceQuery<T> template, long timeout, TakeModifiers modifiers) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
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
    <T> AsyncFuture<T> asyncTake(T template) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
     *
     * <p>Note, the timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * @param template The template used for matching. Matching is done against
     *                 the template with <code>null</code> fields being wildcards (
     *                 "match anything") other fields being values ("match exactly
     *                 on the serialized form").
     * @param listener A listener to be notified when a result arrives
     * @return A removed entry from the space
     * @throws DataAccessException
     */
    <T> AsyncFuture<T> asyncTake(T template, AsyncFutureListener<T> listener) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
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
    <T> AsyncFuture<T> asyncTake(T template, long timeout) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
     *
     * @param template The template used for matching. Matching is done against
     *                 the template with <code>null</code> fields being wildcards (
     *                 "match anything") other fields being values ("match exactly
     *                 on the serialized form").
     * @param timeout  How long the client is willing to wait for a
     *                 transactionally proper matching entry. A timeout of
     *                 {@link net.jini.space.JavaSpace#NO_WAIT} means to wait no
     *                 time at all; this is equivalent to a wait of zero.
     * @param listener A listener to be notified when a result arrives
     * @return A removed entry from the space
     * @throws DataAccessException
     */
    <T> AsyncFuture<T> asyncTake(T template, long timeout, AsyncFutureListener<T> listener) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #asyncTake(Object, long, TakeModifiers)} instead.
     */
    @Deprecated
    <T> AsyncFuture<T> asyncTake(T template, long timeout, int modifiers) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
     *
     * @param template  The template used for matching. Matching is done against
     *                  the template with <code>null</code> fields being wildcards (
     *                  "match anything") other fields being values ("match exactly
     *                  on the serialized form").
     * @param timeout   How long the client is willing to wait for a
     *                  transactionally proper matching entry. A timeout of
     *                  {@link net.jini.space.JavaSpace#NO_WAIT} means to wait no
     *                  time at all; this is equivalent to a wait of zero.
     * @param modifiers one or a union of {@link TakeModifiers}.
     * @return A removed entry from the space
     * @throws DataAccessException
     * @since 9.0.1
     */
    <T> AsyncFuture<T> asyncTake(T template, long timeout, TakeModifiers modifiers) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #asyncTake(Object, long, TakeModifiers, AsyncFutureListener)} instead.
     */
    @Deprecated
    <T> AsyncFuture<T> asyncTake(T template, long timeout, int modifiers, AsyncFutureListener<T> listener) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
     *
     * @param template  The template used for matching. Matching is done against
     *                  the template with <code>null</code> fields being wildcards (
     *                  "match anything") other fields being values ("match exactly
     *                  on the serialized form").
     * @param timeout   How long the client is willing to wait for a
     *                  transactionally proper matching entry. A timeout of
     *                  {@link net.jini.space.JavaSpace#NO_WAIT} means to wait no
     *                  time at all; this is equivalent to a wait of zero.
     * @param modifiers one or a union of {@link TakeModifiers}.
     * @param listener A listener to be notified when a result arrives
     * @return A removed entry from the space
     * @throws DataAccessException
     * @since 9.0.1
     */
    <T> AsyncFuture<T> asyncTake(T template, long timeout, TakeModifiers modifiers, AsyncFutureListener<T> listener) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
     *
     * <p>Note, the timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.
     * @return A removed entry from the space
     * @throws DataAccessException
     */
    <T> AsyncFuture<T> asyncTake(ISpaceQuery<T> template) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
     *
     * <p>Note, the timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.
     * @param listener A listener to be notified when a result arrives.
     * @return A removed entry from the space
     * @throws DataAccessException
     */
    <T> AsyncFuture<T> asyncTake(ISpaceQuery<T> template, AsyncFutureListener<T> listener) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
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
    <T> AsyncFuture<T> asyncTake(ISpaceQuery<T> template, long timeout) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.
     * @param timeout  How long the client is willing to wait for a
     *                 transactionally proper matching entry. A timeout of
     *                 {@link net.jini.space.JavaSpace#NO_WAIT} means to wait no
     *                 time at all; this is equivalent to a wait of zero.
     * @param listener A listener to be notified when a result arrives.
     * @return A removed entry from the space
     * @throws DataAccessException
     */
    <T> AsyncFuture<T> asyncTake(ISpaceQuery<T> template, long timeout, AsyncFutureListener<T> listener) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #asyncTake(ISpaceQuery, long, TakeModifiers)} instead.
     */
    @Deprecated
    <T> AsyncFuture<T> asyncTake(ISpaceQuery<T> template, long timeout, int modifiers) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
     *
     * @param template  A query to be executed against the space. Most common one is
     *                  {@link com.j_spaces.core.client.SQLQuery}.
     * @param timeout   How long the client is willing to wait for a
     *                  transactionally proper matching entry. A timeout of
     *                  {@link net.jini.space.JavaSpace#NO_WAIT} means to wait no
     *                  time at all; this is equivalent to a wait of zero.
     * @param modifiers one or a union of {@link TakeModifiers}.
     * @return A removed entry from the space
     * @throws DataAccessException
     * @since 9.0.1
     */
    <T> AsyncFuture<T> asyncTake(ISpaceQuery<T> template, long timeout, TakeModifiers modifiers) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #asyncTake(ISpaceQuery, long, TakeModifiers, AsyncFutureListener)} instead.
     */
    @Deprecated
    <T> AsyncFuture<T> asyncTake(ISpaceQuery<T> template, long timeout, int modifiers, AsyncFutureListener<T> listener) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space in an asynchronous manner. Returns
     * immediately with a future. The future can then be used to check if there is a
     * match or not. Once a match is found or the timeout expires, the future will
     * return a result (<code>null</code> in case there was no match).
     *
     * @param template  A query to be executed against the space. Most common one is
     *                  {@link com.j_spaces.core.client.SQLQuery}.
     * @param timeout   How long the client is willing to wait for a
     *                  transactionally proper matching entry. A timeout of
     *                  {@link net.jini.space.JavaSpace#NO_WAIT} means to wait no
     *                  time at all; this is equivalent to a wait of zero.
     * @param modifiers one or a union of {@link TakeModifiers}.
     * @param listener  A listener to be notified when a result arrives.
     * @return A removed entry from the space
     * @throws DataAccessException
     * @since 9.0.1
     */
    <T> AsyncFuture<T> asyncTake(ISpaceQuery<T> template, long timeout, TakeModifiers modifiers, AsyncFutureListener<T> listener) throws DataAccessException;

    /**
     * Take (remove) an object from the space matching its id and the class. Returns
     * <code>null</code> if there is no match.
     *
     * <p>Matching and timeouts are done as in <code>takeById</code>, except that blocking in this
     * call is done only if necessary to wait for transactional state to settle.
     *
     * <p>The timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * <p>Note, if the space is partitioned, and the Entry has a specific property
     * for its routing value, the operation will broadcast to all partitions. The
     * {@link #takeById(Class, Object, Object)} can be used to specify the routing.
     *
     * @param clazz The class of the entry
     * @param id    The id of the entry
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     */
    <T> T takeIfExistsById(Class<T> clazz, Object id) throws DataAccessException;

    /**
     * Take (remove) an object from the space matching its id, the class and the routing value. Returns
     * <code>null</code> if there is no match.
     *
     * <p>Matching and timeouts are done as in <code>takeById</code>, except that blocking in this
     * call is done only if necessary to wait for transactional state to settle.
     *
     * <p>The timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * @param clazz   The class of the entry
     * @param id      The id of the entry
     * @param routing The routing value
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     */
    <T> T takeIfExistsById(Class<T> clazz, Object id, Object routing) throws DataAccessException;

    /**
     * Take (remove) an object from the space matching its id, the class and the routing value. Returns
     * <code>null</code> if there is no match within the specified timeout.
     *
     * <p>Matching and timeouts are done as in <code>takeById</code>, except that blocking in this
     * call is done only if necessary to wait for transactional state to settle.
     *
     * @param clazz   The class of the entry
     * @param id      The id of the entry
     * @param routing The routing value
     * @param timeout The timeout value to wait for a matching entry if it does not exists within the space
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     */
    <T> T takeIfExistsById(Class<T> clazz, Object id, Object routing, long timeout) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #takeIfExistsById(Class, Object, Object, long, TakeModifiers)} instead.
     */
    @Deprecated
    <T> T takeIfExistsById(Class<T> clazz, Object id, Object routing, long timeout, int modifiers) throws DataAccessException;

    /**
     * Take (remove) an object from the space matching its id, the class and the routing value. Returns
     * <code>null</code> if there is no match within the specified timeout.
     *
     * <p>Matching and timeouts are done as in <code>takeById</code>, except that blocking in this
     * call is done only if necessary to wait for transactional state to settle.
     *
     * @param clazz     The class of the entry
     * @param id        The id of the entry
     * @param routing   The routing value
     * @param timeout   The timeout value to wait for a matching entry if it does not exists within the space
     * @param modifiers one or a union of {@link TakeModifiers}.
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     * @since 9.0.1
     */
    <T> T takeIfExistsById(Class<T> clazz, Object id, Object routing, long timeout, TakeModifiers modifiers) throws DataAccessException;

    /**
     * Take (remove) an object from the space matching the specified id query. Returns
     * <code>null</code> if there is no match.
     *
     * <p>The timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * <p>Note, if the space is partitioned, and the Entry has a specific property
     * for its routing value, the operation will broadcast to all partitions. The
     * {@link #readById(Class, Object, Object)} can be used to specify the routing.
     *
     * <p>Matching and timeouts are done as in
     * <code>readById</code>, except that blocking in this call is done only if
     * necessary to wait for transactional state to settle.
     * 
     * @param query Query to search by.
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     * @since 8.0
     */
    <T> T takeIfExistsById(IdQuery<T> query) throws DataAccessException;

    /**
     * Take (remove) an object from the space matching the specified id query. Returns
     * <code>null</code> if there is no match within the specified timeout.
     *
     * <p>Matching and timeouts are done as in
     * <code>readById</code>, except that blocking in this call is done only if
     * necessary to wait for transactional state to settle.
     *
     * @param query Query to search by.
     * @param timeout The timeout value to wait for a matching entry if it does not exists within the space
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     * @since 8.0
     */
    <T> T takeIfExistsById(IdQuery<T> query, long timeout) throws DataAccessException;
    
    /**
     * @deprecated since 9.0.1 - use {@link #takeIfExistsById(IdQuery, long, TakeModifiers)} instead.
     */
    @Deprecated
    <T> T takeIfExistsById(IdQuery<T> query, long timeout, int modifiers) throws DataAccessException;

    /**
     * Take (remove) an object from the space matching the specified id query. Returns
     * <code>null</code> if there is no match within the specified timeout.
     *
     * <p>Matching and timeouts are done as in
     * <code>readById</code>, except that blocking in this call is done only if
     * necessary to wait for transactional state to settle.
     *
     * @param query Query to search by.
     * @param timeout The timeout value to wait for a matching entry if it does not exists within the space
     * @param modifiers one or a union of {@link TakeModifiers}.
     * @return A matching object, or <code>null</code> if no matching is found within the timeout value.
     * @since 9.0.1
     */
    <T> T takeIfExistsById(IdQuery<T> query, long timeout, TakeModifiers modifiers) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space, returning <code>null</code> if
     * there currently is none. Matching and timeouts are done as in <code>take</code>, 
     * except that blocking in this call is done only if necessary to wait for transactional 
     * state to settle.
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
     * Take (remove) any matching entry from the space, returning <code>null</code> if
     * there currently is none. Matching and timeouts are done as in <code>take</code>,
     * except that blocking in this call is done only if necessary to wait for transactional
     * state to settle.
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
     * @deprecated since 9.0.1 - use {@link #takeIfExists(Object, long, TakeModifiers)} instead.
     */
    @Deprecated
    <T> T takeIfExists(T template, long timeout, int modifiers) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space, returning <code>null</code> if
     * there currently is none. Matching and timeouts are done as in <code>take</code>,
     * except that blocking in this call is done only if necessary to wait for transactional
     * state to settle.
     *
     * @param template The template used for matching. Matching is done against
     *                 the template with <code>null</code> fields being wildcards (
     *                 "match anything") other fields being values ("match exactly
     *                 on the serialized form").
     * @param timeout  How long the client is willing to wait for a
     *                 transactionally proper matching entry. A timeout of
     *                 {@link net.jini.space.JavaSpace#NO_WAIT} means to wait no
     *                 time at all; this is equivalent to a wait of zero.
     * @param modifiers one or a union of {@link TakeModifiers}.
     * @return A removed entry from the space
     * @throws DataAccessException
     * @since 9.0.1
     */
    <T> T takeIfExists(T template, long timeout, TakeModifiers modifiers) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space, returning <code>null</code> if
     * there currently is none. Matching and timeouts are done as in <code>take</code>,
     * except that blocking in this call is done only if necessary to wait for transactional
     * state to settle.
     *
     * <p>Note, the timeout is the default timeout this interface is configured with
     * (using its factory) and defaults to {@link net.jini.space.JavaSpace#NO_WAIT}.
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.
     * @return A removed entry from the space
     * @throws DataAccessException
     */
    <T> T takeIfExists(ISpaceQuery<T> template) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space, returning <code>null</code> if
     * there currently is none. Matching and timeouts are done as in <code>take</code>,
     * except that blocking in this call is done only if necessary to wait for transactional
     * state to settle.
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
    <T> T takeIfExists(ISpaceQuery<T> template, long timeout) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #takeIfExists(ISpaceQuery, long, TakeModifiers)} instead.
     */
    @Deprecated
    <T> T takeIfExists(ISpaceQuery<T> template, long timeout, int modifiers) throws DataAccessException;

    /**
     * Take (remove) any matching entry from the space, returning <code>null</code> if
     * there currently is none. Matching and timeouts are done as in <code>take</code>,
     * except that blocking in this call is done only if necessary to wait for transactional
     * state to settle.
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.
     * @param timeout  How long the client is willing to wait for a
     *                 transactionally proper matching entry. A timeout of
     *                 {@link net.jini.space.JavaSpace#NO_WAIT} means to wait no
     *                 time at all; this is equivalent to a wait of zero.
     * @param modifiers one or a union of {@link TakeModifiers}.
     * @return A removed entry from the space
     * @throws DataAccessException
     * @since 9.0.1
     */
    <T> T takeIfExists(ISpaceQuery<T> template, long timeout, TakeModifiers modifiers) throws DataAccessException;

    /**
     * Takes (removes) all the entries matching the specified template from this
     * space.
     * Same as calling {@link #takeMultiple(Object, int) takeMultiple(template, Integer.MAX_VALUE)}.
     *
     * @param template   The template used for matching. Matching is done against
     *                   the template with <code>null</code> fields being.
     *                   wildcards ("match anything") other fields being values ("match
     *                   exactly on the serialized form"). The template can also be one
     *                   of the different {@link com.gigaspaces.query.ISpaceQuery} classes
     * @return Removed matched entries from the space
     * @throws DataAccessException In the event of a take error, DataAccessException will
     *         wrap a TakeMultipleException, accessible via DataAccessException.getRootCause().
     */
    <T> T[] takeMultiple(T template) throws DataAccessException;
        
    /**
     * Takes (removes) all the entries matching the specified template from this
     * space.
     *
     * @param template   The template used for matching. Matching is done against
     *                   the template with <code>null</code> fields being.
     *                   wildcards ("match anything") other fields being values ("match
     *                   exactly on the serialized form"). The template can also be one
     *                   of the different {@link com.gigaspaces.query.ISpaceQuery} classes
     * @param maxEntries A limit on the number of entries to be returned. Use
     *                   {@link Integer#MAX_VALUE} for the uppermost limit.
     * @return Removed matched entries from the space
     * @throws DataAccessException In the event of a take error, DataAccessException will
     *         wrap a TakeMultipleException, accessible via DataAccessException.getRootCause().
     */
    <T> T[] takeMultiple(T template, int maxEntries) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #takeMultiple(Object, int, TakeModifiers)} instead.
     */
    @Deprecated
    <T> T[] takeMultiple(T template, int maxEntries, int modifiers) throws DataAccessException;

    /**
     * Takes (removes) all the entries matching the specified template from this
     * space.
     *
     * 
     * @param template   The template used for matching. Matching is done against
     *                   the template with <code>null</code> fields being.
     *                   wildcards ("match anything") other fields being values ("match
     *                   exactly on the serialized form"). The template can also be one
     *                   of the different {@link com.gigaspaces.query.ISpaceQuery} classes
     * @param maxEntries A limit on the number of entries to be returned. Use
     *                   {@link Integer#MAX_VALUE} for the uppermost limit.
     * @param modifiers one or a union of {@link TakeModifiers}.
     * @return Removed matched entries from the space
     * @throws DataAccessException In the event of a take error, DataAccessException will
     *         wrap a TakeMultipleException, accessible via DataAccessException.getRootCause().
     * @since 9.0.1
     */
    <T> T[] takeMultiple(T template, int maxEntries, TakeModifiers modifiers) throws DataAccessException;

    /**
     * Takes (removes) all the entries matching the specified template from this
     * space.
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.  
     *                 
     * @return Removed matched entries from the space
     * @throws DataAccessException In the event of a take error, DataAccessException will
     *         wrap a TakeMultipleException, accessible via DataAccessException.getRootCause().
     */
    <T> T[] takeMultiple(ISpaceQuery<T> template) throws DataAccessException;
    
    /**
     * Takes (removes) all the entries matching the specified template from this
     * space.
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.  
     * @param maxEntries A limit on the number of entries to be returned. Use
     *                   {@link Integer#MAX_VALUE} for the uppermost limit.
     * @return Removed matched entries from the space
     * @throws DataAccessException In the event of a take error, DataAccessException will
     *         wrap a TakeMultipleException, accessible via DataAccessException.getRootCause().
     */
    <T> T[] takeMultiple(ISpaceQuery<T> template, int maxEntries) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #takeMultiple(ISpaceQuery, int, TakeModifiers)} instead.
     */
    @Deprecated
    <T> T[] takeMultiple(ISpaceQuery<T> template, int maxEntries, int modifiers) throws DataAccessException;

    /**
     * Takes (removes) all the entries matching the specified template from this
     * space.
     *
     * @param template A query to be executed against the space. Most common one is
     *                 {@link com.j_spaces.core.client.SQLQuery}.  
     * @param maxEntries A limit on the number of entries to be returned. Use
     *                   {@link Integer#MAX_VALUE} for the uppermost limit.
     * @param modifiers one or a union of {@link TakeModifiers}.
     * @return Removed matched entries from the space
     * @throws DataAccessException In the event of a take error, DataAccessException will
     *         wrap a TakeMultipleException, accessible via DataAccessException.getRootCause().
     * @since 9.0.1
     */
    <T> T[] takeMultiple(ISpaceQuery<T> template, int maxEntries, TakeModifiers modifiers) throws DataAccessException;

    /**
     * Writes a new object to the space, returning its {@link com.j_spaces.core.LeaseContext}.
     *
     * <p>By default uses the {@link WriteModifiers#UPDATE_OR_WRITE}
     * modifier (see {@link #write(Object,long,long,int)}. In order to force the operation
     * to perform "write" only (a bit more performant), the {@link WriteModifiers#WRITE_ONLY}
     * modifier can be used resulting in an {@link org.openspaces.core.EntryAlreadyInSpaceException}
     * if the entry already exists in the space (it must have an id for this exception to be raised).
     *
     * <p>If the object has a primary key that isn't auto - generated, and the modifiers value is one of
     * {@link WriteModifiers#UPDATE_OR_WRITE},
     * {@link WriteModifiers#UPDATE_ONLY} or
     * {@link WriteModifiers#PARTIAL_UPDATE}, it will call to the update method,
     * returning a LeaseContext holder. This lease is unknown to the grantor, unless an actual 'write' was performed.
     *
     * <p>The entry will be written using the default lease this interface is configured with
     * (using the its factory). In order to explicitly define the lease, please use
     * {@link #write(Object,long)}.
     *
     * @param entry The entry to write to the space
     * @return A usable <code>Lease</code> on a successful write, or <code>null</code> if performed
     *         with the proxy configured with NoWriteLease flag.
     *         <p>when {@link WriteModifiers#UPDATE_OR_WRITE} modifier is applied,
     *         {@link LeaseContext#getObject()} returns <code>null</code> on a successful write
     *         or the previous value - on successful update.  {@link org.openspaces.core.UpdateOperationTimeoutException}
     *         is thrown if timeout occurred while trying to update the object.
     * @throws DataAccessException
     */
    <T> LeaseContext<T> write(T entry) throws DataAccessException;

    /**
     * Writes a new object to the space, returning its {@link com.j_spaces.core.LeaseContext}.
     *
     * <p>By default uses the {@link WriteModifiers#UPDATE_OR_WRITE}
     * modifier (see {@link #write(Object,long,long,int)}. In order to force the operation
     * to perform "write" only (a bit more performant), the {@link WriteModifiers#WRITE_ONLY}
     * modifier can be used resulting in an {@link org.openspaces.core.EntryAlreadyInSpaceException}
     * if the entry already exists in the space (it must have an id for this exception to be raised).
     *
     * <p>If the object has a primary key that isn't auto - generated, and the modifiers value is one of
     * {@link WriteModifiers#UPDATE_OR_WRITE},
     * {@link WriteModifiers#UPDATE_ONLY} or
     * {@link WriteModifiers#PARTIAL_UPDATE}, it will call to the update method,
     * returning a LeaseContext holder. This lease is unknown to the grantor, unless an actual 'write' was performed.
     *
     * @param entry The entry to write to the space
     * @param lease The lease the entry will be written with, in <b>milliseconds</b>.
     * @return A usable <code>Lease</code> on a successful write, or <code>null</code> if performed
     *         with the proxy configured with NoWriteLease flag.
     *         <p>when {@link WriteModifiers#UPDATE_OR_WRITE} modifier is applied,
     *         {@link LeaseContext#getObject()} returns <code>null</code> on a successful write
     *         or the previous value - on successful update.  {@link org.openspaces.core.UpdateOperationTimeoutException}
     *         is thrown if timeout occurred while trying to update the object.
     * @throws DataAccessException
     */
    <T> LeaseContext<T> write(T entry, long lease) throws DataAccessException;

    /**
     * Writes a new object to the space, returning its {@link com.j_spaces.core.LeaseContext}.
     *
     * <p>By default uses the {@link WriteModifiers#UPDATE_OR_WRITE}
     * modifier (see {@link #write(Object,long,long,int)}. In order to force the operation
     * to perform "write" only (a bit more performant), the {@link WriteModifiers#WRITE_ONLY}
     * modifier can be used resulting in an {@link org.openspaces.core.EntryAlreadyInSpaceException}
     * if the entry already exists in the space (it must have an id for this exception to be raised).
     *
     * <p>If the object has a primary key that isn't auto - generated, and the modifiers value is one of
     * {@link WriteModifiers#UPDATE_OR_WRITE},
     * {@link WriteModifiers#UPDATE_ONLY} or
     * {@link WriteModifiers#PARTIAL_UPDATE}, it will call to the update method,
     * returning a LeaseContext holder. This lease is unknown to the grantor, unless an actual 'write' was performed.
     *
     * <p>The entry will be written using the default lease this interface is configured with
     * (using the its factory). In order to explicitly define the lease, please use
     * {@link #write(Object,long)}.
     *
     * @param entry The entry to write to the space
     * @param modifiers one or a union of {@link WriteModifiers}.
     * @return A usable <code>Lease</code> on a successful write, or <code>null</code> if performed
     *         with the proxy configured with NoWriteLease flag.
     *         <p>when {@link WriteModifiers#UPDATE_OR_WRITE} modifier is applied,
     *         {@link LeaseContext#getObject()} returns <code>null</code> on a successful write
     *         or the previous value - on successful update.  {@link org.openspaces.core.UpdateOperationTimeoutException}
     *         is thrown if timeout occurred while trying to update the object.
     * @throws DataAccessException
     * @since 9.0.1
     */
    <T> LeaseContext<T> write(T entry, WriteModifiers modifiers) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #write(Object, long, long, WriteModifiers)} instead.
     */
    @Deprecated
    <T> LeaseContext<T> write(T entry, long lease, long timeout, int modifiers) throws DataAccessException;

    /**
     * Writes a new object to the space, returning its {@link com.j_spaces.core.LeaseContext}.
     *
     * <p>By default uses the {@link com.gigaspaces.client.WriteModifiers#UPDATE_OR_WRITE} modifier. 
     * In order to force the operation to perform "write" only (a bit more performant), the 
     * {@link com.gigaspaces.client.WriteModifiers#WRITE_ONLY} modifier can be used resulting in an 
     * {@link org.openspaces.core.EntryAlreadyInSpaceException} if the entry already exists in the space 
     * (it must have an id for this exception to be raised).
     *
     * <p>If the object has a primary key that isn't auto - generated, and the modifiers value is one of
     * {@link WriteModifiers#UPDATE_OR_WRITE},
     * {@link WriteModifiers#UPDATE_ONLY} or
     * {@link WriteModifiers#PARTIAL_UPDATE}, it will call to the update method,
     * returning a LeaseContext holder. This lease is unknown to the grantor, unless an actual 'write' was performed.
     *
     * @param entry The entry to write to the space
     * @param lease The lease the entry will be written with, in <b>milliseconds</b>.
     * @param timeout The timeout of an update operation, in <b>milliseconds</b>. If the entry is locked by another transaction
     * wait for the specified number of milliseconds for it to be released.
     * @param modifiers one or a union of {@link WriteModifiers}.
     * @return A usable <code>Lease</code> on a successful write, or <code>null</code> if performed
     *         with the proxy configured with NoWriteLease flag.
     *         <p>when {@link WriteModifiers#UPDATE_OR_WRITE} modifier is applied,
     *         {@link LeaseContext#getObject()} returns <code>null</code> on a successful write
     *         or the previous value - on successful update.  {@link org.openspaces.core.UpdateOperationTimeoutException}
     *         is thrown if timeout occurred while trying to update the object.
     * @throws DataAccessException
     * @since 9.0.1
     */
    <T> LeaseContext<T> write(T entry, long lease, long timeout, WriteModifiers modifiers) throws DataAccessException;

    /**
     * Writes the specified entries to this space.
     *
     * <p>The entry will be written using the default lease this interface is configured with
     * (using the its factory). In order to explicitly define the lease, please use
     * {@link #writeMultiple(Object[],long)}.
     *
     * @param entries The entries to write to the space.
     * @return Leases for the written entries
     * @throws DataAccessException In the event of a write error, DataAccessException will
     *         wrap a WriteMultipleException, accessible via DataAccessException.getRootCause().
     */
    <T> LeaseContext<T>[] writeMultiple(T[] entries) throws DataAccessException;

    /**
     * Writes the specified entries to this space.
     * 
     * @param entries The entries to write to the space.
     * @param lease   The lease the entry will be written with, in <b>milliseconds</b>.
     * @return Leases for the written entries
     * @throws DataAccessException In the event of a write error, DataAccessException will
     *         wrap a WriteMultipleException, accessible via DataAccessException.getRootCause().
     */
    <T> LeaseContext<T>[] writeMultiple(T[] entries, long lease) throws DataAccessException;

    /**
     * Writes the specified entries to this space.
     * 
     * @param entries The entries to write to the space.
     * @param modifiers one or a union of {@link WriteModifiers}.
     * @return Leases for the written entries
     * @throws DataAccessException In the event of a write error, DataAccessException will
     *         wrap a WriteMultipleException, accessible via DataAccessException.getRootCause().
     * @since 9.0.1
     */
    <T> LeaseContext<T>[] writeMultiple(T[] entries, WriteModifiers modifiers) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #writeMultiple(Object[], long, WriteModifiers)} instead.
     */
    @Deprecated
    <T> LeaseContext<T>[] writeMultiple(T[] entries, long lease, int updateModifiers) throws DataAccessException;

    /**
     * Writes the specified entries to this space.
     * 
     * Same as a single write but for a group of entities sharing the same transaction (if any),
     * applied with the same specified operation modifier. 
     * The semantics of a single write and a batch update are similar - the return 
     * value for each corresponds to it's cell in the returned array.
     * see <code>'returns'</code> for possible return values.
     * 
     * @param entries           the entries to write. 
     * @param lease             the requested lease time, in milliseconds
     * @param modifiers one or a union of {@link WriteModifiers}.
     *         
     * @return array in which each cell is corresponding to the written entry at the same index in the entries array, 
     *         each cell is a usable <code>Lease</code> on a successful write, or <code>null</code> if performed with NoWriteLease attribute.
     *         <p>when {@link WriteModifiers#UPDATE_OR_WRITE} modifier is applied,
     *         <ul>
     *         <li>{@link LeaseContext#getObject()} returns:
     *         <ul>
     *         <li>null - on a successful write
     *         <li>previous value - on successful update
     *         </ul>
     *         <li>or, OperationTimeoutException - thrown if timeout occurred
     *         </ul>
     * @throws DataAccessException In the event of a write error, DataAccessException will
     *         wrap a WriteMultipleException, accessible via DataAccessException.getRootCause().
     * @since 9.0.1
     */
    <T> LeaseContext<T>[] writeMultiple(T[] entries, long lease, WriteModifiers modifiers) throws DataAccessException;

    /**
     * @deprecated since 9.0.1 - use {@link #writeMultiple(Object[], long[], WriteModifiers)} instead.
     */
    @Deprecated
    <T> LeaseContext<T>[] writeMultiple(T[] entries, long[] leases, int updateModifiers) throws DataAccessException;

    /**
     * Writes the specified entries to this space.
     * 
     * Same as a single write but for a group of entities sharing the same transaction (if any),
     * applied with the same specified operation modifier. 
     * The semantics of a single write and a batch update are similar - the return 
     * value for each corresponds to it's cell in the returned array.
     * see <code>'returns'</code> for possible return values.
     * 
     * @param entries           the entries to write. 
     * @param leases            the requested lease time per entry, in milliseconds
     * @param modifiers one or a union of {@link WriteModifiers}.
     *         
     * @return array in which each cell is corresponding to the written entry at the same index in the entries array, 
     *         each cell is a usable <code>Lease</code> on a successful write, or <code>null</code> if performed with NoWriteLease attribute.
     *         <p>when {@link WriteModifiers#UPDATE_OR_WRITE} modifier is applied,
     *         <ul>
     *         <li>{@link LeaseContext#getObject()} returns:
     *         <ul>
     *         <li>null - on a successful write
     *         <li>previous value - on successful update
     *         </ul>
     *         <li>or, OperationTimeoutException - thrown if timeout occurred
     *         </ul>
     * @throws DataAccessException In the event of a write error, DataAccessException will
     *         wrap a WriteMultipleException, accessible via DataAccessException.getRootCause().
     * @since 9.0.1
     */    
    <T> LeaseContext<T>[] writeMultiple(T[] entries, long[] leases, WriteModifiers modifiers) throws DataAccessException;

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
     * <p>The space that the task is executed within can be accessible by marking a field with type {@link org.openspaces.core.GigaSpace}
     * using the {@link org.openspaces.core.executor.TaskGigaSpace} annotation. Another option is by implementing
     * the {@link org.openspaces.core.executor.TaskGigaSpaceAware} interface.
     *
     * <p>Resource injection can be enabled by marking the task with {@link org.openspaces.core.executor.AutowireTask}
     * or with {@link org.openspaces.core.executor.AutowireTaskMarker}. Resources defined within processing unit
     * (space node) the task is executed on are accessible by using either the {@link org.springframework.beans.factory.annotation.Autowired} or
     * <tt>javax.annotation.Resource</tt> annotations (assuming they are enabled using <code>context:annotation-config</code>).
     * Bean life cycle methods, such as {@link org.openspaces.core.cluster.ClusterInfoAware} and
     * {@link org.springframework.context.ApplicationContextAware} are also available.
     *
     * @param task The task to execute
     * @return a Future representing pending completion of the task,
     *         and whose <code>get()</code> method will return the task value upon completion.
     */
    <T extends Serializable> AsyncFuture<T> execute(Task<T> task);

    /**
     * Executes a task on a specific space node. The space node it will
     * execute on should be controlled by having a method that return the routing value
     * annotated with {@link com.gigaspaces.annotation.pojo.SpaceRouting}.
     *
     * <p>In order to control the routing externally, use {@link #execute(org.openspaces.core.executor.Task, Object)}.
     *
     * <p>The space that the task is executed within can be accessible by marking a field with type {@link org.openspaces.core.GigaSpace}
     * using the {@link org.openspaces.core.executor.TaskGigaSpace} annotation. Another option is by implementing
     * the {@link org.openspaces.core.executor.TaskGigaSpaceAware} interface.
     *
     * <p>Resource injection can be enabled by marking the task with {@link org.openspaces.core.executor.AutowireTask}
     * or with {@link org.openspaces.core.executor.AutowireTaskMarker}. Resources defined within processing unit
     * (space node) the task is executed on are accessible by using either the {@link org.springframework.beans.factory.annotation.Autowired} or
     * <tt>javax.annotation.Resource</tt> annotations (assuming they are enabled using <code>context:annotation-config</code>).
     * Bean life cycle methods, such as {@link org.openspaces.core.cluster.ClusterInfoAware} and
     * {@link org.springframework.context.ApplicationContextAware} are also available.
     *
     * @param task     The task to execute
     * @param listener A listener to be notified when execution completes
     * @return a Future representing pending completion of the task,
     *         and whose <code>get()</code> method will return the task value upon completion.
     */
    <T extends Serializable> AsyncFuture<T> execute(Task<T> task, AsyncFutureListener<T> listener);

    /**
     * Executes a task on a specific space node. The space node it will
     * execute on should is controlled by the routing value provided as a second parameter.
     *
     * <p>The routing object itself does not have to be the actual routing value, but can be a POJO
     * that defined a method annotated with <code>@SpaceRouting</code> annotation (this works well
     * when wanting to use entries as the routing parameters).
     *
     * <p>In order to control the using the Task itself, use {@link #execute(org.openspaces.core.executor.Task)}.
     *
     * <p>The space that the task is executed within can be accessible by marking a field with type {@link org.openspaces.core.GigaSpace}
     * using the {@link org.openspaces.core.executor.TaskGigaSpace} annotation. Another option is by implementing
     * the {@link org.openspaces.core.executor.TaskGigaSpaceAware} interface.
     *
     * <p>Resource injection can be enabled by marking the task with {@link org.openspaces.core.executor.AutowireTask}
     * or with {@link org.openspaces.core.executor.AutowireTaskMarker}. Resources defined within processing unit
     * (space node) the task is executed on are accessible by using either the {@link org.springframework.beans.factory.annotation.Autowired} or
     * <tt>javax.annotation.Resource</tt> annotations (assuming they are enabled using <code>context:annotation-config</code>).
     * Bean life cycle methods, such as {@link org.openspaces.core.cluster.ClusterInfoAware} and
     * {@link org.springframework.context.ApplicationContextAware} are also available.
     *
     * @param task    The task to execute
     * @param routing The routing value that will control on which node the task will be executed on
     * @return a Future representing pending completion of the task,
     *         and whose <code>get()</code> method will return the task value upon completion.
     */
    <T extends Serializable> AsyncFuture<T> execute(Task<T> task, Object routing);

    /**
     * Executes a task on a specific space node. The space node it will
     * execute on should is controlled by the routing value provided as a second parameter.
     *
     * <p>The routing object itself does not have to be the actual routing value, but can be a POJO
     * that defined a method annotated with <code>@SpaceRouting</code> annotation (this works well
     * when wanting to use entries as the routing parameters).
     *
     * <p>In order to control the using the Task itself, use {@link #execute(org.openspaces.core.executor.Task)}.
     *
     * <p>The space that the task is executed within can be accessible by marking a field with type {@link org.openspaces.core.GigaSpace}
     * using the {@link org.openspaces.core.executor.TaskGigaSpace} annotation. Another option is by implementing
     * the {@link org.openspaces.core.executor.TaskGigaSpaceAware} interface.
     *
     * <p>Resource injection can be enabled by marking the task with {@link org.openspaces.core.executor.AutowireTask}
     * or with {@link org.openspaces.core.executor.AutowireTaskMarker}. Resources defined within processing unit
     * (space node) the task is executed on are accessible by using either the {@link org.springframework.beans.factory.annotation.Autowired} or
     * <tt>javax.annotation.Resource</tt> annotations (assuming they are enabled using <code>context:annotation-config</code>).
     * Bean life cycle methods, such as {@link org.openspaces.core.cluster.ClusterInfoAware} and
     * {@link org.springframework.context.ApplicationContextAware} are also available.
     *
     * @param task     The task to execute
     * @param routing  The routing value that will control on which node the task will be executed on
     * @param listener A listener to be notified when execution completes
     * @return a Future representing pending completion of the task,
     *         and whose <code>get()</code> method will return the task value upon completion.
     */
    <T extends Serializable> AsyncFuture<T> execute(Task<T> task, Object routing, AsyncFutureListener<T> listener);

    /**
     * Executes a task on all the nodes that correspond to the list of routing values. The task is executed
     * on each space node with all the results reduced by the
     * {@link org.openspaces.core.executor.DistributedTask#reduce(java.util.List)} operation.
     *
     * <p>The routing object itself does not have to be the actual routing value, but can be a POJO
     * that defined a method annotated with <code>@SpaceRouting</code> annotation (this works well
     * when wanting to use entries as the routing parameters).
     *
     * <p>The task can optionally implement {@link com.gigaspaces.async.AsyncResultFilter} that can control
     * if tasks should continue to accumulate or it should break and execute the reduce operation on the
     * results received so far.
     *
     * <p>The future actual result will be the reduced result of the execution, or the exception thrown during
     * during the reduce operation. The moderator can be used as a mechanism to listen for results as they arrive.
     *
     * <p>The last parameter can be of type {@link com.gigaspaces.async.AsyncFutureListener} which, this case,
     * it will be used to register a listener to be notified of the result.
     *
     * <p>The space that the task is executed within can be accessible by marking a field with type {@link org.openspaces.core.GigaSpace}
     * using the {@link org.openspaces.core.executor.TaskGigaSpace} annotation. Another option is by implementing
     * the {@link org.openspaces.core.executor.TaskGigaSpaceAware} interface.
     *
     * <p>Resource injection can be enabled by marking the task with {@link org.openspaces.core.executor.AutowireTask}
     * or with {@link org.openspaces.core.executor.AutowireTaskMarker}. Resources defined within processing unit
     * (space node) the task is executed on are accessible by using either the {@link org.springframework.beans.factory.annotation.Autowired} or
     * <tt>javax.annotation.Resource</tt> annotations (assuming they are enabled using <code>context:annotation-config</code>).
     * Bean life cycle methods, such as {@link org.openspaces.core.cluster.ClusterInfoAware} and
     * {@link org.springframework.context.ApplicationContextAware} are also available.
     *
     * @param task    The task to execute
     * @param routing A list of routing values, each resulting in an execution of the task on the space node
     *                it corresponds to
     * @return a Future representing pending completion of the task,
     *         and whose <code>get()</code> method will return the task value upon completion.
     */
    <T extends Serializable, R> AsyncFuture<R> execute(DistributedTask<T, R> task, Object... routing);

    /**
     * Executes the task on all the primary space nodes within the cluster (broadcast). The task is executed
     * on each space node with all the results reduced by the
     * {@link org.openspaces.core.executor.DistributedTask#reduce(java.util.List)} operation.
     *
     * <p>The task can optionally implement {@link com.gigaspaces.async.AsyncResultFilter} that can control
     * if tasks should continue to accumulate or it should break and execute the reduce operation on the
     * results received so far.
     *
     * <p>The future actual result will be the reduced result of the execution, or the exception thrown during
     * during the reduce operation. The moderator can be used as a mechanism to listen for results as they arrive.
     *
     * <p>The space that the task is executed within can be accessible by marking a field with type {@link org.openspaces.core.GigaSpace}
     * using the {@link org.openspaces.core.executor.TaskGigaSpace} annotation. Another option is by implementing
     * the {@link org.openspaces.core.executor.TaskGigaSpaceAware} interface.
     *
     * <p>Resource injection can be enabled by marking the task with {@link org.openspaces.core.executor.AutowireTask}
     * or with {@link org.openspaces.core.executor.AutowireTaskMarker}. Resources defined within processing unit
     * (space node) the task is executed on are accessible by using either the {@link org.springframework.beans.factory.annotation.Autowired} or
     * <tt>javax.annotation.Resource</tt> annotations (assuming they are enabled using <code>context:annotation-config</code>).
     * Bean life cycle methods, such as {@link org.openspaces.core.cluster.ClusterInfoAware} and
     * {@link org.springframework.context.ApplicationContextAware} are also available.
     *
     * @param task The task to execute
     * @return a Future representing pending completion of the task,
     *         and whose <code>get()</code> method will return the task value upon completion.
     */
    <T extends Serializable, R> AsyncFuture<R> execute(DistributedTask<T, R> task);

    //    /**
    //     * Executes the task on all the primary space nodes within the cluster (broadcast). The task is executed
    //     * on each space node with all the results reduced by the
    //     * {@link org.openspaces.core.executor.DistributedTask#reduce(java.util.List)} operation.
    //     *
    //     * <p>The task can optionally implement {@link com.gigaspaces.async.AsyncResultFilter} that can control
    //     * if tasks should continue to accumulate or it should break and execute the reduce operation on the
    //     * results received so far.
    //     *
    //     * <p>The future actual result will be the reduced result of the execution, or the exception thrown during
    //     * during the reduce operation. The moderator can be used as a mechanism to listen for results as they arrive.
    //     *
    //    * <p>The space that the task is executed within can be accessible by marking a field with type {@link org.openspaces.core.GigaSpace}
    //    * using the {@link org.openspaces.core.executor.TaskGigaSpace} annotation. Another option is by implementing
    //    * the {@link org.openspaces.core.executor.TaskGigaSpaceAware} interface.
    //    *
    //    * <p>Resource injection can be enabled by marking the task with {@link org.openspaces.core.executor.AutowireTask}
    //    * or with {@link org.openspaces.core.executor.AutowireTaskMarker}. Resources defined within processing unit
    //    * (space node) the task is executed on are accessible by using either the {@link org.springframework.beans.factory.annotation.Autowired} or
    //    * <tt>javax.annotation.Resource</tt> annotations (assuming they are enabled using <code>context:annotation-config</code>).
    //    * Bean life cycle methods, such as {@link org.openspaces.core.cluster.ClusterInfoAware} and
    //    * {@link org.springframework.context.ApplicationContextAware} are also available.
    //     *
    //     * @param task     The task to execute
    //     * @param listener A listener to be notified when execution completes
    //     * @return a Future representing pending completion of the task,
    //     *         and whose <code>get()</code> method will return the task value upon completion.
    //     */
    // REMOVE this because of compilation problem, you can still use this API.
    //    <T extends Serializable, R> AsyncFuture<R> execute(DistributedTask<T, R> task, AsyncFutureListener<R> listener);

    /**
     * Constructs an executor builder allowing to accumulate different tasks required to be executed
     * into a single execute mechanism with all the results reduced by the reducer provided.
     *
     * <p>The reducer can optionally implement {@link com.gigaspaces.async.AsyncResultFilter} that can control
     * if tasks should continue to accumulate or it should break and execute the reduce operation on the
     * results received so far.
     *
     * <p>The space that the task is executed within can be accessible by marking a field with type {@link org.openspaces.core.GigaSpace}
     * using the {@link org.openspaces.core.executor.TaskGigaSpace} annotation. Another option is by implementing
     * the {@link org.openspaces.core.executor.TaskGigaSpaceAware} interface.
     *
     * <p>Resource injection can be enabled by marking the task with {@link org.openspaces.core.executor.AutowireTask}
     * or with {@link org.openspaces.core.executor.AutowireTaskMarker}. Resources defined within processing unit
     * (space node) the task is executed on are accessible by using either the {@link org.springframework.beans.factory.annotation.Autowired} or
     * <tt>javax.annotation.Resource</tt> annotations (assuming they are enabled using <code>context:annotation-config</code>).
     * Bean life cycle methods, such as {@link org.openspaces.core.cluster.ClusterInfoAware} and
     * {@link org.springframework.context.ApplicationContextAware} are also available.
     *
     * @param reducer The reducer to reduce the results of all the different tasks added.
     * @return The executor builder.
     */
    <T extends Serializable, R> ExecutorBuilder<T, R> executorBuilder(AsyncResultsReducer<T, R> reducer);
    
    /**
     * Gets the type manager of this GigaSpace instance.
     * 
     * @see org.openspaces.core.GigaSpaceTypeManager
     * 
     * @since 8.0
     */
    GigaSpaceTypeManager getTypeManager();
}
