package org.openspaces.core.gateway;

import java.util.List;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.gigaspaces.internal.cluster.node.impl.gateway.ReplicationDelegationConfig;
import com.gigaspaces.internal.cluster.node.impl.gateway.delegator.ReplicationConnectionDelegatorConfig;
import com.gigaspaces.internal.cluster.node.impl.gateway.delegator.ReplicationConnectionDelegatorContainer;
import com.gigaspaces.internal.cluster.node.impl.gateway.lus.ReplicationLookupParameters;

/**
 * A gateway delegator factory bean for creating a {@link ReplicationConnectionDelegatorContainer}.
 * 
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewayDelegatorFactoryBean extends AbstractGatewayComponentFactoryBean implements DisposableBean, InitializingBean {

    private ReplicationConnectionDelegatorContainer replicationConnectiondelegatorContainer;
    private List<GatewayDelegation> gatewayDelegations;
    
    public GatewayDelegatorFactoryBean() {
    }

    /**
     * @return Gateway delegator factory bean's delegations as a {@link GatewayDelegation} array. 
     */
    public List<GatewayDelegation> getGatewayDelegations() {
        return gatewayDelegations;
    }
    
    /**
     * Sets the gateway delegations for the configured {@link ReplicationConnectionDelegatorContainer}.
     * @param gatewayDelegations {@link GatewayDelegation}s array.
     */
    public void setGatewayDelegations(List<GatewayDelegation> gatewayDelegations) {
        this.gatewayDelegations = gatewayDelegations;
    }

    /**
     * Initializes a {@link ReplicationConnectionDelegatorContainer} using the provided bean's properties.
     */
    @Override
    protected void afterPropertiesSetImpl() {
        
        ReplicationConnectionDelegatorConfig config = new ReplicationConnectionDelegatorConfig(getLocalGatewayName());
        config.setStartLookupService(isStartEmbeddedLus());
        if (gatewayDelegations != null) {
            for (GatewayDelegation delegation : gatewayDelegations) {
                ReplicationDelegationConfig replicationRoutingConfig = new ReplicationDelegationConfig();
                replicationRoutingConfig.setTargetName(delegation.getTarget());
                replicationRoutingConfig.setDelegation(delegation.getDelegateThrough());
                config.addDelegator(replicationRoutingConfig);
            }
        }
        if (getGatewayLookups() != null) {
            ReplicationLookupParameters lookupParameters = getGatewayLookups().asReplicationLookupParameters();
            config.setGatewayLookupParameters(lookupParameters);
        }
        // TODO WAN: add finder timeout
        replicationConnectiondelegatorContainer = new ReplicationConnectionDelegatorContainer(config);
    }

    /**
     * Destroys and unregisters the gateway delegator component.
     */
    @Override
    protected void destroyImpl() {
        if (replicationConnectiondelegatorContainer != null) {
            replicationConnectiondelegatorContainer.close();
            replicationConnectiondelegatorContainer = null;
        }
    }

}
