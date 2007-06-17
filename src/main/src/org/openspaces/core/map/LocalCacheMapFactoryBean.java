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

package org.openspaces.core.map;

import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.client.cache.map.MapCache;
import com.j_spaces.javax.cache.EvictionStrategy;
import com.j_spaces.map.IMap;
import com.j_spaces.map.eviction.NoneEvictionStrategy;

import java.util.Properties;

/**
 * Local cache map factory creates a map implementation on top of the Space with a local cache.
 *
 * @author kimchy
 */
public class LocalCacheMapFactoryBean extends AbstractMapFactoryBean {

    private static final String LOCAL_CACHE_UPDATE_MODE_PUSH = "push";

    private static final String LOCAL_CACHE_UPDATE_MODE_PULL = "pull";

    private int localCacheUpdateMode = SpaceURL.UPDATE_MODE_PULL;

    private int compression = 0;

    private boolean versioned = false;

    private EvictionStrategy evictionStrategy;

    private boolean useMulticast = false;

    private boolean putFirst = true;

    private int sizeLimit = 100000;

    private Properties properties = new Properties();

    /**
     * Sets the compression level. Default to <code>0</code>.
     */
    public void setCompression(int compression) {
        this.compression = compression;
    }

    /**
     * Controls if this local cache will be versioned or not. Note, when settings this to
     * <code>true</code>, make sure that the actual space is versioned as well.
     */
    public void setVersioned(boolean versioned) {
        this.versioned = versioned;
    }

    /**
     * Sets the eviction strategy for the local cache.
     */
    public void setEvictionStrategy(EvictionStrategy evictionStrategy) {
        this.evictionStrategy = evictionStrategy;
    }

    /**
     * If set to {@link SpaceURL#UPDATE_MODE_PULL} (<code>1</code>) each update triggers an
     * invalidation event at every cache instance. The invalidate event marks the object in the
     * local cache instances as invalid. Therefore, an attempt to read this object triggers a reload
     * process in the master space. This configuration is useful in cases where objects are updated
     * frequently, but the updated value is required by the application less frequently.
     *
     * <p>If set to {@link SpaceURL#UPDATE_MODE_PUSH} (<code>2</code>) the master pushes the
     * updates to the local cache, which holds a reference to the same updated object.
     *
     * @see #setUpdateModeName(String)
     */
    public void setUpdateMode(int localCacheUpdateMode) {
        this.localCacheUpdateMode = localCacheUpdateMode;
    }

    /**
     * Allows to set the local cahce update mode using a descriptive name instead of integer
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
                    + "shoudl be either '" + LOCAL_CACHE_UPDATE_MODE_PULL + "' or '" + LOCAL_CACHE_UPDATE_MODE_PUSH
                    + "'");
        }
    }

    public void setUseMulticast(boolean useMulticast) {
        this.useMulticast = useMulticast;
    }

    public void setPutFirst(boolean putFirst) {
        this.putFirst = putFirst;
    }

    /**
     * Sets the size limit of the local cache. Default to <code>100000</code>.
     */
    public void setSizeLimit(int sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    protected IMap createMap() throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating local cache map over Space [" + getSpace() + "] with compression");
        }
        if (evictionStrategy == null) {
            evictionStrategy = new NoneEvictionStrategy();
        }

        // TODO Once we have the lookup URL we need to use it instead of the "connected space" url
        SpaceURL spaceUrl = (SpaceURL) getSpace().getURL().clone();
        spaceUrl.putAll(properties);
        spaceUrl.getCustomProperties().putAll(properties);

        return new MapCache(getSpace(), versioned, localCacheUpdateMode, evictionStrategy, useMulticast, putFirst,
                sizeLimit, compression, spaceUrl.getURL());
    }
}