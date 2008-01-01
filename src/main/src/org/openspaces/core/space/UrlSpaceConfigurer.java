/*
 * Copyright 2006-2007 the original author or authors.
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
import com.j_spaces.core.IJSpace;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.space.filter.FilterProviderFactory;
import org.openspaces.core.space.filter.replication.ReplicationFilterProviderFactory;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * A simple configurer helper to create {@link IJSpace} instances. The configurer wraps
 * {@link org.openspaces.core.space.UrlSpaceFactoryBean} and providing a simpler means
 * to configure it using code.
 *
 * <p>An example of using it:
 * <pre>
 * UrlSpaceConfigurer urlSpaceConfigurer = new UrlSpaceConfigurer("/./space").schema("persistent")
 *          .noWriteLeaseMode(true).lookupGroups(new String[] {"kimchy"});
 * IJSpace space = urlSpaceConfigurer.space();
 * ...
 * urlSpaceConfigurer.destroy(); // optional
 * </pre>
 *
 * @author kimchy
 */
public class UrlSpaceConfigurer {

    private UrlSpaceFactoryBean urlSpaceFactoryBean;

    private IJSpace space;

    private Properties properties = new Properties();

    private Properties urlProperties = new Properties();

    private Map<String, Object> parameters = new HashMap<String, Object>();

    private List<FilterProviderFactory> filterProviderFactories = new ArrayList<FilterProviderFactory>();

    public UrlSpaceConfigurer(String url) {
        this.urlSpaceFactoryBean = new UrlSpaceFactoryBean(url);
    }

    /**
     * @see org.openspaces.core.space.UrlSpaceFactoryBean#setParameters(java.util.Map)
     */
    public UrlSpaceConfigurer addParameter(String name, String value) {
        parameters.put(name, value);
        return this;
    }

    /**
     * @see org.openspaces.core.space.UrlSpaceFactoryBean#setProperties(java.util.Properties)
     */
    public UrlSpaceConfigurer addProperty(String name, String value) {
        properties.setProperty(name, value);
        return this;
    }

    /**
     * @see org.openspaces.core.space.UrlSpaceFactoryBean#setUrlProperties(java.util.Properties)
     */
    public UrlSpaceConfigurer addUrlProperty(String name, String value) {
        urlProperties.setProperty(name, value);
        return this;
    }

    /**
     * @see org.openspaces.core.space.UrlSpaceFactoryBean#setSchema(String)
     */
    public UrlSpaceConfigurer schema(String schema) {
        urlSpaceFactoryBean.setSchema(schema);
        return this;
    }

    /**
     * @see org.openspaces.core.space.UrlSpaceFactoryBean#setFifo(boolean)
     */
    public UrlSpaceConfigurer fifo(boolean fifo) {
        urlSpaceFactoryBean.setFifo(fifo);
        return this;
    }

    /**
     * @see org.openspaces.core.space.UrlSpaceFactoryBean#setLookupGroups(String)
     */
    public UrlSpaceConfigurer lookupGroups(String lookupGroups) {
        urlSpaceFactoryBean.setLookupGroups(lookupGroups);
        return this;
    }

    /**
     * @see org.openspaces.core.space.UrlSpaceFactoryBean#setLookupGroups(String)
     */
    public UrlSpaceConfigurer lookupGroups(String ... lookupGroups) {
        urlSpaceFactoryBean.setLookupGroups(StringUtils.arrayToCommaDelimitedString(lookupGroups));
        return this;
    }

    /**
     * @see org.openspaces.core.space.UrlSpaceFactoryBean#setLookupLocators(String)
     */
    public UrlSpaceConfigurer lookupLocators(String lookupLocators) {
        urlSpaceFactoryBean.setLookupLocators(lookupLocators);
        return this;
    }

    /**
     * @see org.openspaces.core.space.UrlSpaceFactoryBean#setLookupLocators(String)
     */
    public UrlSpaceConfigurer lookupLocators(String ... lookupLocators) {
        urlSpaceFactoryBean.setLookupLocators(StringUtils.arrayToCommaDelimitedString(lookupLocators));
        return this;
    }

    /**
     * @see org.openspaces.core.space.UrlSpaceFactoryBean#setLookupTimeout(Integer)
     */
    public UrlSpaceConfigurer lookupTimeout(int lookupTimeout) {
        urlSpaceFactoryBean.setLookupTimeout(lookupTimeout);
        return this;
    }

    /**
     * @see org.openspaces.core.space.UrlSpaceFactoryBean#setVersioned(boolean)
     */
    public UrlSpaceConfigurer versioned(boolean versioned) {
        urlSpaceFactoryBean.setVersioned(versioned);
        return this;
    }

    /**
     * @see org.openspaces.core.space.UrlSpaceFactoryBean#setNoWriteLease(boolean)
     */
    public UrlSpaceConfigurer noWriteLease(boolean noWriteLease) {
        urlSpaceFactoryBean.setNoWriteLease(noWriteLease);
        return this;
    }

    /**
     * @see org.openspaces.core.space.UrlSpaceFactoryBean#setMirror(boolean)
     */
    public UrlSpaceConfigurer mirror(boolean mirror) {
        urlSpaceFactoryBean.setMirror(mirror);
        return this;
    }

    /**
     * @see org.openspaces.core.space.UrlSpaceFactoryBean#setFilterProviders(org.openspaces.core.space.filter.FilterProviderFactory[])
     */
    public UrlSpaceConfigurer addFilterProvider(FilterProviderFactory filterProviderFactory) {
        filterProviderFactories.add(filterProviderFactory);
        return this;
    }

    /**
     * @see org.openspaces.core.space.UrlSpaceFactoryBean#setReplicationFilterProvider(org.openspaces.core.space.filter.replication.ReplicationFilterProviderFactory)
     */
    public UrlSpaceConfigurer replicationFilterProvider(ReplicationFilterProviderFactory replicationFilterProvider) {
        urlSpaceFactoryBean.setReplicationFilterProvider(replicationFilterProvider);
        return this;
    }

    /**
     * @see org.openspaces.core.space.UrlSpaceFactoryBean#setExternalDataSource(com.gigaspaces.datasource.ManagedDataSource)
     */
    public UrlSpaceConfigurer externalDataSource(ManagedDataSource externalDataSource) {
        urlSpaceFactoryBean.setExternalDataSource(externalDataSource);
        return this;
    }

    /**
     * @see org.openspaces.core.space.UrlSpaceFactoryBean#setClusterInfo(org.openspaces.core.cluster.ClusterInfo)
     */
    public UrlSpaceConfigurer clusterInfo(ClusterInfo clusterInfo) {
        urlSpaceFactoryBean.setClusterInfo(clusterInfo);
        return this;
    }

    /**
     * @see org.openspaces.core.space.AbstractSpaceFactoryBean#setRegisterForSpaceModeNotifications(boolean)
     */
    public UrlSpaceConfigurer registerForSpaceModeNotifications(boolean registerForSpaceMode) {
        urlSpaceFactoryBean.setRegisterForSpaceModeNotifications(registerForSpaceMode);
        return this;
    }

    /**
     * @see org.openspaces.core.space.AbstractSpaceFactoryBean#setSecurityConfig(SecurityConfig)
     */
    public UrlSpaceConfigurer securityConfig(SecurityConfig securityConfig) {
        urlSpaceFactoryBean.setSecurityConfig(securityConfig);
        return this;
    }

    /**
     * Sets a custom primary backup listener
     */
    public UrlSpaceConfigurer primaryBackupListener(ISpaceModeListener primaryBackupListener) {
        urlSpaceFactoryBean.setPrimaryBackupListener(primaryBackupListener);
        return this;
    }

    /**
     * Creates or finds (if not already created) a new Space by calling
     * {@link UrlSpaceFactoryBean#afterPropertiesSet()}.
     */
    public IJSpace space() {
        if (space == null) {
            urlSpaceFactoryBean.setParameters(parameters);
            urlSpaceFactoryBean.setProperties(properties);
            urlSpaceFactoryBean.setUrlProperties(urlProperties);
            urlSpaceFactoryBean.setFilterProviders(filterProviderFactories.toArray(new FilterProviderFactory[filterProviderFactories.size()]));
            urlSpaceFactoryBean.afterPropertiesSet();
            space = (IJSpace) urlSpaceFactoryBean.getObject();
        }
        return space;
    }

    /**
     * Destroys the Space by calling {@link UrlSpaceFactoryBean#destroy()}.
     */
    public void destroy() throws Exception {
        urlSpaceFactoryBean.destroy();
    }
}
