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
    
}
