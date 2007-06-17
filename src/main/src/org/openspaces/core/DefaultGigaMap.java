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

import com.j_spaces.core.client.ReadModifiers;
import com.j_spaces.map.IMap;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.space.JavaSpace;
import org.openspaces.core.exception.ExceptionTranslator;
import org.openspaces.core.transaction.TransactionProvider;
import org.springframework.transaction.TransactionDefinition;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author kimchy
 */
public class DefaultGigaMap<K, V> implements GigaMap<K, V> {

    private IMap map;

    private TransactionProvider txProvider;

    private ExceptionTranslator exTranslator;

    private long defaultWaitForResponse = JavaSpace.NO_WAIT;

    private long defaultTimeToLive = Lease.FOREVER;

    private int defaultIsolationLevel;

    /**
     * Constructs a new DefaultGigaMap implementation.
     *
     * @param map          The map implementation to delegate operations to
     * @param txProvider   The transaction provider for declarative transaction ex.
     * @param exTranslator Exception translator to translate low level exceptions into GigaSpaces runtime
     *                     exception
     */
    public DefaultGigaMap(IMap map, TransactionProvider txProvider, ExceptionTranslator exTranslator,
                          int defaultIsolationLevel) {
        this.map = map;
        this.txProvider = txProvider;
        this.exTranslator = exTranslator;
        // set the default read take modifiers according to the default isolation level
        // NOTE: by default, Map implemenation use REPEATABLE_READ
        switch (defaultIsolationLevel) {
            case TransactionDefinition.ISOLATION_DEFAULT:
                this.defaultIsolationLevel = ReadModifiers.REPEATABLE_READ;
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
                throw new IllegalArgumentException("GigaMap does not support serializable isolation level");
        }
    }

    public void setDefaultWaitForResponse(long defaultWaitForResponse) {
        this.defaultWaitForResponse = defaultWaitForResponse;
    }

    public void setDefaultTimeToLive(long defaultTimeToLive) {
        this.defaultTimeToLive = defaultTimeToLive;
    }

    public IMap getMap() {
        return this.map;
    }

    public TransactionProvider getTxProvider() {
        return this.txProvider;
    }

    public int size() {
        // TODO add transactional context here + modifiers
        return map.size();
    }

    public boolean isEmpty() {
        // TODO add transactional context here + modifiers
        return map.isEmpty();
    }

    public boolean containsKey(Object key) {
        // TODO add transactional context here + modifiers
        return map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        // TODO add transactional context here + modifiers
        return map.containsValue(value);
    }

    public V get(Object key) {
        return get(key, defaultWaitForResponse);
    }

    public V get(Object key, long waitForResponse) {
        return get(key, waitForResponse, getModifiersForIsolationLevel());
    }

    @SuppressWarnings({"unchecked"})
    public V get(Object key, long waitForResponse, int modifiers) {
        try {
            return (V) map.get(key, getCurrentTransaction(), waitForResponse, modifiers);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public V put(K key, V value) {
        return put(key, value, defaultTimeToLive);
    }

    @SuppressWarnings({"unchecked"})
    public V put(K key, V value, long timeToLive) {
        try {
            return (V) map.put(key, value, getCurrentTransaction(), timeToLive);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public V remove(Object key) {
        return remove(key, defaultWaitForResponse);
    }

    @SuppressWarnings({"unchecked"})
    public V remove(Object key, long waitForReponse) {
        try {
            return (V) map.remove(key, getCurrentTransaction(), waitForReponse);
        } catch (Exception e) {
            throw exTranslator.translate(e);
        }
    }

    public void putAll(Map<? extends K, ? extends V> t) {
        // TODO add transactional context here + modifiers
        map.putAll(t);
    }

    public void clear() {
        // TODO add transactional context here + modifiers
        map.clear();
    }

    public void clear(boolean clearMaster) {
        // TODO add transactional context here + modifiers
        map.clear(clearMaster);
    }

    @SuppressWarnings({"unchecked"})
    public Set<K> keySet() {
        // TODO add transactional context here + modifiers
        return map.keySet();
    }

    @SuppressWarnings({"unchecked"})
    public Collection<V> values() {
        // TODO add transactional context here + modifiers
        return map.values();
    }

    @SuppressWarnings({"unchecked"})
    public Set<Entry<K, V>> entrySet() {
        // TODO add transactional context here + modifiers
        return map.entrySet();
    }

    // Support methods

    public Transaction getCurrentTransaction() {
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
     * default isolation level associated with this class (which is <code>REPEATABLE_READ</code>).
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

    public String toString() {
        return map.toString();
    }
}
