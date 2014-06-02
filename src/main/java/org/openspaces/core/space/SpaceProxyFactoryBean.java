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
import com.gigaspaces.sync.SpaceSynchronizationEndpoint;
import org.openspaces.core.config.BlobStoreDataPolicyFactoryBean;
import org.openspaces.core.config.CustomCachePolicyFactoryBean;
import org.openspaces.core.space.filter.replication.ReplicationFilterProviderFactory;

import java.util.Map;
import java.util.Properties;

/**
 * @author yuvalm
 */
public class SpaceProxyFactoryBean extends UrlSpaceFactoryBean {

    public static final String PROXY_URL_PREFIX = "jini://*/*/";

    public SpaceProxyFactoryBean() {}

    public SpaceProxyFactoryBean(String name) {
        super(PROXY_URL_PREFIX+name);
    }

    public SpaceProxyFactoryBean(String name,  Map<String, Object> params) {
        super(PROXY_URL_PREFIX+name, params);
    }

    public void setName(String name) {
        if (name.startsWith(PROXY_URL_PREFIX)) {
            super.setUrl(name);
        } else {
            super.setUrl(PROXY_URL_PREFIX+name);
        }
    }

    @Override
    public void setCustomCachePolicy(CustomCachePolicyFactoryBean customCachePolicy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCachePolicy(CachePolicy cachePolicy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setBlobStoreDataPolicy(BlobStoreDataPolicyFactoryBean blobStoreDataPolicy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setParameters(Map<String, Object> parameters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setReplicationFilterProvider(ReplicationFilterProviderFactory replicationFilterProvider) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSchema(String schema) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setNoWriteLease(boolean noWriteLease) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSpaceSynchronizationEndpoint(SpaceSynchronizationEndpoint spaceSynchronizationEndpoint) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSpaceDataSource(SpaceDataSource spaceDataSource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setExternalDataSource(ManagedDataSource externalDataSource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setUrlProperties(Properties urlProperties) {
        throw new UnsupportedOperationException();
    }
}
