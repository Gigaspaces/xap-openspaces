package org.openspaces.core;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * This exception is thrown if read or take operations executed in FIFO mode,
 * but the template class FIFO mode already been set to non FIFO.
 *
 * @author kimchy
 */
public class InvalidFifoTemplateException extends InvalidDataAccessResourceUsageException {

    private com.j_spaces.core.InvalidFifoTemplateException e;

    public InvalidFifoTemplateException(com.j_spaces.core.InvalidFifoTemplateException e) {
        super(e.getMessage(), e);
        this.e = e;
    }

    /**
     * Returns invalid template className.
     */
    public String getTemplateClassName() {
        return e.getTemplateClassName();
    }

}
