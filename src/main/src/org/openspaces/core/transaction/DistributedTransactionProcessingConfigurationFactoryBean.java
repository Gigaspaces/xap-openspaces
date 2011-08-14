package org.openspaces.core.transaction;

import com.gigaspaces.internal.cluster.node.impl.processlog.multisourcesinglefile.DistributedTransactionProcessingConfiguration;

/**
 * A bean for configuring distributed transaction processing at Mirror/Sink components.
 * 
 * Its possible to configure two parameters:
 * <ul>
 * <li>
 * {@link #setDistributedTransactionWaitTimeout(Float)} - determines the wait timeout for all distributed transaction participants data
 * before committing only the data that arrived.
 * </li>
 * <li>
 * {@link #setDistributedTransactionWaitForOperations(Integer)} - determines the number of operations to wait for before committing
 *  a distributed transaction when data from all participants haven't arrived.  
 * </li>
 * </ul>
 * 
 * @author idan
 * @since 8.0.4
 *
 */
public class DistributedTransactionProcessingConfigurationFactoryBean {

    private Long distributedTransactionWaitTimeout;
    private Long distributedTransactionWaitForOperations;
    
    public DistributedTransactionProcessingConfigurationFactoryBean() {
    }
    
    public Long getDistributedTransactionWaitTimeout() {
        return distributedTransactionWaitTimeout;
    }
    
    public void setDistributedTransactionWaitTimeout(Long distributedTransactionWaitTimeout) {
        this.distributedTransactionWaitTimeout = distributedTransactionWaitTimeout;
    }
    
    public Long getDistributedTransactionWaitForOperations() {
        return distributedTransactionWaitForOperations;
    }
    
    public void setDistributedTransactionWaitForOperations(Long distributedTransactionWaitForOperations) {
        this.distributedTransactionWaitForOperations = distributedTransactionWaitForOperations;
    }

    public void copyParameters(DistributedTransactionProcessingConfiguration transactionProcessingConfiguration) {
        if (distributedTransactionWaitTimeout != null)
            transactionProcessingConfiguration.setTimeoutBeforePartialCommit(distributedTransactionWaitTimeout.longValue());
        if (distributedTransactionWaitForOperations != null)
            transactionProcessingConfiguration.setWaitForOperationsBeforePartialCommit(distributedTransactionWaitForOperations.longValue());
    }
    
}
