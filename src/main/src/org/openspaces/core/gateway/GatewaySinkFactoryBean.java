package org.openspaces.core.gateway;

import java.util.List;
import java.util.Map;

import org.openspaces.core.space.SecurityConfig;
import org.openspaces.core.transaction.DistributedTransactionProcessingConfigurationFactoryBean;
import org.openspaces.pu.service.InvocableService;
import org.openspaces.pu.service.ServiceDetails;
import org.openspaces.pu.service.ServiceDetailsProvider;
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
 * @author idan
 * @since 8.0.3
 *
 */
public class GatewaySinkFactoryBean extends AbstractGatewayComponentFactoryBean implements DisposableBean, InitializingBean, InvocableService, ServiceDetailsProvider {

    private String localSpaceUrl;
    private List<GatewaySource> gatewaySources;
    private LocalClusterReplicationSink localClusterReplicationSink;
    private boolean requiresBootstrap;
    private SinkErrorHandlingFactoryBean errorHandlingConfiguration;
    private Long transactionTimeout;
    private Long localSpaceLookupTimeout;
    private DistributedTransactionProcessingConfigurationFactoryBean transactionProcessingConfiguration;
    
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

    /**
     * Gets the transaction timeout for the operations made by the Sink against the local cluster.
     * @return The transaction timeout in milliseconds. 
     */
    public Long getTransactionTimeout() {
        return transactionTimeout;
    }

    /**
     * Sets the transaction timeout for the operations made by the Sink against the local cluster.
     * @param transactionTimeout The transaction timeout in milliseconds.
     */
    public void setTransactionTimeout(Long transactionTimeout) {
        this.transactionTimeout = transactionTimeout;
    }

    /**
     * Sets the lookup timeout for finding the local cluster the Sink works against.
     * @param lookupTimeout The lookup timeout in milliseconds.
     */
    public void setLocalSpaceLookupTimeout(Long lookupTimeout) {
        this.localSpaceLookupTimeout = lookupTimeout;
    }

    /**
     * Gets the lookup timeout for finding the local cluster the Sink works against.
     * @return The lookup timeout in milliseconds.
     */
    public Long getLocalSpaceLookupTimeout() {
        return localSpaceLookupTimeout;
    }

    /**
     * Gets distributed transaction processing configuration for the Sink component.
     * @return Distributed transaction processing configuration.
     */
    public DistributedTransactionProcessingConfigurationFactoryBean getDistributedTransactionProcessingConfiguration() {
        return transactionProcessingConfiguration;
    }
    
    /**
     * Sets the distributed transaction processing configuration for the Sink component.
     * @param transactionProcessingConfiguration The distributed transaction processing configuration to set.
     */
    public void setDistributedTransactionProcessingConfiguration(DistributedTransactionProcessingConfigurationFactoryBean transactionProcessingConfiguration) {
        this.transactionProcessingConfiguration = transactionProcessingConfiguration;
    }
    
    @Override
    protected void afterPropertiesSetImpl(SecurityConfig securityConfig){
        LocalClusterReplicationSinkConfig config = new LocalClusterReplicationSinkConfig(getLocalGatewayName());
        config.setLocalClusterSpaceUrl(localSpaceUrl);
        config.setStartLookupService(isStartEmbeddedLus());
        config.setRequiresBootstrap(requiresBootstrap);
        if (transactionTimeout != null)
            config.setTransactionTimeout(transactionTimeout.longValue());
        if (localSpaceLookupTimeout != null)
            config.setFindTimeout(localSpaceLookupTimeout.longValue());
        if (errorHandlingConfiguration != null)
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
        if (transactionProcessingConfiguration != null)
            transactionProcessingConfiguration.copyParameters(config.getTransactionProcessingParameters());
        if (securityConfig != null)
            config.setUserDetails(securityConfig.toUserDetails());
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
            Long bootstrapTimeoutInSeconds = (Long) namedArgs.get("bootstrapTimeout");
            try 
            {
                BootstrapConfig config = new BootstrapConfig(bootstrapRemoteGatewayName);
                if (bootstrapTimeoutInSeconds != null)
                    config.setTimeout(bootstrapTimeoutInSeconds.longValue());
                
                localClusterReplicationSink.bootstrapFromRemoteSink(config);
            } catch (Exception e) {
               //TODO WAN: properly wrap this
                throw new RuntimeException(e.getMessage(), e);
            }
            return null;
        }   
        
        throw new UnsupportedOperationException("Only enableIncomingReplication and bootstrapFromGateway invocations are supported");
    }

    public ServiceDetails[] getServicesDetails() {
        String[] gatewaySourcesNames = null;
        if (gatewaySources != null) {
            gatewaySourcesNames = new String[gatewaySources.size()];
            int index = 0;
            for (GatewaySource gatewaySource : gatewaySources) {
                gatewaySourcesNames[index++] = gatewaySource.getName();
            }
        } else {
            gatewaySourcesNames = new String[0];
        }
        return new ServiceDetails[]{new GatewaySinkServiceDetails(getLocalGatewayName(), gatewaySourcesNames, requiresBootstrap, getLocalSpaceUrl())};
    }
    
}
