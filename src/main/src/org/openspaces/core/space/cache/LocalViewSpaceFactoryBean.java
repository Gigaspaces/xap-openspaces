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

import com.gigaspaces.internal.client.cache.SpaceCacheException;
import com.gigaspaces.internal.client.cache.SpaceCacheFactory;
import com.gigaspaces.internal.client.cache.LocalViewType;
import com.gigaspaces.internal.client.cache.localview.LocalViewConfig;
import com.gigaspaces.internal.client.spaceproxy.IDirectSpaceProxy;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.SpaceURL;

import org.openspaces.core.space.CannotCreateSpaceException;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * The space local view proxy maintains a subset of the master space's data, allowing the client to
 * read distributed data without any remote operations.
 *
 * <p>
 * Data is streamed into the client view in an implicit manner - as opposed to local cache data ({@link LocalCacheSpaceFactoryBean}),
 * that is loaded into the client on- demand, and later updated or evicted.
 *
 * <p>
 * The local view is defined via a filter ({@link #setLocalViews(java.util.List)} localViews) - as
 * opposed to the local cache that caches data at the client side only after the data has been read
 *
 * @author kimchy
 */
public class LocalViewSpaceFactoryBean extends AbstractLocalCacheSpaceFactoryBean {

    private List<Object> viewTemplates;
    private Integer batchSize;
    private Long batchTimeout;
    private Long maxStaleDuration;
    private LocalViewType localViewType = LocalViewType.DEFAULT;
    
    /**
     * Sets an array of filters/views that define what portion of the data from the master space
     * will be streamed to this local view.
     */
    public void setLocalViews(List<Object> viewTemplates) {
        this.viewTemplates = viewTemplates;
    }
    
    public void addViewTemplate(Object viewTemplate) {
        if (this.viewTemplates == null)
            this.viewTemplates = new ArrayList<Object>();
        this.viewTemplates.add(viewTemplate);
    }
    
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setBatchTimeout(long batchTimeout) {
        this.batchTimeout = batchTimeout;
    }

    public void setMaxStaleDuration(long maxStaleDuration) {
        this.maxStaleDuration = maxStaleDuration;
    }
    
    public void setLocalViewType(LocalViewType localViewType) {
        this.localViewType = localViewType;
    }

    /**
     * Creates the space view 
     */
    @Override
    protected IJSpace createCache(IDirectSpaceProxy remoteSpace, Properties props, SpaceURL spaceUrl) {
        Assert.notNull(viewTemplates, "localViews must be set");
        Assert.isTrue(viewTemplates.size() > 0, "At least one local view must be defined");
        
        try {
            return SpaceCacheFactory.createLocalView(remoteSpace, createLocalViewConfig(props, spaceUrl));
        } catch (SpaceCacheException e) {
            throw new CannotCreateSpaceException("Failed to create local view for space [" + remoteSpace + "]", e);
        }
    }

    private LocalViewConfig createLocalViewConfig(Properties props, SpaceURL spaceUrl) {
        LocalViewConfig config = new LocalViewConfig(props, spaceUrl, this.localViewType, this.viewTemplates);
        if (this.batchSize != null)
            config.setBatchSize(this.batchSize);
        if (this.batchTimeout != null)
            config.setBatchTimeout(this.batchTimeout);
        if (this.maxStaleDuration != null)
            config.setMaxStaleDuration(this.maxStaleDuration);
        return config;
    }
}
