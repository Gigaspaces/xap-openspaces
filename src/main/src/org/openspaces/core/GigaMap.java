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

import com.j_spaces.javax.cache.Cache;
import com.j_spaces.map.IMap;
import net.jini.core.transaction.Transaction;
import org.openspaces.core.transaction.TransactionProvider;

import java.util.Map;

/**
 * Provides a simpler interface on top of {@link com.j_spaces.map.IMap} and
 * {@link com.j_spaces.javax.cache.Cache} implementation.
 *
 * <p>Though this interface has a single implementation it is still important to work against the
 * interface as it allows for simpler testing and mocking.
 *
 * <p>Transaction management is implicit and works in a declarative manner. Operations do not accept a
 * transaction object, and will automatically use the {@link TransactionProvider} in order to acquire
 * the current running transaction. If there is no current running transaction the operation will be
 * executed without a transaction.
 *
 * @author kimchy
 */
public interface GigaMap extends Map, Cache {

    /**
     * Returns the <code>IMap</code> used by this GigaMap implementation to delegate
     * different space operations.
     *
     * <p>Allows to execute map operations that are not exposed by this interface, as well as using
     * it as a parameter to other low level GigaSpace components.
     *
     * <p>If a transaction object is required for low level operations (as low level operations do not
     * have declarative transaction ex) the {@link #getTxProvider()} should be used to acquire the
     * current running transaction.
     */
    IMap getMap();

    /**
     * Returns the transaction provider allowing to access the current running transaction. Allows
     * to execute low level {@link IMap} operations that requires explicit
     * transaction object.
     */
    TransactionProvider getTxProvider();

    /**
     * Returns the current running transaction. Can be <code>null</code> if no transaction is in progress.
     */
    Transaction getCurrentTransaction();

    /**
     * Returns the object that is associated with <code>key</code> in this cache.
     * Client will wait at most <code>waitForResponse</code> milliseconds for
     * get call to return.
     *
     * @param key             key whose associated value is to be returned.
     * @param waitForResponse time to wait for response
     * @return Returns the object that is associated with the key
     */
    Object get(Object key, long waitForResponse);

    /**
     * Returns the object that is associated with <code>key</code> in this cache.
     * Client will wait at most <code>waitForResponse</code> milliseconds for
     * get call to return.
     *
     * @param key             key whose associated value is to be returned.
     * @param waitForResponse time to wait for response
     * @param modifiers       one or a union of {@link com.j_spaces.core.client.ReadModifiers}.
     * @return Returns the object that is associated with the key
     */
    Object get(Object key, long waitForResponse, int modifiers);

    /**
     * Puts <code>value</code> to the cache with specified <code>key</code> for
     * <code>timeToLive</code> milliseconds to live in the cache.
     *
     * @param key        key for the <code>value</code>
     * @param value      object(~ entry)
     * @param timeToLive time to keep object in this cache, in milliseconds
     * @return previous value associated with specified key, or <tt>null</tt>
     *         if there was no mapping for key.
     */
    Object put(Object key, Object value, long timeToLive);


    /**
     * Copies all of the mappings from the specified map to the cache.
     *
     * @param t A map of key and values to be copy into the cache
     */
    void putAll(Map t, long timeToLive);


    /**
     * Removes the mapping for this <code>key</code> from this cache.
     * Client will wait at most <code>waitForResponse</code> milliseconds for
     * get call to return.
     *
     * @param key             key whose associated value is to be returned.
     * @param waitForResponse time to wait for response
     * @return The removed object
     */
    Object remove(Object key, long waitForResponse);

    /**
     * The clear method will remove all objects from
     * the cache including the key and the associated value.
     *
     * @param clearMaster if <code>true</code> clear also master, when <code>false</code> same as {@link #clear()}.)
     */
    void clear(boolean clearMaster);
}
