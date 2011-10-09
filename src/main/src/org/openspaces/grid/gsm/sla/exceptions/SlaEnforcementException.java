package org.openspaces.grid.gsm.sla.exceptions;

public abstract class SlaEnforcementException extends Exception {

    private static final long serialVersionUID = 1L;

    public SlaEnforcementException(String message) {
        super(message);
    }
    
    public SlaEnforcementException(String message, Exception cause) {
        super(message, cause);
    }
    
    /**
     * Override the method to avoid expensive stack build and synchronization,
     * since no one uses it anyway.
     */
    @Override
    public Throwable fillInStackTrace()
    {
        return null;
    }
}
