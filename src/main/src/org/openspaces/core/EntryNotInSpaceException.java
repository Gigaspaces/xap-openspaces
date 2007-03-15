package org.openspaces.core;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * This exception is thrown when <code>update</code>, <code>readIfExist</code> or
 * <code>takeIfExist</code> operations are rejected. The entry specified by the UID is not in the
 * space - it was not found or has been deleted. Wraps
 * {@link com.j_spaces.core.client.EntryNotInSpaceException}.
 *
 * @author kimchy
 */
public class EntryNotInSpaceException extends DataRetrievalFailureException {

    private static final long serialVersionUID = 1654923353943041796L;

    private com.j_spaces.core.client.EntryNotInSpaceException e;

    public EntryNotInSpaceException(com.j_spaces.core.client.EntryNotInSpaceException e) {
        super(e.getMessage(), e);
        this.e = e;
    }

    /**
     * Returns Entry UID.
     *
     * @return unique ID of the entry that caused this exception
     * @see com.j_spaces.core.client.EntryNotInSpaceException#getUID()
     */
    public String getUID() {
        return e.getUID();
    }

    /**
     * Check if deleted in the same transaction.
     *
     * @return <code>true</code> if deleted by the same transaction
     * @see com.j_spaces.core.client.EntryNotInSpaceException#isDeletedByOwnTxn()
     */
    public boolean isDeletedByOwnTxn() {
        return e.isDeletedByOwnTxn();
    }
}
