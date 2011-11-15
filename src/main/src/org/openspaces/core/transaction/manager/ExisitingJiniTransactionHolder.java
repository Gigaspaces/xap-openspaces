package org.openspaces.core.transaction.manager;

import net.jini.core.transaction.Transaction.Created;
import net.jini.lease.LeaseRenewalManager;

/**
 * Represents an holder for an existing Jini transaction, usually when propagating existing to space task. 
 * @author guy
 */
public class ExisitingJiniTransactionHolder extends JiniTransactionHolder {

    public ExisitingJiniTransactionHolder(Created txCreated, int isolationLevel, LeaseRenewalManager leaseRenewalManager) {
        super(txCreated, isolationLevel, leaseRenewalManager);
    }

}
