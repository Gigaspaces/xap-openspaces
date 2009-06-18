package org.openspaces.core.internal;

import com.gigaspaces.async.AsyncFuture;
import net.jini.core.transaction.Transaction;
import org.openspaces.core.GigaSpace;

/**
 * An internal API of {@link org.openspaces.core.GigaSpace}
 *
 * @author kimchy
 */
public interface InternalGigaSpace extends GigaSpace {

    <T> AsyncFuture<T> wrapFuture(AsyncFuture<T> future, Transaction tx);
}
