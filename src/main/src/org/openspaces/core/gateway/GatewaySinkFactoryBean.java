package org.openspaces.core.gateway;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.gigaspaces.internal.cluster.node.impl.gateway.lus.ReplicationLookupParameters;
import com.gigaspaces.internal.cluster.node.impl.gateway.sink.LocalClusterReplicationGateway;
import com.gigaspaces.internal.cluster.node.impl.gateway.sink.LocalClusterReplicationGatewayConfig;

/**
 * 
 * @author idan
 * @since 8.0.2
 *
 */
public class GatewaySinkFactoryBean extends AbstractGatewayComponentFactoryBean implements DisposableBean, InitializingBean {

    private String localSpaceUrl;
    private GatewaySource[] gatewaySources;
    private LocalClusterReplicationGateway localClusterReplicationGateway;

    public GatewaySinkFactoryBean() {
    }

    public String getLocalSpaceUrl() {
        return localSpaceUrl;
    }
    public void setLocalSpaceUrl(String localSpaceUrl) {
        this.localSpaceUrl = localSpaceUrl;
    }
    
    public GatewaySource[] getGatewaySources() {
        return gatewaySources;
    }
    public void setGatewaySources(GatewaySource[] gatewaySources) {
        this.gatewaySources = gatewaySources;
    }

    @Override
    protected void afterPropertiesSetImpl(){
        LocalClusterReplicationGatewayConfig config = new LocalClusterReplicationGatewayConfig(getLocalGatewayName());
        config.setLocalClusterSpaceUrl(localSpaceUrl);
        config.setStartLookupService(isStartEmbeddedLus());
        if (getGatewaySources() != null) {
            String[] gatewaySourcesNames = new String[getGatewaySources().length];
            for (int i = 0; i < getGatewaySources().length; i++) {
                gatewaySourcesNames[i] = getGatewaySources()[i].getName();
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
