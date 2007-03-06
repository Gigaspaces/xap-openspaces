package org.openspaces.events.polling;

import org.openspaces.core.GigaSpaceException;
import org.openspaces.events.AbstractEventListenerContainer;
import org.openspaces.events.EventTemplateProvider;
import org.openspaces.events.polling.receive.ReceiveOperationHandler;
import org.openspaces.events.polling.receive.SingleTakeReceiveOperationHandler;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.Assert;

/**
 * <p>Base class for listener container implementations which are based on polling.
 * Provides support for listener handling based on Space take operations.
 *
 * <p>This listener container variant is built for repeated polling attempts,
 * each invoking the {@link #receiveAndExecute} method. The receive timeout for each
 * attempt can be configured through the {@link #setReceiveTimeout "receiveTimeout"} property.
 *
 * <p>The container allows to set the template object used for the operations. Note, this
 * can be a Pojo based template, or one of GigaSpace's query classes such as
 * {@link com.j_spaces.core.client.SQLQuery}.
 *
 * <p>A pluggable recieve operation handler can be provided by setting
 * {@link #setReceiveOperationHandler(org.openspaces.events.polling.receive.ReceiveOperationHandler)}.
 * The default handler used it {@link org.openspaces.events.polling.receive.SingleTakeReceiveOperationHandler}.
 *
 * <p>Event reception and listener execution can automatically be wrapped
 * in transactions through passing a Spring {@link org.springframework.transaction.PlatformTransactionManager}
 * into the {@link #setTransactionManager "transactionManager"} property. This will usually
 * be a {@link org.openspaces.core.transaction.manager.LocalJiniTransactionManager}.
 *
 * <p>This base class does not assume any specific mechanism for asynchronous
 * execution of polling invokers. Check out {@link SimplePollingEventListenerContainer}
 * for a concrete implementation which is based on Spring's
 * {@link org.springframework.core.task.TaskExecutor} abstraction,
 * including dynamic scaling of concurrent consumers and automatic self recovery.
 *
 * <p>The {@link #setTemplate(Object)} parameter is required in order to perform matching on which
 * events to receive. If the {@link #setEventListener(org.openspaces.events.SpaceDataEventListener)}
 * implements {@link org.openspaces.events.EventTemplateProvider} and the template is directly set,
 * the event listener will be used to get the template. This feature helps when event listeners
 * directly can only work with a certain template and removes the requirement of configuring the
 * template as well.
 *
 * @author kimchy
 */
public abstract class AbstractPollingEventListenerContainer extends AbstractEventListenerContainer {

    /**
     * The default receive timeout: 1000 ms = 1 second.
     */
    public static final long DEFAULT_RECEIVE_TIMEOUT = 1000;

    private Object template;

    private PlatformTransactionManager transactionManager;

    private DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();

    private long receiveTimeout = DEFAULT_RECEIVE_TIMEOUT;

    private ReceiveOperationHandler receiveOperationHandler = new SingleTakeReceiveOperationHandler();


    /**
     * Sets the specified template to be used with the polling space operation.
     *
     * @see org.openspaces.core.GigaSpace#take(Object,long)
     */
    public void setTemplate(Object template) {
        this.template = template;
    }

    protected Object getTemplate() {
        return template;
    }

    /**
     * <p>Specify the Spring {@link org.springframework.transaction.PlatformTransactionManager}
     * to use for transactional wrapping of event reception plus listener execution.
     *
     * <p>Default is none, not performing any transactional wrapping.
     */
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * Return the Spring PlatformTransactionManager to use for transactional
     * wrapping of message reception plus listener execution.
     */
    protected final PlatformTransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    /**
     * Specify the transaction name to use for transactional wrapping.
     * Default is the bean name of this listener container, if any.
     *
     * @see org.springframework.transaction.TransactionDefinition#getName()
     */
    public void setTransactionName(String transactionName) {
        this.transactionDefinition.setName(transactionName);
    }

    /**
     * Specify the transaction timeout to use for transactional wrapping, in <b>seconds</b>.
     * Default is none, using the transaction manager's default timeout.
     *
     * @see org.springframework.transaction.TransactionDefinition#getTimeout()
     * @see #setReceiveTimeout
     */
    public void setTransactionTimeout(int transactionTimeout) {
        this.transactionDefinition.setTimeout(transactionTimeout);
    }

    /**
     * Specify the transaciton isolation to use for transactional wrapping.
     *
     * @see org.springframework.transaction.support.DefaultTransactionDefinition#setIsolationLevel(int)
     */
    public void setTransactionIsolationLevel(int transactionIsolationLevel) {
        this.transactionDefinition.setIsolationLevel(transactionIsolationLevel);
    }

    /**
     * Specify the transaciton isolation to use for transactional wrapping.
     *
     * @see org.springframework.transaction.support.DefaultTransactionDefinition#setIsolationLevelName(String) 
     */
    public void setTransactionIsolationLevelName(String transactionIsolationLevelName) {
        this.transactionDefinition.setIsolationLevelName(transactionIsolationLevelName);
    }

    /**
     * <p>Set the timeout to use for receive calls, in <b>milliseconds</b>.
     * The default is 1000 ms, that is, 1 second.
     *
     * <p><b>NOTE:</b> This value needs to be smaller than the transaction
     * timeout used by the transaction manager (in the appropriate unit,
     * of course).
     *
     * @see org.openspaces.core.GigaSpace#take(Object,long)
     */
    public void setReceiveTimeout(long receiveTimeout) {
        this.receiveTimeout = receiveTimeout;
    }


    /**
     * Returns the timeout used for receive calls, in <b>millisecond</b>.
     * The default is 1000 ms, that is, 1 second.
     */
    protected long getReceiveTimeout() {
        return receiveTimeout;
    }

    /**
     * Allows to set a receive operation handler that will perform the actual receive operation.
     * Defaults to {@link org.openspaces.events.polling.receive.SingleTakeReceiveOperationHandler}.
     */
    public void setReceiveOperationHandler(ReceiveOperationHandler receiveOperationHandler) {
        this.receiveOperationHandler = receiveOperationHandler;
    }

    public void afterPropertiesSet() {
        if (getEventListener() != null && getEventListener() instanceof EventTemplateProvider && template == null) {
            setTemplate(((EventTemplateProvider) getEventListener()).getTemplate());
        }
        super.afterPropertiesSet();
    }

    public void initialize() {
        // Use bean name as default transaction name.
        if (this.transactionDefinition.getName() == null) {
            this.transactionDefinition.setName(getBeanName());
        }

        // Proceed with superclass initialization.
        super.initialize();
    }


    protected void validateConfiguration() {
        super.validateConfiguration();
        Assert.isTrue(receiveTimeout >= 0, "receiveTimeout must have a non negative value");
        Assert.notNull(template, "template property is required");
    }

    /**
     * Execute the listener for a message received from the given consumer,
     * wrapping the entire operation in an external transaction if demanded.
     *
     * @see #doReceiveAndExecute
     */
    protected boolean receiveAndExecute() throws GigaSpaceException {
        if (this.transactionManager != null) {
            // Execute receive within transaction.
            TransactionStatus status = this.transactionManager.getTransaction(this.transactionDefinition);
            boolean messageReceived;
            try {
                messageReceived = doReceiveAndExecute(status);
            } catch (RuntimeException ex) {
                rollbackOnException(status, ex);
                throw ex;
            } catch (Error err) {
                rollbackOnException(status, err);
                throw err;
            }
            this.transactionManager.commit(status);
            return messageReceived;
        } else {
            // Execute receive outside of transaction.
            return doReceiveAndExecute(null);
        }
    }

    protected boolean doReceiveAndExecute(TransactionStatus status) {
        Object dataEvent = receiveEvent();
        if (dataEvent != null) {
            if (dataEvent.getClass().isArray()) {
                Object[] dataEvents = (Object[]) dataEvent;
                for (int i = 0; i < dataEvents.length; i++) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Received event [" + dataEvent + "]");
                    }
                    eventReceived(dataEvents[i]);
                    try {
                        invokeListener(dataEvents[i], status, null);
                    } catch (Throwable ex) {
                        if (status != null) {
                            // in case of an exception, we rollback the transaction and return (since we rolled back)
                            if (logger.isDebugEnabled()) {
                                logger.debug("Rolling back transaction because of listener exception thrown: " + ex);
                            }
                            status.setRollbackOnly();
                            handleListenerException(ex);
                            return true;
                        } else {
                            // in case we do not work within a transaction, just handle the exception and continue
                            handleListenerException(ex);
                        }
                    }
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Received event [" + dataEvent + "]");
                }
                eventReceived(dataEvent);
                try {
                    invokeListener(dataEvent, status, null);
                } catch (Throwable ex) {
                    if (status != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Rolling back transaction because of listener exception thrown: " + ex);
                        }
                        status.setRollbackOnly();
                    }
                    handleListenerException(ex);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Perform a rollback, handling rollback exceptions properly.
     *
     * @param status object representing the transaction
     * @param ex     the thrown application exception or error
     */
    private void rollbackOnException(TransactionStatus status, Throwable ex) {
        logger.debug("Initiating transaction rollback on application exception", ex);
        try {
            this.transactionManager.rollback(status);
        } catch (RuntimeException ex2) {
            logger.error("Application exception overridden by rollback exception", ex);
            throw ex2;
        } catch (Error err) {
            logger.error("Application exception overridden by rollback error", ex);
            throw err;
        }
    }

    /**
     * Receive an event
     */
    protected Object receiveEvent() throws GigaSpaceException {
        return receiveOperationHandler.receive(getTemplate(), getGigaSpace(), getReceiveTimeout());
    }

    /**
     * Template method that gets called right when a new message has been received,
     * before attempting to process it. Allows subclasses to react to the event
     * of an actual incoming message, for example adapting their consumer count.
     */
    protected void eventReceived(Object event) {
    }

}
