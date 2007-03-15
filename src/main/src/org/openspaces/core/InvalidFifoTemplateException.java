package org.openspaces.core;

import org.springframework.dao.InvalidDataAccessResourceUsageException;

/**
 * This exception is thrown if read or take operations executed in FIFO mode,
 * but the template class FIFO mode already been set to non FIFO.
 * It is a wrapper for {@link com.j_spaces.core.InvalidFifoTemplateException}.
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
     *
     * @see com.j_spaces.core.InvalidFifoTemplateException#getTemplateClassName()
     */
    public String getTemplateClassName() {
        return e.getTemplateClassName();
    }

}
