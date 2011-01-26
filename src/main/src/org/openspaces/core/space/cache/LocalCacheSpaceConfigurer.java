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

import com.j_spaces.core.IJSpace;

import java.util.Properties;

import org.openspaces.core.space.SpaceConfigurer;

/**
 * A simple configurer helper to create {@link IJSpace} local cache. The configurer wraps
 * {@link LocalCacheSpaceFactoryBean} and providing a simpler means
 * to configure it using code.
 *
 * <p>An example of using it:
 * <pre>
 * UrlSpaceConfigurer urlSpaceConfigurer = new UrlSpaceConfigurer("/./space").schema("cache")
 *          .noWriteLeaseMode(true).lookupGroups(new String[] {"kimchy"});
 *
 * LocalCacheSpaceConfigurer localCacheConfigurer = new LocalCacheSpaceConfigurer(urlSpaceConfigurer).updateMode(UpdateMode.PULL);
 * GigaSpace localCacheGigaSpace = new GigaSpaceConfigurer(localCacheConfigurer).gigaSpace();
 * ...
 * localCacheConfigurer.destroy();
 * urlSpaceConfigurer.destroy();
 * </pre>
 *
 * @author kimchy
 */
public class LocalCacheSpaceConfigurer implements SpaceConfigurer {

    public static enum UpdateMode {
        PULL,
        PUSH
    }

    private LocalCacheSpaceFactoryBean localCacheSpaceFactoryBean;

    private IJSpace space;

    private Properties properties = new Properties();

    public LocalCacheSpaceConfigurer(SpaceConfigurer spaceConfigurer) {
        this(spaceConfigurer.space());
    }

    public LocalCacheSpaceConfigurer(IJSpace space) {
        localCacheSpaceFactoryBean = new LocalCacheSpaceFactoryBean();
        localCacheSpaceFactoryBean.setSpace(space);
    }

    /**
     * @see LocalCacheSpaceFactoryBean#setProperties(java.util.Properties)
     */
    public LocalCacheSpaceConfigurer addProperty(String name, String value) {
        properties.setProperty(name, value);
        return this;
    }

    /**
     * @see org.openspaces.core.space.cache.LocalCacheSpaceFactoryBean#setUpdateModeName(String)
     */
    public LocalCacheSpaceConfigurer updateMode(UpdateMode mode) {
        if (mode == UpdateMode.PULL) {
            localCacheSpaceFactoryBean.setUpdateModeName(LocalCacheSpaceFactoryBean.LOCAL_CACHE_UPDATE_MODE_PULL);
        } else if (mode == UpdateMode.PUSH) {
            localCacheSpaceFactoryBean.setUpdateModeName(LocalCacheSpaceFactoryBean.LOCAL_CACHE_UPDATE_MODE_PUSH);
        }
        return this;
    }

    /**
     * @see org.openspaces.core.space.cache.LocalCacheSpaceFactoryBean#setSize(int)
     */
    public LocalCacheSpaceConfigurer size(int size) {
        localCacheSpaceFactoryBean.setSize(size);
        return this;
    }

    /**
     * Creates and returns a local cache according to the configured settings.
     * @return A configured space.
     * @since 8.0
     */
    public IJSpace create() {
        if (space == null) {
            localCacheSpaceFactoryBean.setProperties(properties);
            localCacheSpaceFactoryBean.afterPropertiesSet();
            space = (IJSpace) localCacheSpaceFactoryBean.getObject();
        }
        return this.space;
    }
    public IJSpace localCache() {
        return create();
    }

    public void destroy() {
        localCacheSpaceFactoryBean.destroy();
    }

    public IJSpace space() {
        return create();
    }
}
