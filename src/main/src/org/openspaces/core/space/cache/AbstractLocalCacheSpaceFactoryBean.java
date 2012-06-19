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

package org.openspaces.core.space.cache;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.space.SpaceServiceDetails;
import org.openspaces.pu.service.ServiceDetails;
import org.openspaces.pu.service.ServiceDetailsProvider;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.gigaspaces.internal.client.cache.ISpaceCache;
import com.gigaspaces.internal.client.cache.SpaceCacheConfig;
import com.gigaspaces.internal.client.spaceproxy.IDirectSpaceProxy;
import com.j_spaces.core.IJSpace;

/**
 * Base class for different Local cache space proxies that work with a master {@link IJSpace}. The
 * master is set using {@link #setSpace(IJSpace)}. This factory represents an {@link IJSpace} that
 * is the local cache proxy on top of the master space.
 *
 * <p>
 * Allows to set additional properties that further configure the local cache using
 * {@link #setProperties(Properties)}.
 *
 * @author kimchy
 */
public abstract class AbstractLocalCacheSpaceFactoryBean implements InitializingBean, DisposableBean, FactoryBean, BeanNameAware, ServiceDetailsProvider {

    protected Log logger = LogFactory.getLog(this.getClass());

    private IJSpace space;
    private String beanName;
    private IJSpace localCacheSpace;

    /**
     * Sets the master space that a local cache will be built on top.
     */
    public void setSpace(IJSpace space) {
        this.space = space;
    }

    /**
     * Sets additional properties for the local cache.
     */
    public void setProperties(Properties properties) {
        getCacheConfig().setCustomProperties(properties);
    }
    
    public void addProperty(String name, String value) {
        getCacheConfig().getCustomProperties().setProperty(name, value);
    }

    public void setBatchSize(int batchSize) {
        getCacheConfig().setBatchSize(batchSize);
    }

    public void setBatchTimeout(long batchTimeout) {
        getCacheConfig().setBatchTimeout(batchTimeout);
    }

    public void setMaxDisconnectionDuration(long maxDisconnectionDuration) {
        getCacheConfig().setMaxDisconnectionDuration(maxDisconnectionDuration);
    }

    /**
     * Spring callback that sets the bean name.
     */
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    protected String getBeanName() {
        return this.beanName;
    }

    /**
     * Constructs a new local cache {@link IJSpace} based on the master local cache set using
     * {@link #setSpace(IJSpace)} and a set of properties driving the actual local cache type from the configuration.
     * Additional properties are applied based on {@link #setProperties(java.util.Properties)}.
     */
    @Override
    public void afterPropertiesSet() {
        validate();

        localCacheSpace = createCache((IDirectSpaceProxy) space);
    }

    protected abstract IJSpace createCache(IDirectSpaceProxy remoteSpace);
    
    /**
     * Closes the local cache space
     */
    @Override
    public void destroy() {
        if (localCacheSpace instanceof ISpaceCache) {
            ((ISpaceCache) localCacheSpace).close();
        }
    }

    protected void validate() {
        Assert.notNull(space, "space property must be set");
        Assert.isInstanceOf(IDirectSpaceProxy.class, space, "unsupported space proxy class: " + space.getClass().getName());
    }
        
    protected abstract SpaceCacheConfig getCacheConfig();

    /**
     * Returns an {@link com.j_spaces.core.IJSpace IJSpace} that is the local cache wrapping the
     * master proxy set using {@link #setSpace(com.j_spaces.core.IJSpace)}.
     */
    @Override
    public Object getObject() {
        return this.localCacheSpace;
    }

    /**
     * Returns the type of the factory object.
     */
    @Override
    public Class<? extends IJSpace> getObjectType() {
        return (localCacheSpace == null ? IJSpace.class : localCacheSpace.getClass());
    }

    /**
     * Returns <code>true</code> since this bean is a singleton.
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public ServiceDetails[] getServicesDetails() {
        return new ServiceDetails[] {new SpaceServiceDetails(beanName, localCacheSpace)};
    }
}
