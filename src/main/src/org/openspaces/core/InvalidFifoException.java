package org.openspaces.core;

import com.j_spaces.core.FifoOperationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * @author kimchy
 */
public class InvalidFifoException extends InvalidDataAccessResourceUsageException {

    public InvalidFifoException(FifoOperationException e) {
        super(e.getMessage(), e);
    }
}
