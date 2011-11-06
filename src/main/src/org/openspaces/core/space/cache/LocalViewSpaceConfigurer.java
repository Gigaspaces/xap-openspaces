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
import com.j_spaces.core.client.SQLQuery;
import com.j_spaces.core.client.view.View;

import org.openspaces.core.space.SpaceConfigurer;

/**
 * A simple configurer helper to create {@link IJSpace} local view. The configurer wraps
 * {@link LocalViewSpaceFactoryBean} and providing a simpler means
 * to configure it using code.
 *
 * <p>An example of using it:
 * <pre>
 * UrlSpaceConfigurer urlSpaceConfigurer = new UrlSpaceConfigurer("/./space").schema("persistent")
 *          .noWriteLeaseMode(true).lookupGroups(new String[] {"kimchy"});
 *
 * LocalViewSpaceConfigurer localViewConfigurer = new LocalViewSpaceConfigurer(urlSpaceConfigurer)
 *           .addView(new View(SimpleMessage.class, "processed = true"));
 * GigaSpace localViewGigaSpace = new GigaSpaceConfigurer(localViewConfigurer).gigaSpace();
 * ...
 * localViewConfigurer.destroy();
 * urlSpaceConfigurer.destroy(); // optional
 * </pre>
 *
 * @author kimchy
 */
public class LocalViewSpaceConfigurer implements SpaceConfigurer {

    private LocalViewSpaceFactoryBean localViewSpaceFactoryBean;

    private IJSpace space;

    public LocalViewSpaceConfigurer(SpaceConfigurer spaceConfigurer) {
        this(spaceConfigurer.space());
    }

    public LocalViewSpaceConfigurer(IJSpace space) {
        localViewSpaceFactoryBean = new LocalViewSpaceFactoryBean();
        localViewSpaceFactoryBean.setSpace(space);
    }

    /**
     * @see LocalViewSpaceFactoryBean#setProperties(java.util.Properties)
     */
    public LocalViewSpaceConfigurer addProperty(String name, String value) {
        localViewSpaceFactoryBean.addProperty(name, value);
        return this;
    }

    /**
     * @deprecated since 8.0.5 - use {@link #addTemplate(SQLQuery)} instead.
     */
    @Deprecated 
    public LocalViewSpaceConfigurer addView(View view) {
        return addTemplate(view);
    }

    /**
     * Adds a template to the view.
     * @since 8.0.5
     */
    public LocalViewSpaceConfigurer addTemplate(SQLQuery template) {
        localViewSpaceFactoryBean.addViewTemplate(template);
        return this;
    }
    
    /**
     * Sets the local view batch size.
     * @since 8.0.5
     */
    public LocalViewSpaceConfigurer batchSize(int batchSize) {
        localViewSpaceFactoryBean.setBatchSize(batchSize);
        return this;
    }

    /**
     * Sets the local view batch timeout (i.e. maximum time the server will batch entries before updating the client.
     * @since 8.0.5
     */
    public LocalViewSpaceConfigurer batchTimeout(long batchTimeout) {
        localViewSpaceFactoryBean.setBatchTimeout(batchTimeout);
        return this;
    }

    public LocalViewSpaceConfigurer maxDisconnectionDuration(long maxDisconnectionDuration) {
        localViewSpaceFactoryBean.setMaxDisconnectionDuration(maxDisconnectionDuration);
        return this;
    }

    /**
     * Creates and returns a local cache according to the configured settings.
     * @return A configured space.
     * @since 8.0
     */
    public IJSpace create() {
        if (space == null) {
            localViewSpaceFactoryBean.afterPropertiesSet();
            space = (IJSpace) localViewSpaceFactoryBean.getObject();
        }
        return this.space;
    }
    public IJSpace localView() {
        return create();
    }
    public void destroy() {
        localViewSpaceFactoryBean.destroy();
    }

    public IJSpace space() {
        return create();
    }
}