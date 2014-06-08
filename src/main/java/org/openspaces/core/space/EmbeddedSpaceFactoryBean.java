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
import org.openspaces.core.config.BlobStoreDataPolicyFactoryBean;
import org.openspaces.core.config.CustomCachePolicyFactoryBean;
import org.openspaces.core.gateway.GatewayTargetsFactoryBean;
import org.openspaces.core.space.filter.FilterProviderFactory;
import org.openspaces.core.space.filter.replication.ReplicationFilterProviderFactory;
import org.openspaces.core.transaction.DistributedTransactionProcessingConfigurationFactoryBean;
import org.springframework.dao.DataAccessException;

import java.util.Properties;

/**
 * @author yuvalm
 * @since 10.0
 */
public class EmbeddedSpaceFactoryBean extends AbstractSpaceFactoryBean {

    public static final String URL_PREFIX = "/./";

    private final UrlSpaceFactoryBean factoryBean;

    public EmbeddedSpaceFactoryBean() {
        this.factoryBean = new UrlSpaceFactoryBean();
    }

    public EmbeddedSpaceFactoryBean(String name) {
        this();
        setName(name);
    }

    @Override
    protected IJSpace doCreateSpace() throws DataAccessException {
        return factoryBean.doCreateSpace();
    }

    public void setName(String name) {
        factoryBean.setUrl(name.startsWith(URL_PREFIX) ? name : URL_PREFIX + name);
    }

    @Override
    public void setSecurityConfig(SecurityConfig securityConfig) {
        super.setSecurityConfig(securityConfig);
        factoryBean.setSecurityConfig(securityConfig);
    }

    public void setProperties(Properties properties) {
        factoryBean.setProperties(properties);
    }

    public void setLookupGroups(String lookupGroups) {
        factoryBean.setLookupGroups(lookupGroups);
    }

    public void setLookupLocators(String lookupLocators) {
        factoryBean.setLookupLocators(lookupLocators);
    }

    public void setLookupTimeout(int lookupTimeout) {
        factoryBean.setLookupTimeout(lookupTimeout);
    }

    public void setVersioned(boolean versioned) {
        factoryBean.setVersioned(versioned);
    }

    public void setSecured(boolean secured) {
        factoryBean.setSecured(secured);
    }

    public void setSchema(String schema) {
        factoryBean.setSchema(schema);
    }

    public void setMirrored(boolean mirrored) {
        factoryBean.setMirror(mirrored);
    }

    public void setReplicationFilterProvider(ReplicationFilterProviderFactory replicationFilterProvider) {
        factoryBean.setReplicationFilterProvider(replicationFilterProvider);
    }

    public void setExternalDataSource(ManagedDataSource externalDataSource) {
        factoryBean.setExternalDataSource(externalDataSource);
    }

    public void setSpaceDataSource(SpaceDataSource spaceDataSource) {
        factoryBean.setSpaceDataSource(spaceDataSource);
    }

    public void setSpaceSynchronizationEndpoint(SpaceSynchronizationEndpoint spaceSynchronizationEndpoint) {
        factoryBean.setSpaceSynchronizationEndpoint(spaceSynchronizationEndpoint);
    }

    public void setCachePolicy(CachePolicy cachePolicy) {
        factoryBean.setCachePolicy(cachePolicy);
    }

    public void setClusterInfo(ClusterInfo clusterInfo) {
        factoryBean.setClusterInfo(clusterInfo);
    }

    public void setFilterProviders(FilterProviderFactory[] filterProviders) {
        factoryBean.setFilterProviders(filterProviders);
    }

    public void setSpaceTypes(SpaceTypeDescriptor[] spaceTypes) {
        factoryBean.setSpaceTypes(spaceTypes);
    }

    public void setGatewayTargets(GatewayTargetsFactoryBean gatewayTargets) {
        factoryBean.setGatewayTargets(gatewayTargets);
    }

    public void setDistributedTransactionProcessingConfiguration(
            DistributedTransactionProcessingConfigurationFactoryBean distributedTransactionProcessingConfiguration) {
        factoryBean.setDistributedTransactionProcessingConfiguration(distributedTransactionProcessingConfiguration);
    }

    public void setCustomCachePolicy(CustomCachePolicyFactoryBean customCachePolicy) {
        factoryBean.setCustomCachePolicy(customCachePolicy);
    }

    public void setBlobStoreDataPolicy(BlobStoreDataPolicyFactoryBean blobStoreDataPolicy) {
        factoryBean.setBlobStoreDataPolicy(blobStoreDataPolicy);
    }
}
