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

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.cache.ISpaceLocalCache;
import com.j_spaces.map.IMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.util.SpaceUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Base class for different {@link com.j_spaces.map.IMap} factories. Uses the referenced space
 * (using {@link #setSpace(com.j_spaces.core.IJSpace)}) in order to build the map interface around
 * it.
 *
 * <p>Supports the cluster flag controlling if the map will work with a clustered view of the space
 * or directly with a cluster memeber. By default it will work directly with a cluster member if
 * the space was loaded in embedded mode (as if clustered flag was set to <code>true</code>), otherwise
 * it will work with a clustered view.
 *
 * @author kimchy
 */
public abstract class AbstractMapFactoryBean implements InitializingBean, FactoryBean, BeanNameAware {

    protected Log logger = LogFactory.getLog(getClass());

    private IJSpace space;

    private Boolean clustered;

    private String beanName;

    private IMap map;

    /**
     * Sets the Space the Map will be built on top.
     */
    public void setSpace(IJSpace space) {
        this.space = space;
    }

    protected IJSpace getSpace() {
        return space;
    }

    /**
     * <p>Sets the cluster flag controlling if this {@link com.j_spaces.map.IMap} will work with a clustered
     * view of the space or directly with a cluster member. By default if this flag is not set it will be set
     * automatically by this factory. It will be set to <code>true</code> if the space is an embedded one AND
     * the space is not a local cache proxy. It will be set to <code>false</code> otherwise (i.e. the space
     * is not an embedded space OR the space is a local cache proxy).
     *
     * @param clustered If the {@link com.j_spaces.map.IMap} is going to work with a clsutered view of the
     *                  space or directly with a cluster memeber
     */
    public void setClustered(Boolean clustered) {
        this.clustered = clustered;
    }

    /**
     * Injected by Spring with the bean name.
     */
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(space, "space property must be set");
        if (clustered == null) {
            // in case the space is a local cache space, set the clustered flag to true since we do
            // not want to get the actual memeber (the cluster flag was set on the local cache already)
            if (space instanceof ISpaceLocalCache) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Clustered flag automatically set to [" + clustered + "] since the space is a local cache space for bean [" + beanName + "]");
                }
                clustered = true;
            } else {
                clustered = SpaceUtils.isRemoteProtocol(space);
                if (logger.isDebugEnabled()) {
                    logger.debug("Clustered flag automatically set to [" + clustered + "] for bean [" + beanName + "]");
                }
            }
        }
        if (!clustered) {
            space = SpaceUtils.getClusterMemberSpace(space, true);
        }
        map = createMap();
    }

    /**
     * Sub classes will override this method in order to create the {@link com.j_spaces.map.IMap} implementation.
     */
    protected abstract IMap createMap() throws Exception;

    public Object getObject() throws Exception {
        return map;
    }

    public Class getObjectType() {
        return map == null ? IMap.class : map.getClass();
    }

    public boolean isSingleton() {
        return true;
    }
}
