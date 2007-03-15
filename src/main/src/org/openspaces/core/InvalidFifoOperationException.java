package org.openspaces.core;

import com.j_spaces.core.FifoOperationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * A base class for invalid fifo operations exceptions. Wraps {@link com.j_spaces.core.FifoOperationException}.
 *
 * @author kimchy
 */
public class InvalidFifoOperationException extends InvalidDataAccessResourceUsageException {

    public InvalidFifoOperationException(FifoOperationException e) {
        super(e.getMessage(), e);
    }
}
