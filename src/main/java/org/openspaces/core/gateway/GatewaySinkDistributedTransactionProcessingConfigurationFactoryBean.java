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

import com.gigaspaces.internal.cluster.node.impl.processlog.multisourcesinglefile.DistributedTransactionProcessingConfiguration;


/**
 * A bean for configuring distributed transaction processing at Sink component.
 * 
 * Its possible to configure three parameters:
 * <ul>
 * <li>
 * {@link #setDistributedTransactionWaitTimeout(Long)} - determines the wait timeout for all distributed transaction participants data
 * before committing only the data that arrived.
 * </li>
 * <li>
 * {@link #setDistributedTransactionWaitForOperations(Long)} - determines the number of operations to wait for before committing
 *  a distributed transaction when data from all participants haven't arrived.  
 * </li>
 * <li>
 * {@link #setDistributedTransactionConsolidationFailureAction(String)} - determines the action to take when a transaction consolidation is failed.  
 * </li>
 * </ul>
 * 
 * @author eitany
 * @since 9.0.1
 */
public class GatewaySinkDistributedTransactionProcessingConfigurationFactoryBean extends
        DistributedTransactionProcessingConfigurationFactoryBean {
    
    private String distributedTransactionConsolidationFailureAction;
    
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
    
    public void copyParameters(DistributedTransactionProcessingConfiguration transactionProcessingConfiguration) {
        if (getDistributedTransactionWaitTimeout() != null)
            transactionProcessingConfiguration.setTimeoutBeforePartialCommit(getDistributedTransactionWaitTimeout());
        if (getDistributedTransactionWaitForOperations() != null)
            transactionProcessingConfiguration.setWaitForOperationsBeforePartialCommit(getDistributedTransactionWaitForOperations());
        if (isMonitorPendingOperationsMemory() != null)
            transactionProcessingConfiguration.setMonitorPendingOperationsMemory(isMonitorPendingOperationsMemory().booleanValue());
    }
    
}
