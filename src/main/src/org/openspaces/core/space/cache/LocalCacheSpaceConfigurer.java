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

/**
 * A simple configurer helper to create {@link IJSpace} local cache. The configurer wraps
 * {@link LocalCacheSpaceFactoryBean} and providing a simpler means
 * to configure it using code.
 *
 * <p>An example of using it:
 * <pre>
 * UrlSpaceConfigurer urlSpaceConfigurer = new UrlSpaceConfigurer("/./space").schema("persistent")
 *          .noWriteLeaseMode(true).lookupGroups(new String[] {"kimchy"});
 * IJSpace space = urlSpaceConfigurer.space();
 *
 * LocalCacheSpaceConfigurer localCacheConfigurer = new LocalCacheSpaceConfigurer(space).updateMode(UpdateMode.PULL);
 * IJSpace localCache = localCacheConfigurer.localCache();
 * ...
 * localCacheConfigurer.destroy();
 * urlSpaceConfigurer.destroy(); // optional
 * </pre>
 *
 * @author kimchy
 */
public class LocalCacheSpaceConfigurer {

    public static enum UpdateMode {
        PULL,
        PUSH
    }

    private LocalCacheSpaceFactoryBean localCacheSpaceFactoryBean;

    private IJSpace space;

    private Properties properties = new Properties();

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

    public LocalCacheSpaceConfigurer updateMode(UpdateMode mode) {
        if (mode == UpdateMode.PULL) {
            localCacheSpaceFactoryBean.setUpdateModeName(LocalCacheSpaceFactoryBean.LOCAL_CACHE_UPDATE_MODE_PULL);
        } else if (mode == UpdateMode.PUSH) {
            localCacheSpaceFactoryBean.setUpdateModeName(LocalCacheSpaceFactoryBean.LOCAL_CACHE_UPDATE_MODE_PUSH);
        }
        return this;
    }

    public IJSpace localCache() {
        if (space == null) {
            localCacheSpaceFactoryBean.setProperties(properties);
            localCacheSpaceFactoryBean.afterPropertiesSet();
            space = (IJSpace) localCacheSpaceFactoryBean.getObject();
        }
        return this.space;
    }

    public void destroy() {
        localCacheSpaceFactoryBean.destroy();
    }
}
