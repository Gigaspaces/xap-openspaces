package org.openspaces.grid.gsm.rebalancing;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementPolicy;

import edu.emory.mathcs.backport.java.util.Arrays;

public class RebalancingSlaPolicy extends ServiceLevelAgreementPolicy {

    private GridServiceContainer[] containers;
    private int maxNumberOfConcurrentRelocationsPerMachine;
    private ProcessingUnitSchemaConfig schema;
    
    public void setContainers(GridServiceContainer[] containers) {
        this.containers = containers;
    }
    
    public GridServiceContainer[] getContainers() {
        return containers;
    }
    
    public void setMaximumNumberOfConcurrentRelocationsPerMachine(int maxNumberOfConcurrentRelocationsPerMachine) {
        this.maxNumberOfConcurrentRelocationsPerMachine = maxNumberOfConcurrentRelocationsPerMachine;
    }
    
    public int getMaximumNumberOfConcurrentRelocationsPerMachine() {
        return maxNumberOfConcurrentRelocationsPerMachine;
    }

    public ProcessingUnitSchemaConfig getSchemaConfig() {
        return schema;
    }
    
    public void setSchemaConfig(ProcessingUnitSchemaConfig schemaConfig) {
        this.schema = schemaConfig;
    }
    
    public boolean equals(Object other) {
        return other instanceof RebalancingSlaPolicy &&
        ((RebalancingSlaPolicy)other).maxNumberOfConcurrentRelocationsPerMachine == maxNumberOfConcurrentRelocationsPerMachine &&
        ((RebalancingSlaPolicy)other).schema == schema &&
        Arrays.asList(((RebalancingSlaPolicy)other).containers).equals(Arrays.asList(containers));
    }
   
}
