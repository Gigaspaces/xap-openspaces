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
package org.openspaces.grid.gsm.machines.plugins;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.openspaces.admin.pu.ProcessingUnit;

import com.j_spaces.kernel.GSThreadFactory;

public class NonBlockingElasticMachineProvisioningAdapterFactory {

    private ExecutorService service = Executors
    .newCachedThreadPool(new GSThreadFactory(this.getClass().getName(),true));
   
    private ScheduledThreadPoolExecutor scheduledExecutorService = 
        (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(
                1,
                new GSThreadFactory(this.getClass().getName(),true));
    
    public NonBlockingElasticMachineProvisioningAdapter create(ProcessingUnit pu, ElasticMachineProvisioning machineProvisioning) {
        return new NonBlockingElasticMachineProvisioningAdapter(pu, machineProvisioning, service, scheduledExecutorService);
    }
    
    public void destroy() {
        service.shutdownNow();
        scheduledExecutorService.shutdownNow();
    }
}
