/*******************************************************************************
 * 
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
 *  
 ******************************************************************************/
package org.openspaces.core.gateway;

import com.gigaspaces.cluster.replication.gateway.conflict.ConflictResolver;
import com.gigaspaces.internal.cluster.node.impl.gateway.sink.LocalClusterReplicationSinkConfig;

/**
 * A factory bean used for configuring the Sink component error handling
 * configuration.
 * 
 * @author idan
 * @since 8.0.3
 *
 */
public class SinkErrorHandlingFactoryBean {

    private ConflictResolver conflictResolver;
    private Integer maximumRetriesOnTransactionLock;
    private Integer transactionLockRetryInterval;

    /**
     * Sets the maximum retries number of a failed operation due to transaction lock.
     * @param maximumRetriesOnTransactionLock Maximum retries number.
     */
    public void setMaximumRetriesOnTransactionLock(Integer maximumRetriesOnTransactionLock) {
        this.maximumRetriesOnTransactionLock = maximumRetriesOnTransactionLock;
    }
    
    /**
     * Gets the maximum retries number defined for a failed operation which failed due to transaction lock.
     * @return Maximum retries count.
     */
    public Integer getMaximumRetriesOnTransactionLock() {
        return maximumRetriesOnTransactionLock;
    }
    
    /**
     * Sets the time interval between failed operations retry attempts.
     * @param transactionLockRetryInterval Time interval between retry attempts.
     */
    public void setTransactionLockRetryInterval(Integer transactionLockRetryInterval) {
        this.transactionLockRetryInterval = transactionLockRetryInterval;
    }
    
    /**
     * Gets the defined time interval between failed operations retry attempts.
     * @return Time interval between attempts.
     */
    public Integer getTransactionLockRetryInterval() {
        return transactionLockRetryInterval;
    }

    /**
     * Sets the {@link ConflictResolver} implementation to be used with the Sink component.
     * @param conflictResolver The {@link ConflictResolver} implementation.
     */
    public void setConflictResolver(ConflictResolver conflictResolver) {
        this.conflictResolver = conflictResolver;
    }

    /**
     * Gets the {@link ConflictResolver} implementation which is used with the Sink component.
     * @return The {@link ConflictResolver} implementation.
     */
    public ConflictResolver getConflictResolver() {
        return conflictResolver;
    }

    protected void copyToSinkConfiguration(LocalClusterReplicationSinkConfig config) {
        if (conflictResolver != null)
            config.setConflictResolver(conflictResolver);
        if (maximumRetriesOnTransactionLock != null)
            config.setMaximumRetriesOnTransactionLock(maximumRetriesOnTransactionLock);
        if (transactionLockRetryInterval != null)
            config.setTransactionLockRetryInterval(transactionLockRetryInterval);
    }

}
