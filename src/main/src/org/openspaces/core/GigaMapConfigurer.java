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

package org.openspaces.core;

import com.j_spaces.map.IMap;
import org.openspaces.core.exception.ExceptionTranslator;
import org.openspaces.core.transaction.TransactionProvider;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * A simple programmatic configurer for {@link org.openspaces.core.GigaMap} instance wrapping
 * the {@link GigaMapFactoryBean}.
 *
 * <p>Usage example:
 * <pre>
 * UrlSpaceConfigurer urlSpaceConfigurer = new UrlSpaceConfigurer("/./space").schema("persistent")
 *          .noWriteLeaseMode(true).lookupGroups(new String[] {"kimchy"});
 * IJSpace space = urlSpaceConfigurer.space();
 * IMap map = new MapConfigurer(space).localCachePutFirst(true).map();
 *
 * GigaMap gigaMap = new GigaMapConfigurer(map).gigaMap();
 * ...
 * urlSpaceConfigurer.destroySpace(); // optional
 * </pre>
 *
 * @author kimchy
 */
public class GigaMapConfigurer {

    final private GigaMapFactoryBean gigaMapFactoryBean;

    private GigaMap gigaMap;

    public GigaMapConfigurer(IMap map) {
        gigaMapFactoryBean = new GigaMapFactoryBean();
        gigaMapFactoryBean.setMap(map);
    }

    /**
     * @see org.openspaces.core.GigaMapFactoryBean#setTxProvider(org.openspaces.core.transaction.TransactionProvider)
     */
    public GigaMapConfigurer txProvider(TransactionProvider txProvider) {
        gigaMapFactoryBean.setTxProvider(txProvider);
        return this;
    }

    /**
     * @see org.openspaces.core.GigaMapFactoryBean#setExTranslator(org.openspaces.core.exception.ExceptionTranslator)
     */
    public GigaMapConfigurer exTranslator(ExceptionTranslator exTranslator) {
        gigaMapFactoryBean.setExTranslator(exTranslator);
        return this;
    }

    /**
     * @see org.openspaces.core.GigaMapFactoryBean#setDefaultWaitForResponse(long)
     */
    public GigaMapConfigurer setDefaultWaitForResponse(long defaultWaitForResponse) {
        gigaMapFactoryBean.setDefaultWaitForResponse(defaultWaitForResponse);
        return this;
    }

    /**
     * @see org.openspaces.core.GigaMapFactoryBean#setDefaultTimeToLive(long)
     */
    public GigaMapConfigurer defaultTimeToLive(long defaultTimeToLive) {
        gigaMapFactoryBean.setDefaultTimeToLive(defaultTimeToLive);
        return this;
    }

    /**
     * @see org.openspaces.core.GigaMapFactoryBean#setDefaultLockTimeToLive(long)
     */
    public GigaMapConfigurer defaultLockTimeToLive(long defaultLockTimeToLive) {
        gigaMapFactoryBean.setDefaultLockTimeToLive(defaultLockTimeToLive);
        return this;
    }

    /**
     * @see org.openspaces.core.GigaMapFactoryBean#setDefaultWaitingForLockTimeout(long)
     */
    public GigaMapConfigurer defaultWaitingForLockTimeout(long defaultWaitingForLockTimeout) {
        gigaMapFactoryBean.setDefaultWaitingForLockTimeout(defaultWaitingForLockTimeout);
        return this;
    }

    /**
     * @see org.openspaces.core.GigaMapFactoryBean#setDefaultIsolationLevel(int)
     */
    public GigaMapConfigurer defaultIsolationLevel(int defaultIsolationLevel) {
        gigaMapFactoryBean.setDefaultIsolationLevel(defaultIsolationLevel);
        return this;
    }

    /**
     * @see org.openspaces.core.GigaMapFactoryBean#setTransactionManager(org.springframework.transaction.PlatformTransactionManager)
     */
    public GigaMapConfigurer transactionManager(PlatformTransactionManager transactionManager) {
        gigaMapFactoryBean.setTransactionManager(transactionManager);
        return this;
    }

    /**
     * Creates a new {@link org.openspaces.core.GigaMap} instance if non already created.
     */
    public GigaMap gigaMap() {
        if (gigaMap == null) {
            gigaMapFactoryBean.afterPropertiesSet();
            gigaMap = (GigaMap) gigaMapFactoryBean.getObject();
        }
        return gigaMap;
    }
}
