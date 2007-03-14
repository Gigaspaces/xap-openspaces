package org.openspaces.events.polling.receive;

import org.openspaces.core.GigaSpace;
import org.springframework.dao.DataAccessException;

/**
 * First tries and perform a {@link org.openspaces.core.GigaSpace#takeMultiple(Object,int)} using
 * the provided template and configured maxEntries (defaults to <code>50</code>). If no values
 * are returned, will perform a blocking take operation using
 * {@link org.openspaces.core.GigaSpace#take(Object,long)}.
 * 
 * @author kimchy
 */
public class MultiTakeReceiveOperationHandler implements ReceiveOperationHandler {

    private static final int DEFAULT_MAX_ENTRIES = 50;

    private int maxEntries = DEFAULT_MAX_ENTRIES;

    /**
     * Sets the max entries the inital take multiple operation will perform.
     */
    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    /**
     * First tries and perform a {@link org.openspaces.core.GigaSpace#takeMultiple(Object,int)}
     * using the provided template and configured maxEntries (defaults to <code>50</code>). If no
     * values are returned, will perform a blocking take operation using
     * {@link org.openspaces.core.GigaSpace#take(Object,long)}.
     */
    public Object receive(Object template, GigaSpace gigaSpace, long receiveTimeout) throws DataAccessException {
        Object[] results = gigaSpace.takeMultiple(template, maxEntries);
        if (results != null && results.length > 0) {
            return results;
        }
        return gigaSpace.take(template, receiveTimeout);
    }
}
