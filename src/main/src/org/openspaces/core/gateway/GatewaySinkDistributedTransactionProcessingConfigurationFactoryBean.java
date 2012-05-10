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
package org.openspaces.core.gateway;

import org.openspaces.core.transaction.DistributedTransactionProcessingConfigurationFactoryBean;

import com.gigaspaces.cluster.replication.gateway.transaction.AbortOnConsolidationAbortedInterceptor;
import com.gigaspaces.cluster.replication.gateway.transaction.ExecuteOnConsolidationAbortedInterceptor;
import com.gigaspaces.cluster.replication.gateway.transaction.TransactionConsolidationInterceptor;
import com.gigaspaces.internal.cluster.node.impl.gateway.sink.GatewaySinkDistributedTransactionProcessingConfiguration;
import com.gigaspaces.internal.utils.StringUtils;


/**
 * A bean for configuring distributed transaction processing at Sink component.
 * 
 * Its possible to configure two parameters:
 * <ul>
 * <li>
 * {@link #setDistributedTransactionWaitTimeout(Long)} - determines the wait timeout for all distributed transaction participants data
 * before committing only the data that arrived.
 * </li>
 * <li>
 * {@link #setDistributedTransactionWaitForOperations(Long)} - determines the number of operations to wait for before committing
 *  a distributed transaction when data from all participants haven't arrived.  
 * </li>
 * </ul>
 * 
 * @author eitany
 * @since 9.0.1
 */
public class GatewaySinkDistributedTransactionProcessingConfigurationFactoryBean extends
        DistributedTransactionProcessingConfigurationFactoryBean {
    
    private String distributedTransactionConsolidationFailureAction;
    private TransactionConsolidationInterceptor distributedTransactionConsolidationInterceptor;
    
    /**
     * @param distributedTransactionConsolidationFailureAction the distributedTransactionConsolidationFailedAction to set
     */
    public void setDistributedTransactionConsolidationFailureAction(
            String distributedTransactionConsolidationFailureAction) {
        this.distributedTransactionConsolidationFailureAction = distributedTransactionConsolidationFailureAction;
    }
    
    /**
     * @return the distributedTransactionConsolidationFailedAction
     */
    public String getDistributedTransactionConsolidationFailureAction() {
        return distributedTransactionConsolidationFailureAction;
    }
    
    /**
     * @param distributedTransactionConsolidationInterceptor the distributedTransactionConsolidationIntereceptor to set
     */
    public void setDistributedTransactionConsolidationInterceptor(
            TransactionConsolidationInterceptor distributedTransactionConsolidationInterceptor) {
        this.distributedTransactionConsolidationInterceptor = distributedTransactionConsolidationInterceptor;
    }
    
    /**
     * @return the distributedTransactionConsolidationIntereceptor
     */
    public TransactionConsolidationInterceptor getDistributedTransactionConsolidationInterceptor() {
        return distributedTransactionConsolidationInterceptor;
    }
    
    public void copyParameters(GatewaySinkDistributedTransactionProcessingConfiguration transactionProcessingConfiguration) {
        if (getDistributedTransactionWaitTimeout() != null)
            transactionProcessingConfiguration.setTimeoutBeforePartialCommit(getDistributedTransactionWaitTimeout());
        if (getDistributedTransactionWaitForOperations() != null)
            transactionProcessingConfiguration.setWaitForOperationsBeforePartialCommit(getDistributedTransactionWaitForOperations());
        TransactionConsolidationInterceptor interceptor = null;
        if (StringUtils.hasLength(distributedTransactionConsolidationFailureAction)) {
            if (distributedTransactionConsolidationFailureAction.equals("abort"))
                interceptor = AbortOnConsolidationAbortedInterceptor.INSTANCE;
            else
                interceptor = ExecuteOnConsolidationAbortedInterceptor.INSTANCE;
        }
        if (distributedTransactionConsolidationInterceptor != null)
        {
            if (interceptor != null)
                throw new IllegalStateException("Cannot specify both distributed transaction consolidation failed action and a custom transaction consolidation interceptor");
            
            interceptor = distributedTransactionConsolidationInterceptor;
        }
        
        if (interceptor != null)
            transactionProcessingConfiguration.setTransactionConsolidationInterceptor(interceptor);
    }
    
}
