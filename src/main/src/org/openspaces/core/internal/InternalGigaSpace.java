package org.openspaces.core.internal;

import com.gigaspaces.async.AsyncFuture;
import net.jini.core.transaction.Transaction;
import org.openspaces.core.GigaSpace;

import java.util.concurrent.ExecutorService;

/**
 * An intenral API of {@link org.openspaces.core.GigaSpace}
 *
 * @author kimchy
 */
public interface InternalGigaSpace extends GigaSpace {

    /**
     * Returns the intenral thread pool for async based invocations.
     */
    ExecutorService getAsyncExecutorService();

    <T> AsyncFuture<T> wrapFuture(AsyncFuture<T> future, Transaction tx);
}
