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
package org.openspaces.grid.gsm.machines.exceptions;

import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnit;

/**
 * @author itaif
 *
 */
public class NeedToWaitUntilAllGridServiceAgentsDiscoveredException extends
GridServiceAgentSlaEnforcementInProgressException {

 private static final long serialVersionUID = 1L;
    
    public NeedToWaitUntilAllGridServiceAgentsDiscoveredException(ProcessingUnit pu, GridServiceContainer container) {
        super(new String[] {pu.getName()}, "Waiting for the Grid Service Agent that started Grid Service Container " + container.getUid() + " to be discovered. It hosts an instance of pu " + pu.getName() + ".");
    }
}
