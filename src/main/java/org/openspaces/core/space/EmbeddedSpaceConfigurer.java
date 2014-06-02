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

import com.gigaspaces.cluster.activeelection.ISpaceModeListener;
import com.gigaspaces.datasource.ManagedDataSource;
import com.gigaspaces.datasource.SpaceDataSource;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.security.directory.CredentialsProvider;
import com.gigaspaces.security.directory.DefaultCredentialsProvider;
import com.gigaspaces.security.directory.UserDetails;
import com.gigaspaces.sync.SpaceSynchronizationEndpoint;
import com.j_spaces.core.IJSpace;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.space.filter.FilterProviderFactory;
import org.openspaces.core.space.filter.replication.ReplicationFilterProviderFactory;

import java.util.Properties;

/**
 * @author yuvalm
 */
public class EmbeddedSpaceConfigurer implements SpaceConfigurer {

    private UrlSpaceConfigurer urlSpaceConfigurer;

    public EmbeddedSpaceConfigurer(String name) {
        urlSpaceConfigurer = new UrlSpaceConfigurer(name);
        urlSpaceConfigurer.setUrlSpaceFactoryBean(new EmbeddedSpaceFactoryBean(name));
    }

    @Override
    public IJSpace space() {
        return urlSpaceConfigurer.space();
    }

    public EmbeddedSpaceConfigurer addParameter(String name, String value) {
        urlSpaceConfigurer.addParameter(name, value);
        return this;
    }

    public EmbeddedSpaceConfigurer addProperty(String name, String value) {
        urlSpaceConfigurer.addProperty(name, value);
        return this;
    }

    public EmbeddedSpaceConfigurer addProperties(Properties properties) {
        urlSpaceConfigurer.addProperties(properties);
        return this;
    }

    public EmbeddedSpaceConfigurer addUrlProperty(String name, String value) {
        urlSpaceConfigurer.addUrlProperty(name, value);
        return this;
    }

    public EmbeddedSpaceConfigurer schema(String schema) {
        urlSpaceConfigurer.schema(schema);
        return this;
    }

    public EmbeddedSpaceConfigurer fifo(boolean fifo) {
        urlSpaceConfigurer.fifo(fifo);
        return this;
    }

    public EmbeddedSpaceConfigurer lookupGroups(String lookupGroups) {
        urlSpaceConfigurer.lookupGroups(lookupGroups);
        return this;
    }

    public EmbeddedSpaceConfigurer lookupGroups(String... lookupGroups) {
        urlSpaceConfigurer.lookupGroups(lookupGroups);
        return this;
    }

    public EmbeddedSpaceConfigurer lookupLocators(String lookupLocators) {
        urlSpaceConfigurer.lookupLocators(lookupLocators);
        return this;
    }

    public EmbeddedSpaceConfigurer lookupLocators(String... lookupLocators) {
        urlSpaceConfigurer.lookupLocators(lookupLocators);
        return this;
    }

    public EmbeddedSpaceConfigurer lookupTimeout(int lookupTimeout) {
        urlSpaceConfigurer.lookupTimeout(lookupTimeout);
        return this;
    }

    public EmbeddedSpaceConfigurer versioned(boolean versioned) {
        urlSpaceConfigurer.versioned(versioned);
        return this;
    }

    public EmbeddedSpaceConfigurer noWriteLease(boolean noWriteLease) {
        urlSpaceConfigurer.noWriteLease(noWriteLease);
        return this;
    }

    public EmbeddedSpaceConfigurer mirror(boolean mirror) {
        urlSpaceConfigurer.mirror(mirror);
        return this;
    }

    public EmbeddedSpaceConfigurer addFilterProvider(FilterProviderFactory filterProviderFactory) {
        urlSpaceConfigurer.addFilterProvider(filterProviderFactory);
        return this;
    }

    public EmbeddedSpaceConfigurer addSpaceType(SpaceTypeDescriptor spaceType) {
        urlSpaceConfigurer.addSpaceType(spaceType);
        return this;
    }

    public EmbeddedSpaceConfigurer replicationFilterProvider(ReplicationFilterProviderFactory replicationFilterProvider) {
        urlSpaceConfigurer.replicationFilterProvider(replicationFilterProvider);
        return this;
    }

    public EmbeddedSpaceConfigurer externalDataSource(ManagedDataSource externalDataSource) {
        urlSpaceConfigurer.externalDataSource(externalDataSource);
        return this;
    }

    public EmbeddedSpaceConfigurer spaceDataSource(SpaceDataSource spaceDataSource) {
        urlSpaceConfigurer.spaceDataSource(spaceDataSource);
        return this;
    }

    public EmbeddedSpaceConfigurer spaceSynchronizationEndpoint(SpaceSynchronizationEndpoint synchronizationEndpoint) {
        urlSpaceConfigurer.spaceSynchronizationEndpoint(synchronizationEndpoint);
        return this;
    }

    public EmbeddedSpaceConfigurer cachePolicy(CachePolicy cachePolicy) {
        urlSpaceConfigurer.cachePolicy(cachePolicy);
        return this;
    }

    public EmbeddedSpaceConfigurer clusterInfo(ClusterInfo clusterInfo) {
        urlSpaceConfigurer.clusterInfo(clusterInfo);
        return this;
    }

    public EmbeddedSpaceConfigurer registerForSpaceModeNotifications(boolean registerForSpaceMode) {
        urlSpaceConfigurer.registerForSpaceModeNotifications(registerForSpaceMode);
        return this;
    }

    public EmbeddedSpaceConfigurer secured(boolean secured) {
        urlSpaceConfigurer.secured(secured);
        return this;
    }

    public EmbeddedSpaceConfigurer userDetails(String userName, String password) {
        return securityConfig(new SecurityConfig(userName, password));
    }

    public EmbeddedSpaceConfigurer userDetails(UserDetails userDetails) {
        return credentialsProvider(new DefaultCredentialsProvider(userDetails));
    }

    public EmbeddedSpaceConfigurer credentials(String userName, String password) {
        return securityConfig(new SecurityConfig(userName, password));
    }

    public EmbeddedSpaceConfigurer credentialsProvider(CredentialsProvider credentialsProvider) {
        return securityConfig(new SecurityConfig(credentialsProvider));
    }

    public EmbeddedSpaceConfigurer securityConfig(SecurityConfig securityConfig) {
        urlSpaceConfigurer.securityConfig(securityConfig);
        return this;
    }

    public EmbeddedSpaceConfigurer primaryBackupListener(ISpaceModeListener primaryBackupListener) {
        urlSpaceConfigurer.primaryBackupListener(primaryBackupListener);
        return this;
    }
}
