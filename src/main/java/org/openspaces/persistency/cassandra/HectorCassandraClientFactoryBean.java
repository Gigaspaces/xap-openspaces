/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openspaces.persistency.cassandra;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @since 9.1.1
 * @author Dan Kilman
 */
public class HectorCassandraClientFactoryBean implements
    FactoryBean<HectorCassandraClient>, InitializingBean, DisposableBean {

    private final HectorCassandraClientConfigurer configurer = getConfigurer();

    protected HectorCassandraClientConfigurer getConfigurer() {
        return new HectorCassandraClientConfigurer();
    }

    private HectorCassandraClient hectorCassandraClient;
    
    /**
     * @see HectorCassandraClientConfigurer#hosts(String)
     */
    public void setHosts(String hosts) {
        configurer.hosts(hosts);
    }
    
    /**
     * @see HectorCassandraClientConfigurer#port(int)
     */
    public void setPort(int port) {
        configurer.port(port);
    }
    
    /**
     * @see HectorCassandraClientConfigurer#keyspaceName(String) 
     */
    public void setKeyspaceName(String keyspaceName) {
        configurer.keyspaceName(keyspaceName);
    }

    /**
     * @see HectorCassandraClientConfigurer#clusterName(String)
     */
    public void setClusterName(String clusterName) {
        configurer.clusterName(clusterName);
    }
    
    /**
     * @see HectorCassandraClientConfigurer#columnFamilyGcGraceSeconds(int)
     */
    public void setColumnFamilyGcGraceSeconds(int columnFamilyGcGraceSeconds) {
        configurer.columnFamilyGcGraceSeconds(columnFamilyGcGraceSeconds);
    }
    
    /**
     * @see HectorCassandraClientConfigurer#readConsistencyLevel(CassandraConsistencyLevel)
     */
    public void setReadConsistencyLevel(CassandraConsistencyLevel readConsistencyLevel) {
        configurer.readConsistencyLevel(readConsistencyLevel);
    }
    
    /**
     * @see HectorCassandraClientConfigurer#writeConsistencyLevel(CassandraConsistencyLevel)
     */
    public void setWriteConsistencyLevel(CassandraConsistencyLevel writeConsistencyLevel) {
        configurer.writeConsistencyLevel(writeConsistencyLevel);
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        hectorCassandraClient = configurer.create();
    }

    @Override
    public HectorCassandraClient getObject() throws Exception {
        return hectorCassandraClient;
    }

    @Override
    public Class<?> getObjectType() {
        return HectorCassandraClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void destroy() throws Exception {
        hectorCassandraClient.close();
    }

}
