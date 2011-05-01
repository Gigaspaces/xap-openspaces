package org.openspaces.core.gateway;

import net.jini.core.discovery.LookupLocator;

import com.gigaspaces.internal.cluster.node.impl.gateway.lus.ReplicationLookupParameters;

/**
 * 
 * @author Idan Moyal
 * @since 8.0.2
 *
 */
public class GatewayLookupsFactoryBean {

    private String lookupGroup;
    private GatewayLookup[] gatewayLookups;

    public GatewayLookupsFactoryBean() {
    }
    public GatewayLookupsFactoryBean(String lookupGroup) {
        this.lookupGroup = lookupGroup;
    }

    public String getLookupGroup() {
        return lookupGroup;
    }
    public void setLookupGroup(String lookupGroup) {
        this.lookupGroup = lookupGroup;
    }
    
    public GatewayLookup[] getGatewayLookups() {
        return gatewayLookups;
    }
    public void setGatewayLookups(GatewayLookup[] gatewayLookups) {
        this.gatewayLookups = gatewayLookups;
    }
    public ReplicationLookupParameters asReplicationLookupParameters() {
        ReplicationLookupParameters parameters = new ReplicationLookupParameters();
        parameters.setLookupGroups(new String[] { lookupGroup });
        if (gatewayLookups != null) {
            LookupLocator[] locators = new LookupLocator[gatewayLookups.length];
            for (int i = 0; i < gatewayLookups.length; i++) {
                locators[i] = new LookupLocator(gatewayLookups[i].getHost(), gatewayLookups[i].getLusPort());
            }
            parameters.setLookupLocators(locators);
        }
        return parameters;
    }
    
    
    
}
