/*
 * Copyright 2002-2006 the original author or authors.
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
import com.gigaspaces.async.AsyncFutureListener;
import com.gigaspaces.async.AsyncResultFilter;
import com.gigaspaces.async.AsyncResultsReducer;
import com.gigaspaces.async.FutureFactory;
import com.gigaspaces.client.ReadByIdsResult;
import com.gigaspaces.client.TakeByIdsResult;
import com.gigaspaces.client.ReadByIdsResultImpl;
import com.gigaspaces.client.TakeByIdsResultImpl;
import com.gigaspaces.internal.client.QueryResultTypeInternal;
import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.query.ISpaceQuery;
import com.gigaspaces.query.QueryResultType;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.LeaseContext;
import com.j_spaces.core.client.ReadModifiers;

import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.space.JavaSpace;

import org.openspaces.core.exception.ExceptionTranslator;
import org.openspaces.core.executor.DistributedTask;
import org.openspaces.core.executor.Task;
import org.openspaces.core.executor.TaskRoutingProvider;
import org.openspaces.core.executor.internal.ExecutorMetaDataProvider;
import org.openspaces.core.executor.internal.InternalDistributedSpaceTaskWrapper;
import org.openspaces.core.executor.internal.InternalSpaceTaskWrapper;
import org.openspaces.core.internal.InternalGigaSpace;
import org.openspaces.core.transaction.TransactionProvider;
import org.openspaces.core.transaction.internal.InternalAsyncFuture;
import org.openspaces.core.transaction.internal.InternalAsyncFutureListener;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.TransactionDefinition;

import java.io.Serializable;
import java.rmi.RemoteException;

/**
 * Default implementation of {@link GigaSpace}. Constructed with {@link com.j_spaces.core.IJSpace},
 * {@link org.openspaces.core.transaction.TransactionProvider} and
 * {@link org.openspaces.core.exception.ExceptionTranslator}.
 *
 * <p>Operations are delegated to {@link com.j_spaces.core.IJSpace} with transactions acquired using
 * {@link org.openspaces.core.transaction.TransactionProvider}. Any exceptions thrown during the
 * operations are translated using {@link org.openspaces.core.exception.ExceptionTranslator}.
 *
 * <p>Allows to set default timeouts for read and take operations and default lease for write
 * operation.
 *
 * @author kimchy
 */
public class DefaultGigaSpace implements GigaSpace, InternalGigaSpace {

    private String name;

    final private ISpaceProxy space;

    final private TransactionProvider txProvider;

    final private ExceptionTranslator exTranslator;

    final private QueryResultType queryResultType;
    final private QueryResultTypeInternal queryResultTypeInternal;

    private long defaultReadTimeout = JavaSpace.NO_WAIT;

    private long defaultTakeTimeout = JavaSpace.NO_WAIT;

    private long defaultWriteLease = Lease.FOREVER;

    private int defaultIsolationLevel;
    final private ExecutorMetaDataProvider executorMetaDataProvider = new ExecutorMetaDataProvider();

    private DefaultGigaSpace clusteredGigaSpace;

    /**
     * Constructs a new DefaultGigaSpace implementation.
     *
     * @param space                 The space implementation to delegate operations to
     * @param txProvider            The transaction provider for declarative transaction ex.
     * @param exTranslator          Exception translator to translate low level exceptions into GigaSpaces runtime
     *                              exception
     * @param defaultIsolationLevel The default isolation level for read operations without modifiers. Maps to
     *                              {@link org.springframework.transaction.TransactionDefinition#getIsolationLevel()}
     *                              levels values.
     * @param queryResultType       The type of results for query operations.                            
     */
    public DefaultGigaSpace(IJSpace space, TransactionProvider txProvider, ExceptionTranslator exTranslator,
            int defaultIsolationLevel, QueryResultType queryResultType) {
        this.space = (ISpaceProxy) space;
        this.txProvider = txProvider;
        this.exTranslator = exTranslator;
        this.queryResultType = queryResultType != QueryResultType.NOT_SET ? queryResultType : QueryResultType.OBJECT;
        this.queryResultTypeInternal = getQueryResultTypeInternal(this.queryResultType);
        // set the default read take modifiers according to the default isolation level
        switch (defaultIsolationLevel) {
            case TransactionDefinition.ISOLATION_DEFAULT:
                this.defaultIsolationLevel = space.getReadModifiers();
                break;
            case TransactionDefinition.ISOLATION_READ_UNCOMMITTED:
                this.defaultIsolationLevel = ReadModifiers.DIRTY_READ;
                break;
            case TransactionDefinition.ISOLATION_READ_COMMITTED:
                this.defaultIsolationLevel = ReadModifiers.READ_COMMITTED;
                break;
            case TransactionDefinition.ISOLATION_REPEATABLE_READ:
                this.defaultIsolationLevel = ReadModifiers.REPEATABLE_READ;
                break;
            case TransactionDefinition.ISOLATION_SERIALIZABLE:
                throw new IllegalArgumentException("GigaSpace does not support serializable isolation level");
        }
    }
    
    private DefaultGigaSpace(IJSpace space, DefaultGigaSpace other) {
        this(space, other.txProvider, other.exTranslator, other.defaultIsolationLevel, other.queryResultType);
        setDefaultReadTimeout(other.defaultReadTimeout);
        setDefaultTakeTimeout(other.defaultTakeTimeout);
        setDefaultWriteLease(other.defaultWriteLease);
    }
    
    private static QueryResultTypeInternal getQueryResultTypeInternal(QueryResultType queryResultType) {
        switch (queryResultType)
        {
            case OBJECT: return QueryResultTypeInternal.OBJECT;
            case DOCUMENT: return QueryResultTypeInternal.DOCUMENT_ENTRY;
            default: throw new IllegalArgumentException("Unsupported QueryResultType: " + queryResultType);
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
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

    // GigaSpace interface Methods

    public IJSpace getSpace() {
        return this.space;
    }

    public GigaSpace getClustered() {
        if (clusteredGigaSpace != null) {
            return clusteredGigaSpace;
        }
        if (this.space.isClustered()) {
            clusteredGigaSpace = this;
        } else {
            DefaultGigaSpace newClusteredGigaSpace = null;
            try {
                newClusteredGigaSpace = new DefaultGigaSpace(this.space.getClusteredSpace(), this);
            } catch (Exception e) {
                throw new InvalidDataAccessApiUsageException("Failed to get clustered Space from actual space", e);
            }
            //GS-8287: try to assign the created single clustered GigaSpace instance to the volatile reference
            //but avoid locking at creation - we don't promise a single instance being returned. 
            if (clusteredGigaSpace == null) {
                clusteredGigaSpace = newClusteredGigaSpace;
            }
        }
        return this.clusteredGigaSpace;
    }

    public TransactionProvider getTxProvider() {
        return this.txProvider;
    }

    public ExceptionTranslator getExceptionTranslator() {
        return this.exTranslator;
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

    public int clear(Object template, int modifiers) throws DataAccessException {
        try {
            return space.clear(template, getCurrentTransaction(), modifiers);
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

    public <T> ISpaceQuery<T> snapshot(Object entry) throws DataAccessException {
        try {
            return space.snapshot(entry);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> T readById(Class<T> clazz, Object id) {
        return readById(clazz, id, null, defaultReadTimeout, getModifiersForIsolationLevel());
    }

    public <T> T readById(Class<T> clazz, Object id, Object routing) {
        return readById(clazz, id, routing, defaultReadTimeout, getModifiersForIsolationLevel());
    }

    public <T> T readById(Class<T> clazz, Object id, Object routing, long timeout) {
        return readById(clazz, id, routing, timeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T readById(Class<T> clazz, Object id, Object routing, long timeout, int modifiers) {
        try {
            return (T) space.readById(clazz.getName(), id, routing, getCurrentTransaction(), timeout, modifiers, false, queryResultTypeInternal);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T readById(String typeName, Object id) {
        return (T)readById(typeName, id, null, defaultReadTimeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T readById(String typeName, Object id, Object routing) {
        return (T)readById(typeName, id, routing, defaultReadTimeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T readById(String typeName, Object id, Object routing, long timeout) {
        return (T)readById(typeName, id, routing, timeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T readById(String typeName, Object id, Object routing, long timeout, int modifiers) {
        try {
            return (T) space.readById(typeName, id, routing, getCurrentTransaction(), timeout, modifiers, false, queryResultTypeInternal);
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

    public <T> T read(ISpaceQuery<T> template) throws DataAccessException {
        return read(template, defaultReadTimeout);
    }

    public <T> T read(ISpaceQuery<T> template, long timeout) throws DataAccessException {
        return read(template, timeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T read(ISpaceQuery<T> template, long timeout, int modifiers) throws DataAccessException {
        try {
            return (T) space.read(template, getCurrentTransaction(), timeout, modifiers);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> AsyncFuture<T> asyncRead(T template) throws DataAccessException {
        return asyncRead(template, defaultReadTimeout);
    }

    public <T> AsyncFuture<T> asyncRead(T template, AsyncFutureListener<T> listener) throws DataAccessException {
        return asyncRead(template, defaultReadTimeout, listener);
    }

    public <T> AsyncFuture<T> asyncRead(T template, long timeout) throws DataAccessException {
        return asyncRead(template, timeout, null);
    }

    public <T> AsyncFuture<T> asyncRead(T template, long timeout, AsyncFutureListener<T> listener) throws DataAccessException {
        return asyncRead(template, timeout, getModifiersForIsolationLevel(), listener);
    }

    public <T> AsyncFuture<T> asyncRead(T template, long timeout, int modifiers) throws DataAccessException {
        return asyncRead(template, timeout, modifiers, null);
    }

    public <T> AsyncFuture<T> asyncRead(T template, long timeout, int modifiers, AsyncFutureListener<T> listener) throws DataAccessException {
        Transaction tx = getCurrentTransaction();
        try {
            return wrapFuture(space.asyncRead(template, tx, timeout, modifiers, wrapListener(listener, tx)), tx);
        } catch (RemoteException e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> AsyncFuture<T> asyncRead(ISpaceQuery<T> template) throws DataAccessException {
        return asyncRead(template, defaultReadTimeout);
    }

    public <T> AsyncFuture<T> asyncRead(ISpaceQuery<T> template, AsyncFutureListener<T> listener) throws DataAccessException {
        return asyncRead(template, defaultReadTimeout, listener);
    }

    public <T> AsyncFuture<T> asyncRead(ISpaceQuery<T> template, long timeout) throws DataAccessException {
        return asyncRead(template, timeout, (AsyncFutureListener<T>) null);
    }

    public <T> AsyncFuture<T> asyncRead(ISpaceQuery<T> template, long timeout, AsyncFutureListener<T> listener) throws DataAccessException {
        return asyncRead(template, timeout, getModifiersForIsolationLevel(), listener);
    }

    public <T> AsyncFuture<T> asyncRead(ISpaceQuery<T> template, long timeout, int modifiers) throws DataAccessException {
        return asyncRead(template, timeout, modifiers, (AsyncFutureListener<T>) null);
    }

    public <T> AsyncFuture<T> asyncRead(ISpaceQuery<T> template, long timeout, int modifiers, AsyncFutureListener<T> listener) throws DataAccessException {
        Transaction tx = getCurrentTransaction();
        try {
            return wrapFuture(space.asyncRead(template, tx, timeout, modifiers, wrapListener(listener, tx)), tx);
        } catch (RemoteException e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> T readIfExistsById(Class<T> clazz, Object id) {
        return readIfExistsById(clazz, id, null, defaultReadTimeout, getModifiersForIsolationLevel());
    }

    public <T> T readIfExistsById(Class<T> clazz, Object id, Object routing) {
        return readIfExistsById(clazz, id, routing, defaultReadTimeout, getModifiersForIsolationLevel());
    }

    public <T> T readIfExistsById(Class<T> clazz, Object id, Object routing, long timeout) {
        return readIfExistsById(clazz, id, routing, timeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T readIfExistsById(Class<T> clazz, Object id, Object routing, long timeout, int modifiers) {
        try {
            return (T) space.readById(clazz.getName(), id, routing, getCurrentTransaction(), timeout, modifiers, true, queryResultTypeInternal);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> T readIfExistsById(String typeName, Object id) {
        return (T)readIfExistsById(typeName, id, null, defaultReadTimeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T readIfExistsById(String typeName, Object id, Object routing) {
        return (T)readIfExistsById(typeName, id, routing, defaultReadTimeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T readIfExistsById(String typeName, Object id, Object routing, long timeout) {
        return (T)readIfExistsById(typeName, id, routing, timeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T readIfExistsById(String typeName, Object id, Object routing, long timeout, int modifiers) {
        try {
            return (T) space.readById(typeName, id, routing, getCurrentTransaction(), timeout, modifiers, true, queryResultTypeInternal);
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

    public <T> T readIfExists(ISpaceQuery<T> template) throws DataAccessException {
        return readIfExists(template, defaultReadTimeout);
    }

    public <T> T readIfExists(ISpaceQuery<T> template, long timeout) throws DataAccessException {
        return readIfExists(template, timeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T readIfExists(ISpaceQuery<T> template, long timeout, int modifiers) throws DataAccessException {
        try {
            return (T) space.readIfExists(template, getCurrentTransaction(), timeout, modifiers);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> T[] readMultiple(T template, int maxEntries) throws DataAccessException {
        return readMultiple(template, maxEntries, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T[] readMultiple(T template, int maxEntries, int modifiers) throws DataAccessException {
        try {
            return (T[]) space.readMultiple(template, getCurrentTransaction(), maxEntries, modifiers);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> T[] readMultiple(ISpaceQuery<T> template, int maxEntries) throws DataAccessException {
        return readMultiple(template, maxEntries, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T[] readMultiple(ISpaceQuery<T> template, int maxEntries, int modifiers) throws DataAccessException {
        try {
            return (T[]) space.readMultiple(template, getCurrentTransaction(), maxEntries, modifiers);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> T takeById(Class<T> clazz, Object id) {
        return takeById(clazz, id, null, defaultTakeTimeout, getModifiersForIsolationLevel());
    }

    public <T> T takeById(Class<T> clazz, Object id, Object routing) {
        return takeById(clazz, id, routing, defaultTakeTimeout, getModifiersForIsolationLevel());
    }

    public <T> T takeById(Class<T> clazz, Object id, Object routing, long timeout) {
        return takeById(clazz, id, routing, timeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T takeById(Class<T> clazz, Object id, Object routing, long timeout, int modifiers) {
        try {
            return (T) space.takeById(clazz.getName(), id, routing, getCurrentTransaction(), timeout, modifiers, false, queryResultTypeInternal);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }
    @SuppressWarnings("unchecked")
    public <T> T takeById(String typeName, Object id) {
        return (T)takeById(typeName, id, null, defaultTakeTimeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T takeById(String typeName, Object id, Object routing) {
        return (T)takeById(typeName, id, routing, defaultTakeTimeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T takeById(String typeName, Object id, Object routing, long timeout) {
        return (T)takeById(typeName, id, routing, timeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T takeById(String typeName, Object id, Object routing, long timeout, int modifiers) {
        try {
            return (T) space.takeById(typeName, id, routing, getCurrentTransaction(), timeout, modifiers, false, queryResultTypeInternal);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> T take(T template) throws DataAccessException {
        return take(template, defaultTakeTimeout);
    }

    public <T> T take(T template, long timeout) throws DataAccessException {
        return take(template, timeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T take(T template, long timeout, int modifiers) throws DataAccessException {
        try {
            return (T) space.take(template, getCurrentTransaction(), timeout, modifiers);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> T take(ISpaceQuery<T> template) throws DataAccessException {
        return take(template, defaultTakeTimeout);
    }

    public <T> T take(ISpaceQuery<T> template, long timeout) throws DataAccessException {
        return take(template, timeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T take(ISpaceQuery<T> template, long timeout, int modifiers) throws DataAccessException {
        try {
            return (T) space.take(template, getCurrentTransaction(), timeout, modifiers);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> AsyncFuture<T> asyncTake(T template) throws DataAccessException {
        return asyncTake(template, defaultTakeTimeout);
    }

    public <T> AsyncFuture<T> asyncTake(T template, AsyncFutureListener<T> listener) throws DataAccessException {
        return asyncTake(template, defaultTakeTimeout, listener);
    }

    public <T> AsyncFuture<T> asyncTake(T template, long timeout) throws DataAccessException {
        return asyncTake(template, timeout, getModifiersForIsolationLevel());
    }

    public <T> AsyncFuture<T> asyncTake(T template, long timeout, AsyncFutureListener<T> listener) throws DataAccessException {
        return asyncTake(template, timeout, getModifiersForIsolationLevel(), listener);
    }

    public <T> AsyncFuture<T> asyncTake(T template, long timeout, int modifiers) throws DataAccessException {
        return asyncTake(template, timeout, modifiers, null);
    }

    public <T> AsyncFuture<T> asyncTake(T template, long timeout, int modifiers, AsyncFutureListener<T> listener) throws DataAccessException {
        Transaction tx = getCurrentTransaction();
        try {
            return wrapFuture(space.asyncTake(template, tx, timeout, modifiers, wrapListener(listener, tx)), tx);
        } catch (RemoteException e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> AsyncFuture<T> asyncTake(ISpaceQuery<T> template) throws DataAccessException {
        return asyncTake(template, defaultTakeTimeout);
    }

    public <T> AsyncFuture<T> asyncTake(ISpaceQuery<T> template, AsyncFutureListener<T> listener) throws DataAccessException {
        return asyncTake(template, defaultTakeTimeout, listener);
    }

    public <T> AsyncFuture<T> asyncTake(ISpaceQuery<T> template, long timeout) throws DataAccessException {
        return asyncTake(template, timeout, getModifiersForIsolationLevel());
    }

    public <T> AsyncFuture<T> asyncTake(ISpaceQuery<T> template, long timeout, AsyncFutureListener<T> listener) throws DataAccessException {
        return asyncTake(template, timeout, getModifiersForIsolationLevel(), listener);
    }

    public <T> AsyncFuture<T> asyncTake(ISpaceQuery<T> template, long timeout, int modifiers) throws DataAccessException {
        return asyncTake(template, timeout, modifiers, (AsyncFutureListener<T>) null);
    }

    public <T> AsyncFuture<T> asyncTake(ISpaceQuery<T> template, long timeout, int modifiers, AsyncFutureListener<T> listener) throws DataAccessException {
        Transaction tx = getCurrentTransaction();
        try {
            return wrapFuture(space.asyncTake(template, tx, timeout, modifiers, wrapListener(listener, tx)), tx);
        } catch (RemoteException e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> T takeIfExistsById(Class<T> clazz, Object id) {
        return takeIfExistsById(clazz, id, null, defaultTakeTimeout, getModifiersForIsolationLevel());
    }

    public <T> T takeIfExistsById(Class<T> clazz, Object id, Object routing) {
        return takeIfExistsById(clazz, id, routing, defaultTakeTimeout, getModifiersForIsolationLevel());
    }

    public <T> T takeIfExistsById(Class<T> clazz, Object id, Object routing, long timeout) {
        return takeIfExistsById(clazz, id, routing, timeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T takeIfExistsById(Class<T> clazz, Object id, Object routing, long timeout, int modifiers) {
        try {
            return (T) space.takeById(clazz.getName(), id, routing, getCurrentTransaction(), timeout, modifiers, true, queryResultTypeInternal);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T takeIfExistsById(String typeName, Object id) {
        return (T)takeIfExistsById(typeName, id, null, defaultTakeTimeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T takeIfExistsById(String typeName, Object id, Object routing) {
        return (T)takeIfExistsById(typeName, id, routing, defaultTakeTimeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T takeIfExistsById(String typeName, Object id, Object routing, long timeout) {
        return (T)takeIfExistsById(typeName, id, routing, timeout, getModifiersForIsolationLevel());
    }

    @SuppressWarnings("unchecked")
    public <T> T takeIfExistsById(String typeName, Object id, Object routing, long timeout, int modifiers) {
        try {
            return (T) space.takeById(typeName, id, routing, getCurrentTransaction(), timeout, modifiers, true, queryResultTypeInternal);
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

    public <T> T takeIfExists(ISpaceQuery<T> template) throws DataAccessException {
        return takeIfExists(template, defaultTakeTimeout);
    }

    @SuppressWarnings("unchecked")
    public <T> T takeIfExists(ISpaceQuery<T> template, long timeout) throws DataAccessException {
        try {
            return (T) space.takeIfExists(template, getCurrentTransaction(), timeout);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T[] takeMultiple(T template, int maxEntries) throws DataAccessException {
        try {
            return (T[]) space.takeMultiple(template, getCurrentTransaction(), maxEntries);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T[] takeMultiple(ISpaceQuery<T> template, int maxEntries) throws DataAccessException {
        try {
            return (T[]) space.takeMultiple(template, getCurrentTransaction(), maxEntries);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T[] takeMultiple(T template, int maxEntries, int modifiers) throws DataAccessException {
        try {
            return (T[]) space.takeMultiple(template, getCurrentTransaction(), maxEntries, modifiers);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T[] takeMultiple(ISpaceQuery<T> template, int maxEntries, int modifiers) throws DataAccessException {
        try {
            return (T[]) space.takeMultiple(template, getCurrentTransaction(), maxEntries, modifiers);
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
    public <T> LeaseContext<T>[] writeMultiple(T[] entries, long lease, int updateModifiers) throws DataAccessException {
        try {
            return space.writeMultiple(entries, getCurrentTransaction(), lease, updateModifiers);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> Object[] updateMultiple(T[] entries, long[] leases) throws DataAccessException {
        try {
            Object[] retVals = space.updateMultiple(entries, getCurrentTransaction(), leases);
            for (int i = 0; i < retVals.length; i++) {
                if (retVals[i] instanceof Exception) {
                    retVals[i] = exTranslator.translate((Exception) retVals[i]);
                }
            }
            return retVals;
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T> Object[] updateMultiple(T[] entries, long[] leases, int updateModifiers) throws DataAccessException {
        try {
            Object[] retVals = space.updateMultiple(entries, getCurrentTransaction(), leases, updateModifiers);
            for (int i = 0; i < retVals.length; i++) {
                if (retVals[i] instanceof Exception) {
                    retVals[i] = exTranslator.translate((Exception) retVals[i]);
                }
            }
            return retVals;
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public IteratorBuilder iterator() {
        return new IteratorBuilder(this);
    }

    public <T extends Serializable> AsyncFuture<T> execute(Task<T> task) {
        return execute(task, (AsyncFutureListener<T>) null);
    }

    public <T extends Serializable> AsyncFuture<T> execute(Task<T> task, AsyncFutureListener<T> listener) {
        if (task instanceof DistributedTask) {
            return distExecute((DistributedTask) task, listener);
        }
        Object routing = null;
        if (task instanceof TaskRoutingProvider) {
            Object optionalRouting = executorMetaDataProvider.findRouting(((TaskRoutingProvider) task).getRouting());
            if (optionalRouting != null) {
                routing = optionalRouting;
            }
        }
        if (routing == null) {
            routing = executorMetaDataProvider.findRouting(task);
        }
        return execute(task, routing, listener);
    }

    public <T extends Serializable> AsyncFuture<T> execute(Task<T> task, Object routing) {
        return execute(task, routing, (AsyncFutureListener<T>) null);
    }

    public <T extends Serializable> AsyncFuture<T> execute(Task<T> task, Object routing, AsyncFutureListener<T> listener) {
        Object optionalRouting = executorMetaDataProvider.findRouting(routing);
        if (optionalRouting != null) {
            routing = optionalRouting;
        }
        try {
            Transaction tx = getCurrentTransaction();
            return wrapFuture(space.execute(new InternalSpaceTaskWrapper<T>(task, routing), tx, wrapListener(listener, tx)), tx);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T extends Serializable, R> AsyncFuture<R> execute(DistributedTask<T, R> task, Object... routings) {
        AsyncFutureListener<R> listener = null;
        int numberOfRoutings = routings.length;
        if (routings.length > 0 && routings[routings.length - 1] instanceof AsyncFutureListener) {
            listener = (AsyncFutureListener<R>) routings[routings.length - 1];
            numberOfRoutings -= 1;
        }
        if (numberOfRoutings == 0) {
            return execute(task, listener);
        }
        AsyncFuture<T>[] futures = new AsyncFuture[numberOfRoutings];
        Transaction tx = getCurrentTransaction();
        for (int i = 0; i < numberOfRoutings; i++) {
            try {
                Object routing = routings[i];
                Object optionalRouting = executorMetaDataProvider.findRouting(routing);
                if (optionalRouting != null) {
                    routing = optionalRouting;
                }
                futures[i] = space.execute(new InternalSpaceTaskWrapper<T>(task, routing), tx, (AsyncFutureListener) null);
            } catch (Exception e) {
                throw exTranslator.translate(e);
            }
        }
        AsyncFuture<R> result;
        if (task instanceof AsyncResultFilter) {
            result = FutureFactory.create(futures, task, (AsyncResultFilter<T>) task);
        } else {
            result = FutureFactory.create(futures, task);
        }
        result = wrapFuture(result, tx);
        if (listener != null) {
            result.setListener(wrapListener(listener, tx));
        }
        return result;
    }

    public <T extends Serializable, R> AsyncFuture<R> execute(DistributedTask<T, R> task) {
        return execute(task, (AsyncFutureListener<R>) null);
    }

    public <T extends Serializable, R> AsyncFuture<R> execute(DistributedTask<T, R> task, AsyncFutureListener<R> listener) {
        return distExecute(task, listener);
    }

    public <T extends Serializable, R> AsyncFuture<R> distExecute(DistributedTask<T, R> task, AsyncFutureListener<R> listener) {
        try {
            Transaction tx = getCurrentTransaction();
            return wrapFuture(space.execute(new InternalDistributedSpaceTaskWrapper<T, R>(task), tx, wrapListener(listener, tx)), tx);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public <T extends Serializable, R> ExecutorBuilder<T, R> executorBuilder(AsyncResultsReducer<T, R> reducer) {
        return new ExecutorBuilder<T, R>(this, reducer);
    }

    // Support methods

    public Transaction getCurrentTransaction() {
        Transaction.Created txCreated = txProvider.getCurrentTransaction(this, space);
        if (txCreated != null) {
            return txCreated.transaction;
        }
        return null;
    }

    /**
     * Gets the isolation level from the current running transaction (enabling the usage of Spring
     * declarative isolation level settings). If there is no transaction in progress or the
     * transaction isolation is
     * {@link org.springframework.transaction.TransactionDefinition#ISOLATION_DEFAULT} will use the
     * default isolation level associated with this class.
     */
    public int getModifiersForIsolationLevel() {
        int isolationLevel = txProvider.getCurrentTransactionIsolationLevel(this);
        if (isolationLevel == TransactionDefinition.ISOLATION_DEFAULT) {
            return defaultIsolationLevel;
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
    
    /** {@inheritDoc} */
    public QueryResultType getQueryResultType()
    {
        return queryResultType;
    }

    @Override
    public String toString() {
        return space.toString();
    }

    public <T> AsyncFutureListener<T> wrapListener(AsyncFutureListener<T> listener, Transaction tx) {
        if (listener == null) {
            return null;
        }
        if (tx == null) {
            return new InternalAsyncFutureListener<T>(this, listener);
        }
        return InternalAsyncFutureListener.wrapIfNeeded(listener, this);
    }

    public <T> AsyncFuture<T> wrapFuture(AsyncFuture<T> future, Transaction tx) {
        return new InternalAsyncFuture<T>(future, this, tx);
    }

    @SuppressWarnings("unchecked")
    public <T> ReadByIdsResult<T> readByIds(Class<T> clazz, Object[] ids) {
        try {
            return new ReadByIdsResultImpl<T>((T[]) space.readByIds(clazz.getName(), ids, null, getCurrentTransaction(), getModifiersForIsolationLevel(), queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }    
    
    @SuppressWarnings("unchecked")
    public <T> ReadByIdsResult<T> readByIds(Class<T> clazz, Object[] ids, int modifiers) {
        try {
            return new ReadByIdsResultImpl<T>((T[]) space.readByIds(clazz.getName(), ids, null, getCurrentTransaction(), modifiers, queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ReadByIdsResult<T> readByIds(Class<T> clazz, Object[] ids, Object routing, int modifiers) {
        try {
            return new ReadByIdsResultImpl<T>((T[]) space.readByIds(clazz.getName(), ids, routing, getCurrentTransaction(), modifiers, queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ReadByIdsResult<T> readByIds(Class<T> clazz, Object[] ids, Object[] routings, int modifiers) {
        try {
            return new ReadByIdsResultImpl<T>((T[]) space.readByIds(clazz.getName(), ids, routings, getCurrentTransaction(), modifiers, queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ReadByIdsResult<T> readByIds(Class<T> clazz, Object[] ids, Object routing) {
        try {
            return new ReadByIdsResultImpl<T>((T[]) space.readByIds(clazz.getName(), ids, routing, getCurrentTransaction(), getModifiersForIsolationLevel(), queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ReadByIdsResult<T> readByIds(Class<T> clazz, Object[] ids, Object[] routings) {
        try {
            return new ReadByIdsResultImpl<T>((T[]) space.readByIds(clazz.getName(), ids, routings, getCurrentTransaction(), getModifiersForIsolationLevel(), queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ReadByIdsResult<T> readByIds(String typeName, Object[] ids) {
        try {
            return new ReadByIdsResultImpl<T>((T[]) space.readByIds(typeName, ids, null, getCurrentTransaction(), getModifiersForIsolationLevel(), queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }    
    
    @SuppressWarnings("unchecked")
    public <T> ReadByIdsResult<T> readByIds(String typeName, Object[] ids, int modifiers) {
        try {
            return new ReadByIdsResultImpl<T>((T[]) space.readByIds(typeName, ids, null, getCurrentTransaction(), modifiers, queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ReadByIdsResult<T> readByIds(String typeName, Object[] ids, Object routing, int modifiers) {
        try {
            return new ReadByIdsResultImpl<T>((T[]) space.readByIds(typeName, ids, routing, getCurrentTransaction(), modifiers, queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ReadByIdsResult<T> readByIds(String typeName, Object[] ids, Object[] routings, int modifiers) {
        try {
            return new ReadByIdsResultImpl<T>((T[]) space.readByIds(typeName, ids, routings, getCurrentTransaction(), modifiers, queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ReadByIdsResult<T> readByIds(String typeName, Object[] ids, Object routing) {
        try {
            return new ReadByIdsResultImpl<T>((T[]) space.readByIds(typeName, ids, routing, getCurrentTransaction(), getModifiersForIsolationLevel(), queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> ReadByIdsResult<T> readByIds(String typeName, Object[] ids, Object[] routings) {
        try {
            return new ReadByIdsResultImpl<T>((T[]) space.readByIds(typeName, ids, routings, getCurrentTransaction(), getModifiersForIsolationLevel(), queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> TakeByIdsResult<T> takeByIds(Class<T> clazz, Object[] ids, int modifiers) {
        try {
            return new TakeByIdsResultImpl<T>((T[]) space.takeByIds(clazz.getName(), ids, null, getCurrentTransaction(), modifiers, queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> TakeByIdsResult<T> takeByIds(Class<T> clazz, Object[] ids, Object routing, int modifiers) {
        try {
            return new TakeByIdsResultImpl<T>((T[]) space.takeByIds(clazz.getName(), ids, routing, getCurrentTransaction(), modifiers, queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> TakeByIdsResult<T> takeByIds(Class<T> clazz, Object[] ids, Object[] routings, int modifiers) {
        try {
            return new TakeByIdsResultImpl<T>((T[]) space.takeByIds(clazz.getName(), ids, routings, getCurrentTransaction(), modifiers, queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> TakeByIdsResult<T> takeByIds(String typeName, Object[] ids) {
        try {
            return new TakeByIdsResultImpl<T>((T[]) space.takeByIds(typeName, ids, null, getCurrentTransaction(), getModifiersForIsolationLevel(), queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> TakeByIdsResult<T> takeByIds(String typeName, Object[] ids, Object routing) {
        try {
            return new TakeByIdsResultImpl<T>((T[]) space.takeByIds(typeName, ids, routing, getCurrentTransaction(), getModifiersForIsolationLevel(), queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> TakeByIdsResult<T> takeByIds(String typeName, Object[] ids, Object[] routings) {
        try {
            return new TakeByIdsResultImpl<T>((T[]) space.takeByIds(typeName, ids, routings, getCurrentTransaction(), getModifiersForIsolationLevel(), queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> TakeByIdsResult<T> takeByIds(String typeName, Object[] ids, int modifiers) {
        try {
            return new TakeByIdsResultImpl<T>((T[]) space.takeByIds(typeName, ids, null, getCurrentTransaction(), modifiers, queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> TakeByIdsResult<T> takeByIds(String typeName, Object[] ids, Object routing, int modifiers) {
        try {
            return new TakeByIdsResultImpl<T>((T[]) space.takeByIds(typeName, ids, routing, getCurrentTransaction(), modifiers, queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> TakeByIdsResult<T> takeByIds(String typeName, Object[] ids, Object[] routings, int modifiers) {
        try {
            return new TakeByIdsResultImpl<T>((T[]) space.takeByIds(typeName, ids, routings, getCurrentTransaction(), modifiers, queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> TakeByIdsResult<T> takeByIds(Class<T> clazz, Object[] ids) {
        try {
            return new TakeByIdsResultImpl<T>((T[]) space.takeByIds(clazz.getName(), ids, null, getCurrentTransaction(), getModifiersForIsolationLevel(), queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> TakeByIdsResult<T> takeByIds(Class<T> clazz, Object[] ids, Object routing) {
        try {
            return new TakeByIdsResultImpl<T>((T[]) space.takeByIds(clazz.getName(), ids, routing, getCurrentTransaction(), getModifiersForIsolationLevel(), queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> TakeByIdsResult<T> takeByIds(Class<T> clazz, Object[] ids, Object[] routings) {
        try {
            return new TakeByIdsResultImpl<T>((T[]) space.takeByIds(clazz.getName(), ids, routings, getCurrentTransaction(), getModifiersForIsolationLevel(), queryResultTypeInternal, false));
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public SpaceTypeDescriptor getTypeDescriptor(String typeName)
    {
        try {
            return space.getSpaceTypeDescriptor(typeName);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }
    public SpaceTypeDescriptor getTypeDescriptor(Class<?> type)
    {
        try {
            if (type == null)
                throw new IllegalArgumentException("Argument cannot be null - 'type'.");
            return space.getSpaceTypeDescriptor(type.getName());
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    } 
}
