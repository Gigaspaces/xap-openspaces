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

    private final LocalViewConfig config;
    
    public LocalViewSpaceFactoryBean() {
        this.config = new LocalViewConfig();
    }
    /**
     * Sets an array of filters/views that define what portion of the data from the master space
     * will be streamed to this local view.
     */
    public void setLocalViews(List<Object> viewTemplates) {
        config.setViewTemplates(viewTemplates);
    }
    
    public void addViewTemplate(Object viewTemplate) {
        if (this.config.getViewTemplates() == null)
            this.config.setViewTemplates(new ArrayList<Object>());
        this.config.getViewTemplates().add(viewTemplate);
    }
    
    public void setBatchSize(int batchSize) {
        config.setBatchSize(batchSize);
    }

    public void setBatchTimeout(long batchTimeout) {
        config.setBatchTimeout(batchTimeout);
    }

    public void setMaxStaleDuration(long maxStaleDuration) {
        config.setMaxStaleDuration(maxStaleDuration);
    }
    
    public void setLocalViewType(LocalViewType localViewType) {
        config.setLocalViewType(localViewType);
    }

    /**
     * Creates the space view 
     */
    @Override
    protected IJSpace createCache(IDirectSpaceProxy remoteSpace, Properties props, SpaceURL spaceUrl) {
        Assert.notNull(config.getViewTemplates(), "localViews must be set");
        Assert.isTrue(config.getViewTemplates().size() > 0, "At least one local view must be defined");
        config.setRemoteSpaceUrl(spaceUrl);
        config.setCustomProperties(props);
        
        try {
            return SpaceCacheFactory.createLocalView(remoteSpace, config);
        } catch (SpaceCacheException e) {
            throw new CannotCreateSpaceException("Failed to create local view for space [" + remoteSpace + "]", e);
        }
    }
}
