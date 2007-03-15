package org.openspaces.core;

/**
 * This exception is thrown if read or take operations executed in FIFO mode,
 * but the template class FIFO mode already been set to non FIFO.
 * Wraps {@link com.j_spaces.core.InvalidFifoTemplateException}.
 *
 * @author kimchy
 */
public class InvalidFifoTemplateException extends InvalidFifoOperationException {

    private com.j_spaces.core.InvalidFifoTemplateException e;

    public InvalidFifoTemplateException(com.j_spaces.core.InvalidFifoTemplateException e) {
        super(e);
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
