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

import com.gigaspaces.internal.client.cache.SpaceCacheConfig;
import com.gigaspaces.internal.client.cache.SpaceCacheFactory;
import com.gigaspaces.internal.client.cache.SpaceCacheInitializationException;
import com.gigaspaces.internal.client.cache.localcache.LocalCacheConfig;
import com.gigaspaces.internal.client.spaceproxy.IDirectSpaceProxy;
import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.IJSpace;

import org.openspaces.core.space.CannotCreateSpaceException;
import org.openspaces.core.space.cache.LocalCacheSpaceConfigurer.UpdateMode;

/**
 * In some cases, the memory capacity of an individual application is not capable of holding all the
 * information in the local application memory. When this happens, the desired solution will be to
 * hold only a portion of the information in the application's memory and the rest in a separate
 * process(s). This mode is also known as two-level cache. In this mode, the cache is divided into
 * two components, local cache and master cache. The local cache always resides in the physical
 * address space of the application and the master cache runs in a different process. The master
 * cache is used to share data among the different embedded local caches running within other
 * application instances.
 * 
 * <p>
 * In this mode, when a read/get operation is called, a matching object is first looked up in the
 * local embedded cache. If the object is not found in the local cache, it will be searched for in
 * the master cache. If it is not found in the master cache, it will reload the data from the data
 * source. Updates on the central cache will be propagated into all local embedded cache instances
 * in either pull or push mode, using unicast or multicast protocol.
 * 
 * @author kimchy
 */
public class LocalCacheSpaceFactoryBean extends AbstractLocalCacheSpaceFactoryBean {

    public static final String LOCAL_CACHE_UPDATE_MODE_PUSH = "push";

    public static final String LOCAL_CACHE_UPDATE_MODE_PULL = "pull";

    private final LocalCacheConfig config;

    public LocalCacheSpaceFactoryBean() {
    	this.config = new LocalCacheConfig();
    }

    /**
     * If set to {@link SpaceURL#UPDATE_MODE_PULL} (<code>1</code>) each update triggers an
     * invalidation event at every cache instance. The invalidate event marks the object in the
     * local cache instances as invalid. Therefore, an attempt to read this object triggers a reload
     * process in the master space. This configuration is useful in cases where objects are updated
     * frequently, but the updated value is required by the application less frequently.
     * 
     * <p>
     * If set to {@link SpaceURL#UPDATE_MODE_PUSH} (<code>2</code>) the master pushes the
     * updates to the local cache, which holds a reference to the same updated object.
     * 
     * @see #setUpdateModeName(String)
     */
    public void setUpdateMode(int localCacheUpdateMode) {
    	config.setUpdateMode(localCacheUpdateMode);
    }

    public void setUpdateMode(UpdateMode localCacheUpdateMode) {
        if (localCacheUpdateMode == UpdateMode.NONE)
            config.setUpdateMode(SpaceURL.UPDATE_MODE_NONE);
        else if (localCacheUpdateMode == UpdateMode.PULL)
            config.setUpdateMode(SpaceURL.UPDATE_MODE_PULL);
    	else if (localCacheUpdateMode == UpdateMode.PUSH)
            config.setUpdateMode(SpaceURL.UPDATE_MODE_PUSH);
    }

    /**
     * Allows to set the local cache update mode using a descriptive name instead of integer
     * constants using {@link #setUpdateMode(int) localCacheUpdateMode}. Accepts either
     * <code>push</code> or <code>pull</code>.
     * 
     * @see #setUpdateMode (int)
     */
    public void setUpdateModeName(String localCacheUpdateModeName) {
        if (LOCAL_CACHE_UPDATE_MODE_PULL.equalsIgnoreCase(localCacheUpdateModeName)) {
            setUpdateMode(SpaceURL.UPDATE_MODE_PULL);
        } else if (LOCAL_CACHE_UPDATE_MODE_PUSH.equalsIgnoreCase(localCacheUpdateModeName)) {
            setUpdateMode(SpaceURL.UPDATE_MODE_PUSH);
        } else {
            throw new IllegalArgumentException("Wrong localCacheUpdateModeName [" + localCacheUpdateModeName + "], "
                    + "should be either '" + LOCAL_CACHE_UPDATE_MODE_PULL + "' or '" + LOCAL_CACHE_UPDATE_MODE_PUSH
                    + "'");
        }
    }
    
    public void setMaxTimeToLive(long maxTimeToLive) {
        this.config.setMaxTimeToLive(maxTimeToLive);
    }

    /**
     * Sets the local cache size.
     */
    public void setSize(int size) {
        this.config.setSize(size);
    }


    @Override
    protected SpaceCacheConfig getCacheConfig() {
        return this.config;
    }
    
    /**
     * Creates the local cache. 
     */
    @Override
    protected IJSpace createCache(IDirectSpaceProxy remoteSpace) {
        try {
            return SpaceCacheFactory.createLocalCache(remoteSpace, config);
        } catch (SpaceCacheInitializationException e) {
            throw new CannotCreateSpaceException("Failed to create local cache for space [" + remoteSpace + "]", e);
        }
    }
}
