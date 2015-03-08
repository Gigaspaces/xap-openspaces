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

package org.openspaces.events;

import com.gigaspaces.metrics.LongCounter;
import org.openspaces.core.transaction.manager.JiniPlatformTransactionManager;
import org.openspaces.events.adapter.EventListenerAdapter;
import org.openspaces.events.support.AnnotationProcessorUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A simple based class for {@link SpaceDataEventListener} based containers. Allowing to register a
 * listener and provides several support methods like
 * {@link #invokeListener(SpaceDataEventListener, Object, org.springframework.transaction.TransactionStatus, Object)} in
 * order to simplify event listener based containers.
 *
 * @author kimchy
 */
public abstract class AbstractEventListenerContainer extends AbstractSpaceListeningContainer implements ApplicationContextAware {

    private SpaceDataEventListener eventListener;

    private String eventListenerRef;

    private ApplicationContext applicationContext;

    protected EventExceptionHandler exceptionHandler;

    private final LongCounter processedEvents = new LongCounter();

    private final LongCounter failedEvents = new LongCounter();

    private Object template;
    private boolean performSnapshot = true; // enabled by default
    private Object receiveTemplate;
    private DynamicEventTemplateProvider dynamicTemplate;
    private Object dynamicTemplateRef;

    private PlatformTransactionManager transactionManager;
    private DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
    protected boolean disableTransactionValidation = false;

    protected EventExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    /**
     * Sets an exception handler that will be invoked when an exception occurs on the listener allowing to
     * customize the handling of such cases.
     */
    public void setExceptionHandler(EventExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Sets the event listener implementation that will be used to delegate events to. Also see
     * different adapter classes provided for simpler event listeners integration.
     *
     * @param eventListener The event listener used
     */
    public void setEventListener(SpaceDataEventListener eventListener) {
        this.eventListener = eventListener;
    }

    /**
     * Sets an event listener bean reference name that will be used to lookup the actual listener
     * bean (based on its name). Mainly used when configuring a listener with specific scope setting
     * (such as prototype) allowing to scope to take affect by using <code>getBean</code> for each
     * request of the listener.
     */
    public void setEventListenerRef(String eventListenerRef) {
        this.eventListenerRef = eventListenerRef;
    }

    protected SpaceDataEventListener getEventListener() {
        if (eventListener != null) {
            return eventListener;
        }
        if (eventListenerRef == null) {
            return null;
        }
        return (SpaceDataEventListener) applicationContext.getBean(eventListenerRef);
    }

    protected Object getActualEventListener() {
        Object listener = getEventListener();
        while (listener instanceof EventListenerAdapter) {
            listener = ((EventListenerAdapter) listener).getActualEventListener();
        }
        return listener;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    protected Class<?> getEventListenerClass() {
        if (eventListener != null) {
            return eventListener.getClass();
        }
        return applicationContext.getType(eventListenerRef);
    }

    @Override
    public void initialize() throws DataAccessException {
        initializeTransactionManager();
        initializeTemplate();
        initializeExceptionHandler();
        super.initialize();
    }

    private void initializeTransactionManager() {
        // Use bean name as default transaction name.
        if (this.transactionDefinition.getName() == null) {
            this.transactionDefinition.setName(getBeanName());
        }
    }

    @Override
    protected void validateConfiguration() {
        super.validateConfiguration();
        if (transactionManager != null && !disableTransactionValidation) {
            if (!getGigaSpace().getTxProvider().isEnabled()) {
                throw new IllegalStateException(message("event container is configured to run under transactions (transaction manager is provided) " +
                        "but GigaSpace is not transactional. Please pass the transaction manager to the GigaSpace bean as well"));
            }
        }
    }

    private void initializeTemplate() {
        Object possibleTemplateProvider = null;

        if (template != null) {
            // check if template object is actually a template provider
            possibleTemplateProvider = template;
        }
        else {
            Class<?> eventListenerType = getEventListenerClass();
            if (eventListenerType != null) {
                //check if listener object is also a template provider
                possibleTemplateProvider = getActualEventListener();
            }
        }

        if (possibleTemplateProvider != null) {
            Object templateFromProvider = AnnotationProcessorUtils.findTemplateFromProvider(possibleTemplateProvider);
            if (templateFromProvider != null) {
                setTemplate(templateFromProvider);
            }
        }

        if (dynamicTemplate == null && dynamicTemplateRef != null) {
            Object dynamicTemplateProviderBean = dynamicTemplateRef;
            dynamicTemplate = AnnotationProcessorUtils.findDynamicEventTemplateProvider(dynamicTemplateProviderBean);
            if (dynamicTemplate == null) {
                throw new IllegalArgumentException("Cannot find dynamic template provider in " + dynamicTemplateRef.getClass());
            }
        }

        if (template != null && dynamicTemplate != null) {
            throw new IllegalArgumentException("dynamicTemplate and template are mutually exclusive.");
        }

        if (performSnapshot && template != null) {
            if (logger.isTraceEnabled()) {
                logger.trace(message("Performing snapshot on template [" + template + "]"));
            }
            receiveTemplate = getGigaSpace().prepareTemplate(template);
        } else {
            receiveTemplate = template;
        }
    }

    private void initializeExceptionHandler() {
        if (exceptionHandler == null && getActualEventListener() != null) {
            final AtomicReference<Method> ref = new AtomicReference<Method>();
            ReflectionUtils.doWithMethods(AopUtils.getTargetClass(getActualEventListener()), new ReflectionUtils.MethodCallback() {
                public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                    if (method.isAnnotationPresent(ExceptionHandler.class)) {
                        ref.set(method);
                    }
                }
            });
            if (ref.get() != null) {
                ref.get().setAccessible(true);
                try {
                    setExceptionHandler((EventExceptionHandler) ref.get().invoke(getActualEventListener()));
                } catch (Exception e) {
                    throw new IllegalArgumentException("Failed to set EventExceptionHandler from method [" + ref.get().getName() + "]", e);
                }
            }
        }
    }

    /**
     * Only start if we have a listener registered. If we don't, then
     * explicit start should be called.
     */
    @Override
    protected void doStart() throws DataAccessException {
        if (getEventListener() != null) {
            super.doStart();
        }
    }

    // -------------------------------------------------------------------------
    // Template methods for listener execution
    // -------------------------------------------------------------------------

    /**
     * Executes the given listener if the container is running ({@link #isRunning()}.
     *
     * @param eventData The event data object
     * @param txStatus  An optional transaction status allowing to rollback a transaction programmatically
     * @param source    An optional source (or additional event information)
     */
    protected void executeListener(SpaceDataEventListener eventListener, Object eventData, TransactionStatus txStatus, Object source) throws Throwable {
        if (!isRunning()) {
            return;
        }
        invokeListener(eventListener, eventData, txStatus, source);
    }

    /**
     * Invokes the configured {@link org.openspaces.events.SpaceDataEventListener} based on the
     * provided data. Currently simply delegates to
     * {@link org.openspaces.events.SpaceDataEventListener#onEvent(Object,org.openspaces.core.GigaSpace,org.springframework.transaction.TransactionStatus,Object)}.
     *
     * @param eventData The event data object
     * @param txStatus  An optional transaction status allowing to rollback a transaction programmatically
     * @param source    An optional source (or additional event information)
     * @throws DataAccessException
     */
    protected void invokeListener(SpaceDataEventListener eventListener, Object eventData, TransactionStatus txStatus, Object source) throws Throwable {
        if (exceptionHandler != null) {
            try {
                eventListener.onEvent(eventData, getGigaSpace(), txStatus, source);
                exceptionHandler.onSuccess(eventData, getGigaSpace(), txStatus, source);
            } catch (Throwable e) {
                if (!(e instanceof ListenerExecutionFailedException)) {
                    e = new ListenerExecutionFailedException(e.getMessage(), e);
                }
                exceptionHandler.onException((ListenerExecutionFailedException) e, eventData, getGigaSpace(), txStatus, source);
            }
        } else {
            eventListener.onEvent(eventData, getGigaSpace(), txStatus, source);
        }
        processedEvents.inc();
    }

    /**
     * Handles exception that occurs during the event listening process. Currently simply logs it.
     *
     * @param ex the exception to handle
     */
    protected void handleListenerException(Throwable ex) {
        if (ex instanceof Exception) {
            invokeExceptionListener((Exception) ex);
        }
        if (isActive()) {
            failedEvents.inc();
            // Regular case: failed while active. Log at error level.
            logger.error(message("Execution of event listener failed"), ex);
        } else {
            // Rare case: listener thread failed after container shutdown.
            // Log at debug level, to avoid spamming the shutdown log.
            logger.debug(message("Listener exception after container shutdown"), ex);
        }
    }

    /**
     * A callback to handle exception. Possible extension point for registered exception listeners.
     */
    protected void invokeExceptionListener(Exception e) {
    }

    public long getProcessedEvents() {
        return processedEvents.getCount();
    }

    public long getFailedEvents() {
        return failedEvents.getCount();
    }

    /**
     * Sets the specified template to be used with the polling space operation.
     *
     * @see org.openspaces.core.GigaSpace#take(Object,long)
     */
    public void setTemplate(Object template) {
        this.template = template;
    }

    /**
     * Returns the template that will be used. Note, in order to perform receive operations, the
     * {@link #getReceiveTemplate()} should be used.
     */
    protected Object getTemplate() {
        return this.template;
    }

    /**
     * Called before each take and read polling operation to change the template
     * Overrides any template defined with {@link #setTemplate(Object)}
     * @param templateProvider -
     *      An object that implements {@link DynamicEventTemplateProvider}
     *      or has a method annotated with {@link DynamicEventTemplateProvider}
     */
    public void setDynamicTemplate(Object dynamicTemplate) {
        this.dynamicTemplateRef = dynamicTemplate;
    }

    /**
     * Returns whether dynamic template is configured
     */
    protected boolean isDynamicTemplate(){
        return dynamicTemplate != null;
    }

    /**
     * If set to <code>true</code> will perform snapshot operation on the provided template
     * before invoking registering as an event listener.
     *
     * @see org.openspaces.core.GigaSpace#snapshot(Object)
     */
    public void setPerformSnapshot(boolean performSnapshot) {
        this.performSnapshot = performSnapshot;
    }

    protected boolean isPerformSnapshot() {
        return this.performSnapshot;
    }

    /**
     * Returns the template to be used for receive operations. If
     * {@link #setPerformSnapshot(boolean)} is set to <code>true</code> (the default)
     * will return the snapshot of the provided template.
     */
    protected Object getReceiveTemplate() {

        if (dynamicTemplate != null) {
            return dynamicTemplate.getDynamicTemplate();
        }

        return receiveTemplate;
    }

    /**
     * Specify the Spring {@link org.springframework.transaction.PlatformTransactionManager} to use
     * for transactional wrapping of listener execution.
     *
     * <p>
     * Default is none, not performing any transactional wrapping.
     */
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /**
     * Return the Spring PlatformTransactionManager to use for transactional wrapping of message
     * reception plus listener execution.
     */
    protected final PlatformTransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    /**
     * Specify the transaction name to use for transactional wrapping. Default is the bean name of
     * this listener container, if any.
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
     */
    public void setTransactionTimeout(int transactionTimeout) {
        this.transactionDefinition.setTimeout(transactionTimeout);
    }

    /**
     * Specify the transaction isolation to use for transactional wrapping.
     *
     * @see org.springframework.transaction.support.DefaultTransactionDefinition#setIsolationLevel(int)
     */
    public void setTransactionIsolationLevel(int transactionIsolationLevel) {
        this.transactionDefinition.setIsolationLevel(transactionIsolationLevel);
    }

    /**
     * Specify the transaction isolation to use for transactional wrapping.
     *
     * @see org.springframework.transaction.support.DefaultTransactionDefinition#setIsolationLevelName(String)
     */
    public void setTransactionIsolationLevelName(String transactionIsolationLevelName) {
        this.transactionDefinition.setIsolationLevelName(transactionIsolationLevelName);
    }

    protected DefaultTransactionDefinition getTransactionDefinition() {
        return transactionDefinition;
    }

    /**
     * Should transaction validation be enabled or not (verify and fail if transaction manager is
     * provided and the GigaSpace is not transactional). Default to <code>false</code>.
     */
    public void setDisableTransactionValidation(boolean disableTransactionValidation) {
        this.disableTransactionValidation = disableTransactionValidation;
    }

    public String getTransactionManagerName() {
        if (transactionManager instanceof JiniPlatformTransactionManager) {
            return ((JiniPlatformTransactionManager) transactionManager).getBeanName();
        }
        if (transactionManager != null) {
            return "<<unknown>>";
        }
        return null;
    }

    protected boolean isTransactional() {
        return transactionManager != null;
    }

}
