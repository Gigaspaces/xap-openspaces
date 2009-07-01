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

import com.gigaspaces.internal.client.dcache.ISpaceLocalCache;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.FinderException;
import com.j_spaces.core.client.SpaceFinder;
import com.j_spaces.core.client.SpaceURL;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.space.CannotCreateSpaceException;
import org.openspaces.core.space.SpaceServiceDetails;
import org.openspaces.pu.service.ServiceDetailsProvider;
import org.openspaces.pu.service.ServiceDetails;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.Properties;

/**
 * Base class for different Local cache space proxies that work with a master {@link IJSpace}. The
 * master is set using {@link #setSpace(IJSpace)}. This factory represents an {@link IJSpace} that
 * is the local cache proxy on top of the master space.
 *
 * <p>
 * Allows to set additional properties that further configure the local cache using
 * {@link #setProperties(Properties)}. Properties that control the nature of the local cache are
 * obtained using {@link #createCacheProperties()} callback.
 *
 * @author kimchy
 */
public abstract class AbstractLocalCacheSpaceFactoryBean implements InitializingBean, DisposableBean, FactoryBean, BeanNameAware, ServiceDetailsProvider {

    protected Log logger = LogFactory.getLog(this.getClass());

    private IJSpace space;

    private Properties properties;

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
        this.properties = properties;
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
     * {@link #setSpace(IJSpace)} and a set of properties driving the actual local cache type based
     * on {@link #createCacheProperties()}. Additional properties are applied based on
     * {@link #setProperties(java.util.Properties)}.
     *
     * @see com.j_spaces.core.client.SpaceFinder#find(com.j_spaces.core.client.SpaceURL,com.j_spaces.core.IJSpace,com.sun.jini.start.LifeCycle)
     */
    public void afterPropertiesSet() {
        Assert.notNull(space, "space property must be set");
        IJSpace actualSpace = space;
        Properties props = createCacheProperties();
        props.put(SpaceURL.USE_LOCAL_CACHE, "true");
        if (properties != null) {
            props.putAll(properties);
        }

        SpaceURL spaceUrl = (SpaceURL) space.getFinderURL().clone();
        spaceUrl.putAll(props);
        spaceUrl.getCustomProperties().putAll(props);
        propereUrl(spaceUrl);
        try {
            localCacheSpace = (IJSpace) SpaceFinder.find(spaceUrl, actualSpace, null);
        } catch (FinderException e) {
            throw new CannotCreateSpaceException("Failed to create local cache space for space [" + space + "]", e);
        }
    }

    /**
     * Closes the local cache space
     */
    public void destroy() {
        if (localCacheSpace instanceof ISpaceLocalCache) {
            ((ISpaceLocalCache) localCacheSpace).close();
        }
    }

    /**
     * Subclasses should implement this method to return the properties relevant for the local
     * concrete local cache implementation.
     */
    protected abstract Properties createCacheProperties();

    protected void propereUrl(SpaceURL spaceURL) {
        
    }

    /**
     * Returns an {@link com.j_spaces.core.IJSpace IJSpace} that is the local cache wrapping the
     * master proxy set using {@link #setSpace(com.j_spaces.core.IJSpace)}.
     */
    public Object getObject() {
        return this.localCacheSpace;
    }

    /**
     * Returns the type of the factory object.
     */
    public Class<? extends IJSpace> getObjectType() {
        return (localCacheSpace == null ? IJSpace.class : localCacheSpace.getClass());
    }

    /**
     * Returns <code>true</code> since this bean is a singleton.
     */
    public boolean isSingleton() {
        return true;
    }

    public ServiceDetails[] getServicesDetails() {
        return new ServiceDetails[] {new SpaceServiceDetails(beanName, localCacheSpace)};
    }
}
