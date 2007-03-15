package org.openspaces.core;

import com.j_spaces.core.client.OperationTimeoutException;
import org.springframework.dao.CannotAcquireLockException;

/**
 * Thrown when a space update operation timeouts after waiting for a transactional
 * proper matching entry. Wraps {@link com.j_spaces.core.client.OperationTimeoutException}.
 *
 * <p><i>Note:</i> To preserve timeout semantics defined by JavaSpace API, this
 * exception is thrown <b>only</b> if timeout expires and a space operation is
 * performed with the UPDATE_OR_WRITE modifier.
 *
 * @author kimchy
 * @see com.j_spaces.core.client.UpdateModifiers#UPDATE_OR_WRITE
 */
public class UpdateOperationTimeoutException extends CannotAcquireLockException {

    public UpdateOperationTimeoutException(OperationTimeoutException e) {
        super(e.getMessage(), e);
    }
}
