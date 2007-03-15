package org.openspaces.core;

import com.j_spaces.core.client.EntryVersionConflictException;
import org.springframework.dao.OptimisticLockingFailureException;

/**
 * This exception is thrown when update/take operation is rejected
 * as a result of optimistic locking version conflict. Wraps
 * {@link com.j_spaces.core.client.EntryVersionConflictException}.
 *
 * @author kimchy
 */
public class SpaceOptimisticLockingFailureException extends OptimisticLockingFailureException {

    private EntryVersionConflictException e;

    public SpaceOptimisticLockingFailureException(EntryVersionConflictException e) {
        super(e.getMessage(), e);
        this.e = e;
    }

    /**
     * Returns the entry UID.
     *
     * @see com.j_spaces.core.client.EntryVersionConflictException#getUID()
     */
    public String getUID() {
        return e.getUID();
    }

    /**
     * Returns the entry Space Version ID.
     *
     * @see com.j_spaces.core.client.EntryVersionConflictException#getSpaceVersionID()
     */
    public int getSpaceVersionID() {
        return e.getSpaceVersionID();
    }

    /**
     * Returns the entry client Version ID
     *
     * @see com.j_spaces.core.client.EntryVersionConflictException#getClientVersionID()
     */
    public int getClientVersionID() {
        return e.getClientVersionID();
    }

    /**
     * Return the space operation caused the conflict Take or Update
     *
     * @see com.j_spaces.core.client.EntryVersionConflictException#getOperation()
     */
    public String getOperation() {
        return e.getOperation();
    }

}
