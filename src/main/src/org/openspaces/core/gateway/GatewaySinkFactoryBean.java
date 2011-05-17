package org.openspaces.core.gateway;

import java.util.List;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.gigaspaces.internal.cluster.node.impl.gateway.lus.ReplicationLookupParameters;
import com.gigaspaces.internal.cluster.node.impl.gateway.sink.LocalClusterReplicationGateway;
import com.gigaspaces.internal.cluster.node.impl.gateway.sink.LocalClusterReplicationGatewayConfig;

/**
 * A sink factory bean for creating a {@link LocalClusterReplicationGateway} which
 * represents a gateway sink component.
 * 
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewaySinkFactoryBean extends AbstractGatewayComponentFactoryBean implements DisposableBean, InitializingBean {

    private String localSpaceUrl;
    private List<GatewaySource> gatewaySources;
    private LocalClusterReplicationGateway localClusterReplicationGateway;

    public GatewaySinkFactoryBean() {
    }

    /**
     * @return The local space URL the sink component operates against.
     */
    public String getLocalSpaceUrl() {
        return localSpaceUrl;
    }
    
    /**
     * Sets the local space URL the sink component operates against.
     * @param localSpaceUrl The local space URL.
     */
    public void setLocalSpaceUrl(String localSpaceUrl) {
        this.localSpaceUrl = localSpaceUrl;
    }
    
    /**
     * @return The sink component's gateway replication sources.
     */
    public List<GatewaySource> getGatewaySources() {
        return gatewaySources;
    }
    
    /**
     * Sets the sink component's gateway replication sources.
     * @param gatewaySources The gateway replication sources.
     */
    public void setGatewaySources(List<GatewaySource> gatewaySources) {
        this.gatewaySources = gatewaySources;
    }

    @Override
    protected void afterPropertiesSetImpl(){
        LocalClusterReplicationGatewayConfig config = new LocalClusterReplicationGatewayConfig(getLocalGatewayName());
        config.setLocalClusterSpaceUrl(localSpaceUrl);
        config.setStartLookupService(isStartEmbeddedLus());
        if (getGatewaySources() != null) {
            String[] gatewaySourcesNames = new String[getGatewaySources().size()];
            for (int i = 0; i < getGatewaySources().size(); i++) {
                gatewaySourcesNames[i] = getGatewaySources().get(i).getName();
            }
            config.setSiteNames(gatewaySourcesNames);
            if (getGatewayLookups() == null)
                throw new IllegalArgumentException("gatewayLookups property was not set!");
            ReplicationLookupParameters lookupParameters = getGatewayLookups().asReplicationLookupParameters();
            config.setGatewayLookupParameters(lookupParameters);
        }
        // TODO WAN: add finder timeout
        localClusterReplicationGateway = new LocalClusterReplicationGateway(config); 
    }

    @Override
    protected void destroyImpl() {
        if (localClusterReplicationGateway != null) {
            localClusterReplicationGateway.close();
            localClusterReplicationGateway = null;
        }
    }

    
}
