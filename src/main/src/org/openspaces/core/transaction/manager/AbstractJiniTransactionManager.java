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

package org.openspaces.core.transaction.manager;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lease.LeaseException;
import net.jini.core.transaction.CannotAbortException;
import net.jini.core.transaction.CannotCommitException;
import net.jini.core.transaction.TimeoutExpiredException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.core.transaction.server.NestableTransactionManager;
import net.jini.core.transaction.server.TransactionManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.InvalidIsolationLevelException;
import org.springframework.transaction.InvalidTimeoutException;
import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import java.rmi.RemoteException;

/**
 * Base class for Jini implementation of Springs {@link PlatformTransactionManager}. Uses Jini
 * {@link TransactionManager} in order to manage transactions with sub classes responsible for
 * providing it using {@link #doCreateTransactionManager()}.
 * 
 * <p>Jini transactions are bounded under the {@link #setTransactionalContext(Object)} using Springs
 * {@link TransactionSynchronizationManager#bindResource(Object,Object)}. The transactional context
 * is optional and defaults to the Jini {@link TransactionManager} instance. Note, this can be
 * overridden by sub classes.
 * 
 * <p>By default the transaction timeout will be <code>FOREVER</code>. The default timeout on the
 * transaction manager level can be set using {@link #setDefaultTimeout(Long)}. If the timeout is
 * explicitly set using Spring support for transactions (for example using
 * {@link org.springframework.transaction.TransactionDefinition}) this value will be used.
 * 
 * @author kimchy
 * @see org.openspaces.core.transaction.DefaultTransactionProvider
 * @see org.openspaces.core.transaction.manager.JiniTransactionHolder
 */
// TODO Need to support relative transaction timeout
public abstract class AbstractJiniTransactionManager extends AbstractPlatformTransactionManager implements
        JiniPlatformTransactionManager, InitializingBean {

    // TransactionManager used for creating the actual transaction
    private transient TransactionManager transactionManager;

    // the jini participant - can be javaspace or any other service that wants
    // to take part in the transaction
    private Object transactionalContext;

    private Long defaultTimeout;

    private Long commitTimeout;

    private Long rollbackTimeout;

    public Object getTransactionalContext() {
        return transactionalContext;
    }

    public void setTransactionalContext(Object txResource) {
        this.transactionalContext = txResource;
    }

    /**
     * Sets the default timeout to use if {@link TransactionDefinition#TIMEOUT_DEFAULT} is used. Set
     * in <b>seconds</b> (in order to follow the {@link TransactionDefinition} contract. Defaults
     * to {@link Lease#FOREVER}.
     */
    public void setDefaultTimeout(Long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }

    /**
     * Sets an optional timeout when performing commit. If not set {@link Transaction#commit()} will
     * be called. If set {@link Transaction#commit(long)} will be called.
     */
    public void setCommitTimeout(Long commitTimeout) {
        this.commitTimeout = commitTimeout;
    }

    /**
     * Sets an optional timeout when performing rollback/abort. If not set
     * {@link Transaction#abort()} will be called. If set {@link Transaction#abort(long)} will be
     * called.
     */
    public void setRollbackTimeout(Long rollbackTimeout) {
        this.rollbackTimeout = rollbackTimeout;
    }

    /**
     * Implemented by sub classes to provide a Jini {@link TransactionManager}.
     */
    protected abstract TransactionManager doCreateTransactionManager() throws Exception;

    public void afterPropertiesSet() throws Exception {
        this.transactionManager = doCreateTransactionManager();
        Assert.notNull(this.transactionManager, "Jini transactionManager is required");
        if (transactionalContext == null) {
            transactionalContext = transactionManager;
        }
        if ((transactionManager instanceof NestableTransactionManager)) {
            setNestedTransactionAllowed(true);
        }
    }

    protected Object doGetTransaction() throws TransactionException {

        JiniTransactionObject txObject = new JiniTransactionObject();
        // txObject.setNestedTransactionAllowed
        // txObject.setJiniHolder(transactionalContext);

        // set the jini holder is one is found
        if (TransactionSynchronizationManager.hasResource(transactionalContext)) {
            JiniTransactionHolder jiniHolder = (JiniTransactionHolder) TransactionSynchronizationManager.getResource(transactionalContext);
            if (logger.isDebugEnabled()) {
                logger.debug("Found thread-bound tx data [" + jiniHolder + "] for Jini resource ["
                        + transactionalContext + "]");
            }
            txObject.setJiniHolder(jiniHolder, false);
        }

        return txObject;
    }

    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        JiniTransactionObject txObject = (JiniTransactionObject) transaction;
        if (logger.isDebugEnabled())
            logger.debug("Beginning transaction [" + txObject + "]");
        try {
            doJiniBegin(txObject, definition);
        } catch (UnsupportedOperationException ex) {
            // assume nested transaction not supported
            throw new NestedTransactionNotSupportedException("Implementation does not ex nested transactions", ex);
        }
    }

    protected void doJiniBegin(JiniTransactionObject txObject, TransactionDefinition definition) {

        // create the tx

        try {
            if (txObject.getJiniHolder() == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Creating new transaction for [" + transactionalContext + "]");
                }
                long timeout = Lease.FOREVER;
                if (defaultTimeout != null) {
                    timeout = defaultTimeout;
                }
                if (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT) {
                    timeout = definition.getTimeout() * 1000;
                }
                Transaction.Created txCreated = TransactionFactory.create(transactionManager, timeout);
                JiniTransactionHolder jiniHolder = new JiniTransactionHolder(txCreated, definition.getIsolationLevel());
                jiniHolder.setTimeoutInSeconds(definition.getTimeout());
                txObject.setJiniHolder(jiniHolder, true);
            }

            txObject.getJiniHolder().setSynchronizedWithTransaction(true);

            applyIsolationLevel(txObject, definition.getIsolationLevel());
            // check for timeout just in case
            // applyTimeout(txObject, definition.getTimeout());

            // Bind the session holder to the thread.
            if (txObject.isNewJiniHolder()) {
                TransactionSynchronizationManager.bindResource(transactionalContext, txObject.getJiniHolder());
            }
        } catch (LeaseDeniedException e) {
            throw new CannotCreateTransactionException("Lease denied", e);
        } catch (RemoteException e) {
            throw new CannotCreateTransactionException("Remote exception", e);
        }

    }

    protected void applyIsolationLevel(JiniTransactionObject txObject, int isolationLevel)
            throws InvalidIsolationLevelException {
        if (isolationLevel != TransactionDefinition.ISOLATION_DEFAULT) {
            throw new InvalidIsolationLevelException("TransactionManager does not support custom isolation levels");
        }
    }

    protected void applyTimeout(JiniTransactionObject txObject, int timeout) throws InvalidTimeoutException {
        // TODO: maybe use a LeaseRenewalManager
        if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
            throw new InvalidTimeoutException("TransactionManager does not ex custom timeouts", timeout);
        }
    }

    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        JiniTransactionObject txObject = (JiniTransactionObject) status.getTransaction();
        if (logger.isDebugEnabled())
            logger.debug("Committing Jini transaction [" + txObject.toString() + "]");
        try {
            if (commitTimeout == null) {
                txObject.getTransaction().commit();
            } else {
                txObject.getTransaction().commit(commitTimeout);
            }
        } catch (UnknownTransactionException e) {
            throw convertJiniException(e);
        } catch (CannotCommitException e) {
            throw convertJiniException(e);
        } catch (RemoteException e) {
            throw convertJiniException(e);
        } catch (TimeoutExpiredException e) {
            throw convertJiniException(e);
        }
    }

    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        JiniTransactionObject txObject = (JiniTransactionObject) transaction;
        return txObject.hasTransaction();
    }

    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        JiniTransactionObject txObject = (JiniTransactionObject) status.getTransaction();
        if (logger.isDebugEnabled())
            logger.debug("Rolling back Jini transaction" + txObject.toString());
        try {
            if (rollbackTimeout == null) {
                txObject.getTransaction().abort();
            } else {
                txObject.getTransaction().abort(rollbackTimeout);
            }
        } catch (UnknownTransactionException e) {
            throw convertJiniException(e);
        } catch (CannotAbortException e) {
            throw convertJiniException(e);
        } catch (RemoteException e) {
            throw convertJiniException(e);
        } catch (TimeoutExpiredException e) {
            throw convertJiniException(e);
        }
    }

    protected void doCleanupAfterCompletion(Object transaction) {
        JiniTransactionObject txObject = (JiniTransactionObject) transaction;
        // Remove the session holder from the thread.
        if (txObject.isNewJiniHolder()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Removing per-thread Jini transaction for [" + getTransactionalContext() + "]");
            }
            TransactionSynchronizationManager.unbindResource(getTransactionalContext());
        }
        txObject.getJiniHolder().clear();
    }

    protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
        JiniTransactionObject txObject = (JiniTransactionObject) status.getTransaction();
        if (status.isDebug()) {
            logger.debug("Setting Jini transaction on txContext [" + getTransactionalContext() + "] rollback-only");
        }
        txObject.setRollbackOnly();
    }

    protected void doResume(Object transaction, Object suspendedResources) throws TransactionException {
        JiniTransactionHolder jiniHolder = (JiniTransactionHolder) suspendedResources;
        TransactionSynchronizationManager.bindResource(getTransactionalContext(), jiniHolder);
    }

    protected Object doSuspend(Object transaction) throws TransactionException {
        JiniTransactionObject txObject = (JiniTransactionObject) transaction;
        txObject.setJiniHolder(null, false);
        return TransactionSynchronizationManager.unbindResource(getTransactionalContext());
    }

    protected boolean useSavepointForNestedTransaction() {
        return false;
    }

    protected RuntimeException convertJiniException(Exception exception) {
        if (exception instanceof LeaseException) {
            return new RemoteAccessException("Lease denied", exception);
        }

        if (exception instanceof TransactionException) {
            return new TransactionSystemException(exception.getMessage(), exception);
        }

        if (exception instanceof RemoteException) {
            // Translate to Spring's unchecked remote access exception
            return new RemoteAccessException("RemoteException", exception);
        }

        if (exception instanceof UnusableEntryException) {
            return new RemoteAccessException("Unusable entry", exception);
        }

        if (exception instanceof RuntimeException) {
            return (RuntimeException) exception;
        }

        if (exception instanceof TimeoutExpiredException) {
            throw new TransactionTimedOutException("Transaction timed out (either the transaction or commit/abort)",
                    exception);
        }

        return new TransactionException("unexpected exception ", exception) {
            private static final long serialVersionUID = -2829436028739682240L;
        };
    }

    /**
     * Jini Transaction object. Used as transaction object by GigaSpaceTransactionManager.
     * 
     * TODO: can SmartTransactionObject be implemented?
     */
    static class JiniTransactionObject {

        private JiniTransactionHolder jiniHolder;

        private boolean newJiniHolder;

        public boolean hasTransaction() {
            return (jiniHolder != null && jiniHolder.hasTransaction());
        }

        public void setJiniHolder(JiniTransactionHolder jiniHolder, boolean newSessionHolder) {
            this.jiniHolder = jiniHolder;
            this.newJiniHolder = newSessionHolder;
        }

        public JiniTransactionHolder getJiniHolder() {
            return jiniHolder;
        }

        public boolean isNewJiniHolder() {
            return newJiniHolder;
        }

        public boolean isRollbackOnly() {
            return (jiniHolder != null && jiniHolder.isRollbackOnly());
        }

        public void setRollbackOnly() {
            if (jiniHolder != null) {
                jiniHolder.setRollbackOnly();
            }
        }

        public Transaction getTransaction() {
            if (hasTransaction()) {
                return jiniHolder.getTxCreated().transaction;
            }
            return null;
        }

    }

}
