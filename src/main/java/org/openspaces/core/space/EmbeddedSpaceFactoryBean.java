/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.core.space;

import com.gigaspaces.datasource.ManagedDataSource;
import com.gigaspaces.datasource.SpaceDataSource;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.sync.SpaceSynchronizationEndpoint;
import com.j_spaces.core.IJSpace;

import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.config.BlobStoreDataPolicyFactoryBean;
import org.openspaces.core.config.CustomCachePolicyFactoryBean;
import org.openspaces.core.gateway.GatewayTargetsFactoryBean;
import org.openspaces.core.properties.BeanLevelMergedPropertiesAware;
import org.openspaces.core.space.filter.FilterProviderFactory;
import org.openspaces.core.space.filter.replication.ReplicationFilterProviderFactory;
import org.openspaces.core.transaction.DistributedTransactionProcessingConfigurationFactoryBean;
import org.springframework.dao.DataAccessException;

import java.util.Properties;

/**
 * @author yuvalm
 * @since 10.0
 */
public class EmbeddedSpaceFactoryBean extends AbstractSpaceFactoryBean  implements BeanLevelMergedPropertiesAware, ClusterInfoAware {

    private final InternalSpaceFactory factory = new InternalSpaceFactory();
    private String name;

    public EmbeddedSpaceFactoryBean() {
    }

    public EmbeddedSpaceFactoryBean(String name) {
        this();
        setName(name);
    }

    @Override
    protected IJSpace doCreateSpace() throws DataAccessException {
        return factory.create(this, name, false);
    }

    /**
     * @deprecated Since 10.1
     */
    @Deprecated
    public void setName(String name) {
        this.name = name;
    }

    public void setSpaceName(String name) {
        this.name = name;
    }

    @Override
    public void setSecurityConfig(SecurityConfig securityConfig) {
        super.setSecurityConfig(securityConfig);
        factory.setSecurityConfig(securityConfig);
    }

    public void setProperties(Properties properties) {
        factory.getFactory().setProperties(properties);
    }

    public void setLookupGroups(String lookupGroups) {
        factory.getFactory().setLookupGroups(lookupGroups);
    }

    public void setLookupLocators(String lookupLocators) {
        factory.getFactory().setLookupLocators(lookupLocators);
    }

    public void setLookupTimeout(int lookupTimeout) {
        factory.getFactory().setLookupTimeout(lookupTimeout);
    }

    public void setVersioned(boolean versioned) {
        factory.getFactory().setVersioned(versioned);
    }

    public void setSecured(boolean secured) {
        factory.getFactory().setSecured(secured);
    }

    public void setSchema(String schema) {
        factory.getFactory().setSchema(schema);
    }

    public void setMirrored(boolean mirrored) {
        factory.getFactory().setMirror(mirrored);
    }

    public void setReplicationFilterProvider(ReplicationFilterProviderFactory replicationFilterProvider) {
        factory.setReplicationFilterProvider(replicationFilterProvider);
    }

    public void setExternalDataSource(ManagedDataSource externalDataSource) {
        factory.getFactory().setExternalDataSource(externalDataSource);
    }

    public void setSpaceDataSource(SpaceDataSource spaceDataSource) {
        factory.getFactory().setSpaceDataSource(spaceDataSource);
    }

    public void setSpaceSynchronizationEndpoint(SpaceSynchronizationEndpoint spaceSynchronizationEndpoint) {
        factory.getFactory().setSpaceSynchronizationEndpoint(spaceSynchronizationEndpoint);
    }

    public void setCachePolicy(CachePolicy cachePolicy) {
        factory.setCachePolicy(cachePolicy);
    }

    public void setClusterInfo(ClusterInfo clusterInfo) {
        factory.setClusterInfo(clusterInfo);
    }

    public void setFilterProviders(FilterProviderFactory[] filterProviders) {
        factory.setFilterProviders(filterProviders);
    }

    public void setSpaceTypes(SpaceTypeDescriptor[] typeDescriptors) {
        factory.getFactory().setTypeDescriptors(typeDescriptors);
    }

    public void setGatewayTargets(GatewayTargetsFactoryBean gatewayTargets) {
        factory.setGatewayTargets(gatewayTargets);
    }

    public void setDistributedTransactionProcessingConfiguration(
            DistributedTransactionProcessingConfigurationFactoryBean distributedTransactionProcessingConfiguration) {
        factory.setDistributedTransactionProcessingConfiguration(distributedTransactionProcessingConfiguration);
    }

    public void setCustomCachePolicy(CustomCachePolicyFactoryBean customCachePolicy) {
        if (customCachePolicy != null)
            setCachePolicy(customCachePolicy.asCachePolicy());
    }

    public void setBlobStoreDataPolicy(BlobStoreDataPolicyFactoryBean blobStoreDataPolicy) {
        if (blobStoreDataPolicy != null)
            setCachePolicy(blobStoreDataPolicy.asCachePolicy());
    }

    public void setEnableLastPrimaryStateKeeper(Boolean enableLastPrimaryStateKeeper) {
        if (enableLastPrimaryStateKeeper != null)
            factory.setEnableLastPrimaryStateKeeper(enableLastPrimaryStateKeeper);
    }

	@Override
	public void setMergedBeanLevelProperties(Properties beanLevelProperties) {
        factory.getFactory().setBeanLevelProperties(beanLevelProperties);
	}
}
