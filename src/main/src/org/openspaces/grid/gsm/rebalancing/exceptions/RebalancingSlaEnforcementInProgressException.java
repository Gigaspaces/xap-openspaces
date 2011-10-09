package org.openspaces.grid.gsm.rebalancing.exceptions;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.rebalancing.RebalancingUtils;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementInProgressException;

public class RebalancingSlaEnforcementInProgressException extends SlaEnforcementInProgressException {
 private static final long serialVersionUID = 1L;
    
    public RebalancingSlaEnforcementInProgressException(ProcessingUnit pu) {
        super(inProgressMessage(pu));
    }

    public RebalancingSlaEnforcementInProgressException(ProcessingUnit pu, String message) {
        super(inProgressMessage(pu)+": " + message + 
                "\nInstances " + RebalancingUtils.processingUnitDeploymentToString(pu) + 
                "\nStatus = " + pu.getStatus());
    }
    
    private static String inProgressMessage(ProcessingUnit pu) {
        return pu.getName() + " rebalancing SLA enforcement is in progress";
    }
}
