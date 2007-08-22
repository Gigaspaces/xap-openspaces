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

package org.openspaces.core.map;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.ClientUIDHandler;
import com.j_spaces.core.client.EntryNotInSpaceException;
import com.j_spaces.core.client.ExternalEntry;
import com.j_spaces.core.client.LocalTransactionManager;
import com.j_spaces.core.client.ReadModifiers;
import com.j_spaces.map.Envelope;
import com.j_spaces.map.IMap;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.SpaceTimeoutException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.CannotCreateTransactionException;

import java.rmi.RemoteException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The lock manager is built on top of {@link IMap} and supports the ability to lock and unlock
 * certain keys within the map.
 *
 * @author kimchy
 */
public class LockManager {

    private static Log logger = LogFactory.getLog(LockManager.class);

    private IMap map;

    private ConcurrentHashMap<String, Transaction> lockedUIDHashMap = new ConcurrentHashMap<String, Transaction>();

    private IJSpace masterSpace = null;

    private BlockingQueue<ExternalEntry> externalEntryPool;

    private LocalTransactionManager localTransactionManager;

    private Transaction dummyTxn;

    /**
     * Creates a new Lock Manager based on the {@link com.j_spaces.map.IMap}.
     */
    public LockManager(IMap map) {
        this.map = map;
        this.masterSpace = map.getMasterSpace();
        masterSpace.setOptimisticLocking(false);
        try {
            localTransactionManager = (LocalTransactionManager) LocalTransactionManager.getInstance(masterSpace);
        } catch (RemoteException e) {
            throw new CannotCreateTransactionException("Failed to obtain transaction lock manager", e);
        }

        externalEntryPool = new ArrayBlockingQueue<ExternalEntry>(1000);
        for (int i = 0; i < 1000; i++) {
            externalEntryPool.add(new ExternalEntry(Envelope.ENVELOPE_CLASS_NAME, null, null, null));
        }

        dummyTxn = getTransaction(Long.MAX_VALUE);
    }

    /**
     * Locks the given key for any updates. Retruns a {@link org.openspaces.core.map.LockHandle}
     * that can be used to perform specific updates under the same lock (by using the transaction
     * object stored within it).
     *
     * @param key                   The key to lock
     * @param lockTimeToLive        The lock time to live (in milliseconds)
     * @param timeoutWaitingForLock The time to wait for an already locked lock
     * @return LockHandle that can be used to perfrom operations under the given lock
     */
    public LockHandle lock(Object key, long lockTimeToLive, long timeoutWaitingForLock) {

        String uid = getUID(key);
        Transaction tr = null;
        try {
            tr = getTransaction(lockTimeToLive);
        } finally {
            if (tr == null) {
                lockedUIDHashMap.remove(uid);
                return null;
            }
        }

        ExternalEntry ee = getTemplate(key, uid);
        try {
            Object retTake = masterSpace.read(ee, tr, timeoutWaitingForLock, ReadModifiers.EXCLUSIVE_READ_LOCK);
            releaseEE(ee);
            if (retTake == null) {
                throw new SpaceTimeoutException("Failed waiting for lock on key [" + key + "]");
            }
        } catch (EntryNotInSpaceException e) {
            map.put(key, "", tr, Integer.MAX_VALUE);
        } catch (SpaceTimeoutException e) {
            try {
                tr.abort();
            } catch (Exception re) {
                logger.warn("Failed to abort transaction", e);
            }
            // rethrow
            throw e;
        } catch (Throwable t) {
            try {
                tr.abort();
            } catch (Exception re) {
                logger.warn("Failed to abort transaction", t);
            }
            lockedUIDHashMap.remove(uid);
            throw new DataAccessResourceFailureException("Failed to obtain lock for key [" + key + "]");
        }

        //otherwise, map uid->txn
        lockedUIDHashMap.put(uid, tr);
        return new LockHandle(this, tr, key);
    }

    /**
     * Unlocks the given key and puts the given value in a single operation.
     *
     * @param key   The key to unlock and put the value in
     * @param value The value to put after unlocking the key
     */
    public void putAndUnlock(Object key, Object value) {
        String uid = getUID(key);
        Transaction tr = lockedUIDHashMap.get(uid);

        if (tr == null) {
            map.put(key, value, null, Integer.MAX_VALUE);
            return;
        }

        try {
            map.put(key, value, tr, Integer.MAX_VALUE);
            tr.commit();
        } catch (Throwable t) {
            logger.warn("Failed to commit transaction and unlock the key [" + key + "], ignoring", t);
        } finally {
            lockedUIDHashMap.remove(uid);
        }
    }

    /**
     * Returns <code>true</code> if the given key is locked. Otherwise returns <code>false</code>.
     *
     * @param key The key to check if it locked or not.
     * @return <code>true</code> if the given key is locked or not.
     */
    public boolean islocked(Object key) {
        // first check locally
        String uid = getUID(key);
        boolean locked = lockedUIDHashMap.containsKey(uid);
        if (locked) {
            return true;
        }
        // now check globally
        ExternalEntry ee = getTemplate(key, uid);
        try {
            Object lockEntry = masterSpace.read(ee, null, 0, ReadModifiers.DIRTY_READ);
            if (lockEntry == null) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Unlocks the given lock on the key
     *
     * @param key The key to unlock
     */
    public void unlock(Object key) {
        String uid = getUID(key);

        Transaction tr = lockedUIDHashMap.get(uid);
        if (tr == null) {
            return;
        }

        try {
            tr.abort();
        } catch (Exception e) {
            logger.warn("Failed to abort transaction and unlocking the object, ignoring", e);
        } finally {
            lockedUIDHashMap.remove(uid);
        }
    }

    private void releaseEE(ExternalEntry ee) {
        if (ee != null) {
            if (!externalEntryPool.offer(ee))
                throw new RuntimeException("release of ExternalEntry resource failed.");
        }
    }

    private Transaction getTransaction(long timeout) throws CannotCreateTransactionException {
        Transaction.Created tCreated;
        try {
            tCreated = TransactionFactory.create(localTransactionManager, timeout);
        } catch (Exception e) {
            throw new CannotCreateTransactionException("Failed to create lock transaction", e);
        }
        return tCreated.transaction;
    }

    private String getUID(Object key) {
        return ClientUIDHandler.createUIDFromName(key.toString(), Envelope.ENVELOPE_CLASS_NAME);
    }

    private ExternalEntry getTemplate(Object key, String uid) {
        ExternalEntry ee;
        try {
            ee = externalEntryPool.take();
        } catch (InterruptedException e) {
            throw new DataAccessResourceFailureException("Failed to take resource from pool", e);
        }

        // to support load balancing
        ee.m_FieldsValues = new Object[1];
        ee.m_FieldsValues[Envelope.KEY] = key;
        ee.m_UID = uid;
        return ee;
    }

}
