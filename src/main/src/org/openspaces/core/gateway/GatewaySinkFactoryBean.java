package org.openspaces.core.gateway;

import java.util.List;
import java.util.Map;

import org.openspaces.pu.service.InvocableService;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.gigaspaces.internal.cluster.node.impl.gateway.lus.ReplicationLookupParameters;
import com.gigaspaces.internal.cluster.node.impl.gateway.sink.BootstrapConfig;
import com.gigaspaces.internal.cluster.node.impl.gateway.sink.LocalClusterReplicationSink;
import com.gigaspaces.internal.cluster.node.impl.gateway.sink.LocalClusterReplicationSinkConfig;

/**
 * A sink factory bean for creating a {@link LocalClusterReplicationSink} which
 * represents a gateway sink component.
 * 
 * @author Idan Moyal
 * @since 8.0.3
 *
 */
public class GatewaySinkFactoryBean extends AbstractGatewayComponentFactoryBean implements DisposableBean, InitializingBean, InvocableService {

    private String localSpaceUrl;
    private List<GatewaySource> gatewaySources;
    private LocalClusterReplicationSink localClusterReplicationSink;
    private boolean requiresBootstrap;
    private SinkErrorHandlingFactoryBean errorHandlingConfiguration;

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

    /**
     * Sets whether bootstrap is required for the Sink component.
     * @param requiresBootstrap true if bootstrap is required.
     */
    public void setRequiresBootstrap(boolean requiresBootstrap) {
        this.requiresBootstrap = requiresBootstrap;
    }

    /**
     * Gets whether bootstrap is required for the Sink component.
     * @return true if bootstrap is required.
     */
    public boolean getRequiresBootstrap() {
        return requiresBootstrap;
    }
    
    /**
     * Sets the error handling configuration instance for the Sink component.
     * @param errorHandlingConfiguration The error handling configuration instance.
     */
    public void setErrorHandlingConfiguration(SinkErrorHandlingFactoryBean errorHandlingConfiguration) {
        this.errorHandlingConfiguration = errorHandlingConfiguration;
    }

    /**
     * Gets the error handling configuration instance defined for the Sink component.
     * @return The error handling configuration instance.
     */
    public SinkErrorHandlingFactoryBean getErrorHandlingConfiguration() {
        return errorHandlingConfiguration;
    }

    @Override
    protected void afterPropertiesSetImpl(){
        LocalClusterReplicationSinkConfig config = new LocalClusterReplicationSinkConfig(getLocalGatewayName());
        config.setLocalClusterSpaceUrl(localSpaceUrl);
        config.setStartLookupService(isStartEmbeddedLus());
        config.setRequiresBootstrap(requiresBootstrap);
        errorHandlingConfiguration.copyToSinkConfiguration(config);
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
        localClusterReplicationSink = new LocalClusterReplicationSink(config); 
    }

    @Override
    protected void destroyImpl() {
        if (localClusterReplicationSink != null) {
            localClusterReplicationSink.close();
            localClusterReplicationSink = null;
        }
    }

    public Object invoke(Map<String, Object> namedArgs) {
        if (namedArgs.containsKey("enableIncomingReplication"))
        {
            localClusterReplicationSink.enableIncomingReplication();
            return null;
        }
        if (namedArgs.containsKey("bootstrapFromGateway"))
        {
            String bootstrapRemoteGatewayName = (String) namedArgs.get("bootstrapFromGateway");
            String delegateThrough = (String) namedArgs.get("bootstrapDelegateThroughGateway");
            Integer bootstrapTimeoutInSeconds = (Integer) namedArgs.get("bootstrapTimeout");
            try 
            {
                BootstrapConfig config = new BootstrapConfig(bootstrapRemoteGatewayName);
                if (delegateThrough != null)
                    config.setDelegateThroughGatewayName(delegateThrough);
                if (bootstrapTimeoutInSeconds != null)
                    config.setTimeout(bootstrapTimeoutInSeconds.intValue());
                
                localClusterReplicationSink.bootstrapFromRemoteSink(config);
            } catch (Exception e) {
               //TODO WAN: properly wrap this
                throw new RuntimeException(e.getMessage(), e);
            }
            return null;
        }   
        
        throw new UnsupportedOperationException("Only enableIncomingReplication and bootstrapFromGateway invocations are supported");
    }    
}
