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
package org.openspaces.grid.gsm.containers.exceptions;

import java.io.IOException;

import org.openspaces.admin.internal.gsc.events.DefaultElasticGridServiceContainerProvisioningFailureEvent;
import org.openspaces.admin.internal.pu.elastic.events.InternalElasticProcessingUnitFailureEvent;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.containers.ContainersSlaUtils;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;


public class FailedToStartNewGridServiceContainersException extends ContainersSlaEnforcementInProgressException implements SlaEnforcementFailure {

    private static final long serialVersionUID = 1L;
    private final String machineUid;
    
    public FailedToStartNewGridServiceContainersException(Machine machine, ProcessingUnit pu, Exception reason) {
        super(pu, createMessage(machine, reason),reason);
        this.machineUid = machine.getUid();
    }

    /**
     * Find root cause. Usually an IOException.
     * SlaEnforcementLogStackTrace means the ESM log will contain full stack trace anyway.
     */
    private static String createMessage(Machine machine, final Exception reason) {
        Throwable rootCause = reason; 
        for (int i = 0 ; i< 10 /*endless loop protection*/;i++) {
            if (rootCause == null || rootCause.getCause() == null || rootCause.getCause() == rootCause || 
                (rootCause instanceof IOException)) {
                break;
            }
            rootCause = rootCause.getCause();
        }
        return "Failed to start container on machine."
                + ContainersSlaUtils.machineToString(machine)+ "."+
                (rootCause != null  ? " Caused By:" + rootCause.getMessage(): "");
    } 
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((machineUid == null) ? 0 : machineUid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        FailedToStartNewGridServiceContainersException other = (FailedToStartNewGridServiceContainersException) obj;
        if (machineUid == null) {
            if (other.machineUid != null)
                return false;
        } else if (!machineUid.equals(other.machineUid))
            return false;
        return true;
    }

    @Override
    public InternalElasticProcessingUnitFailureEvent toEvent() {
        DefaultElasticGridServiceContainerProvisioningFailureEvent event = new DefaultElasticGridServiceContainerProvisioningFailureEvent(); 
        event.setFailureDescription(getMessage());
        event.setProcessingUnitName(getProcessingUnitName());
        return event;
    }
}