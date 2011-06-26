package org.openspaces.core.gateway;


/**
 * Holds gateway delegation settings.
 * {@link GatewayDelegation.getTarget} specifies the delegation target name.
 * {@link GatewayDelegation.getDelegateThrough} specifies the component name to delegate through.
 * 
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewayDelegation {
    
    private String target;
    private String delegateThrough;

    public GatewayDelegation() {
    }
    public GatewayDelegation(String target, String delegateThrough) {
        this.target = target;
        this.delegateThrough = delegateThrough;
    }

    /**
     * @return The delegation target name.
     */
    public String getTarget() {
        return target;
    }

    /**
     * @return The name of the component the delegation will be made through.
     */
    public String getDelegateThrough() {
        return delegateThrough;
    }
    
    /**
     * Sets the delegation target name.
     * @param target The delegation target name.
     */
    public void setTarget(String target) {
        this.target = target;
    }
    
    /**
     * Sets the name of the component the delegation will be made through.
     * @param delegateThrough The component name to delegate through.
     */
    public void setDelegateThrough(String delegateThrough) {
        this.delegateThrough = delegateThrough;
    }
    
    
    
}
