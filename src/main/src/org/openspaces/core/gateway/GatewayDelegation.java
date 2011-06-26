package org.openspaces.core.gateway;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.openspaces.admin.gateway.IDelegation;

import com.gigaspaces.internal.io.IOUtils;
import com.gigaspaces.internal.utils.StringUtils;


/**
 * Holds gateway delegation settings.
 * {@link GatewayDelegation.getTarget} specifies the delegation target name.
 * {@link GatewayDelegation.getDelegateThrough} specifies the component name to delegate through.
 * 
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewayDelegation implements IDelegation, Externalizable {
    
    private static final long serialVersionUID = 1L;
    
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
    public String getTargetGatewayName() {
        return target;
    }

    /**
     * @return The name of the component the delegation will be made through.
     */
    public String getDelegateThroughGatewayName() {
        return delegateThrough;
    }
    
    public boolean isDelegateThroughOtherGateway() {
        return StringUtils.hasText(getDelegateThroughGatewayName());
    }
    
    /**
     * Sets the delegation target name.
     * @param target The delegation target name.
     */
    public void setTargetGatewayName(String target) {
        this.target = target;
    }
    
    /**
     * Sets the name of the component the delegation will be made through.
     * @param delegateThrough The component name to delegate through.
     */
    public void setDelegateThroughGatewayName(String delegateThrough) {
        this.delegateThrough = delegateThrough;
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        IOUtils.writeString(out, target);
        IOUtils.writeString(out, delegateThrough);
    }
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        target = IOUtils.readString(in);
        delegateThrough = IOUtils.readString(in);
    }
    
    
    
}
