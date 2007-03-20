package org.openspaces.events;

import org.springframework.dao.DataAccessException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.Assert;

/**
 * A simple based class for {@link SpaceDataEventListener} based containers. Allowing to register a
 * listener and provides several support methods like
 * {@link #invokeListener(Object, org.springframework.transaction.TransactionStatus, Object)} in
 * order to simplify event listener based containers.
 * 
 * @author kimchy
 */
public abstract class AbstractEventListenerContainer extends AbstractSpaceListeningContainer {

    private SpaceDataEventListener eventListener;

    /**
     * Sets the event listener implementation that will be used to delegate events to. Also see
     * different adapter classes provided for simpler event listeners integration.
     * 
     * @param eventListener
     *            The event listener used
     */
    public void setEventListener(SpaceDataEventListener eventListener) {
        this.eventListener = eventListener;
    }

    protected SpaceDataEventListener getEventListener() {
        return eventListener;
    }

    /**
     * Validates that the {@link #setEventListener(SpaceDataEventListener)} property is set.
     */
    protected void validateConfiguration() {
        super.validateConfiguration();
        Assert.notNull(eventListener, "eventListener must be specified");
    }

    // -------------------------------------------------------------------------
    // Template methods for listener execution
    // -------------------------------------------------------------------------

    /**
     * Executes the given listener if the container is running ({@link #isRunning()}.
     * 
     * @param eventData
     *            The event data object
     * @param txStatus
     *            An optional transaction status allowing to rollback a transaction programmatically
     * @param source
     *            An optional source (or additional event information)
     */
    protected void executeListener(Object eventData, TransactionStatus txStatus, Object source)
            throws DataAccessException {
        if (!isRunning()) {
            return;
        }
        invokeListener(eventData, txStatus, source);
    }

    /**
     * Invokes the configured {@link org.openspaces.events.SpaceDataEventListener} basde on the
     * provided data. Currently simply delegates to
     * {@link org.openspaces.events.SpaceDataEventListener#onEvent(Object,org.openspaces.core.GigaSpace,org.springframework.transaction.TransactionStatus,Object)}.
     * 
     * @param eventData
     *            The event data object
     * @param txStatus
     *            An optional transaction status allowing to rollback a transaction programmatically
     * @param source
     *            An optional source (or additional event information)
     * @throws DataAccessException
     */
    protected void invokeListener(Object eventData, TransactionStatus txStatus, Object source)
            throws DataAccessException {
        eventListener.onEvent(eventData, getGigaSpace(), txStatus, source);
    }

    /**
     * Handles exception that occurs during the event listening process. Currently simply logs it.
     * 
     * @param ex
     *            the exception to handle
     */
    protected void handleListenerException(Throwable ex) {
        if (ex instanceof Exception) {
            invokeExceptionListener((Exception) ex);
        }
        if (isActive()) {
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
}
