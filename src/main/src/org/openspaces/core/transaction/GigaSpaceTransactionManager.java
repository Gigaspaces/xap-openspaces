package org.openspaces.core.transaction;

import com.j_spaces.core.client.LocalTransactionManager;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.lease.LeaseException;
import net.jini.core.transaction.CannotAbortException;
import net.jini.core.transaction.CannotCommitException;
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
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
// TODO Check if local transaction supports nested transactions, and if not, why not?
// TODO Need to support transaction timeout    
public class GigaSpaceTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean {

    // TransactionManager used for creating the actual transaction
    private transient TransactionManager transactionManager;

    // the jini participant - can be javaspace or any other service that wants
    // to take part in the transaction
    private Object transactionalContext;

    public GigaSpaceTransactionManager() {

    }

    public GigaSpaceTransactionManager(TransactionManager transactionManager) {
        this(transactionManager, transactionManager);
    }

    public GigaSpaceTransactionManager(TransactionManager transactionManager, Object transactionalContext) {
        this.transactionManager = transactionManager;
        this.transactionalContext = transactionalContext;
        afterPropertiesSet();
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public Object getTransactionalContext() {
        return transactionalContext;
    }

    public void setTransactionalContext(Object txResource) {
        this.transactionalContext = txResource;
    }

    public void afterPropertiesSet() {
        Assert.notNull(transactionManager, "transactionManager property is required");
        if (transactionalContext == null) {
            transactionalContext = transactionManager;
        }
        if ((transactionManager instanceof NestableTransactionManager)) {
            setNestedTransactionAllowed(true);
        }
    }

    protected Object doGetTransaction() throws TransactionException {

        GsTransactionObject txObject = new GsTransactionObject();
        // txObject.setNestedTransactionAllowed
        // txObject.setJiniHolder(transactionalContext);

        // set the jini holder is one is found
        if (TransactionSynchronizationManager.hasResource(transactionalContext)) {
            JiniHolder jiniHolder = (JiniHolder) TransactionSynchronizationManager.getResource(transactionalContext);
            if (logger.isDebugEnabled()) {
                logger.debug("Found thread-bound tx data [" + jiniHolder + "] for Jini resource [" + transactionalContext + "]");
            }
            txObject.setJiniHolder(jiniHolder, false);
        }

        return txObject;
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

        return new TransactionException("unexpected exception ", exception) {
        };
    }

    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        GsTransactionObject txObject = (GsTransactionObject) transaction;
        if (logger.isDebugEnabled())
            logger.debug("Beginning transaction [" + txObject + "]");
        try {
            doJiniBegin(txObject, definition);
        } catch (UnsupportedOperationException ex) {
            // assume nested transaction not supported
            throw new NestedTransactionNotSupportedException("Implementation does not ex nested transactions", ex);
        }
    }

    protected void doJiniBegin(GsTransactionObject txObject, TransactionDefinition definition) {

        // create the tx

        try {
            if (txObject.getJiniHolder() == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Creating new transaction for [" + transactionalContext + "]");
                }
                Transaction.Created txCreated = TransactionFactory.create(transactionManager, definition.getTimeout());
                JiniHolder jiniHolder = new JiniHolder(txCreated);
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

    protected void applyIsolationLevel(GsTransactionObject txObject, int isolationLevel) throws InvalidIsolationLevelException {
        if (isolationLevel != TransactionDefinition.ISOLATION_DEFAULT) {
            throw new InvalidIsolationLevelException("GigaSpaceTransactionManager does not ex custom isolation levels");
        }
    }

    protected void applyTimeout(GsTransactionObject txObject, int timeout) throws InvalidTimeoutException {
        // TODO: maybe use a LeaseRenewalManager
        if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
            throw new InvalidTimeoutException("GigaSpaceTransactionManager does not ex custom timeouts", timeout);
        }
    }

    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        GsTransactionObject txObject = (GsTransactionObject) status.getTransaction();
        if (logger.isDebugEnabled())
            logger.debug("Committing Jini transaction [" + txObject.toString() + "]");
        try {
            txObject.getTransaction().commit();
        } catch (UnknownTransactionException e) {
            throw convertJiniException(e);
        } catch (CannotCommitException e) {
            throw convertJiniException(e);
        } catch (RemoteException e) {
            throw convertJiniException(e);
        }
    }

    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        GsTransactionObject txObject = (GsTransactionObject) transaction;
        return txObject.hasTransaction();
    }

    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        GsTransactionObject txObject = (GsTransactionObject) status.getTransaction();
        if (logger.isDebugEnabled())
            logger.debug("Rolling back Jini transaction" + txObject.toString());
        try {
            txObject.getTransaction().abort();
        } catch (UnknownTransactionException e) {
            throw convertJiniException(e);
        } catch (CannotAbortException e) {
            throw convertJiniException(e);
        } catch (RemoteException e) {
            throw convertJiniException(e);
        }
    }

    protected void doCleanupAfterCompletion(Object transaction) {
        GsTransactionObject txObject = (GsTransactionObject) transaction;
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
        GsTransactionObject txObject = (GsTransactionObject) status.getTransaction();
        if (status.isDebug()) {
            logger.debug("Setting Jini transaction on txContext [" + getTransactionalContext() + "] rollback-only");
        }
        txObject.setRollbackOnly();
    }

    protected void doResume(Object transaction, Object suspendedResources) throws TransactionException {
        JiniHolder jiniHolder = (JiniHolder) suspendedResources;
        TransactionSynchronizationManager.bindResource(getTransactionalContext(), jiniHolder);
    }

    protected Object doSuspend(Object transaction) throws TransactionException {
        GsTransactionObject txObject = (GsTransactionObject) transaction;
        txObject.setJiniHolder(null, false);
        return TransactionSynchronizationManager.unbindResource(getTransactionalContext());
    }

    protected boolean useSavepointForNestedTransaction() {
        return false;
    }

    /**
     * Jini Transaction object. Used as transaction object by
     * GigaSpaceTransactionManager.
     *
     * TODO: can SmartTransactionObject be implemented?
     */
    private static class GsTransactionObject {

        private JiniHolder jiniHolder;

        private boolean newJiniHolder;

        public boolean hasTransaction() {
            return (jiniHolder != null && jiniHolder.hasTransaction());
        }

        public void setJiniHolder(JiniHolder jiniHolder, boolean newSessionHolder) {
            this.jiniHolder = jiniHolder;
            this.newJiniHolder = newSessionHolder;
        }

        public JiniHolder getJiniHolder() {
            return jiniHolder;
        }

        public boolean isNewJiniHolder() {
            return newJiniHolder;
        }

        public boolean isRollbackOnly() {
            return (jiniHolder != null && jiniHolder.isRollbackOnly());
        }

        public void setRollbackOnly() {
            if (jiniHolder != null)
                jiniHolder.setRollbackOnly();
        }

        public Transaction getTransaction() {
            if (hasTransaction())
                return jiniHolder.txCreated.transaction;
            return null;
        }

    }

    // is ResourceHolder really required
    public static class JiniHolder extends ResourceHolderSupport {
        private Transaction.Created txCreated;

        public JiniHolder(Transaction.Created txCreated) {
            this.txCreated = txCreated;
        }

        /**
         * @return Returns the txCreated.
         */
        public Transaction.Created getTxCreated() {
            return txCreated;
        }

        public boolean hasTransaction() {
            return (txCreated != null && txCreated.transaction != null);
        }

    }
}
