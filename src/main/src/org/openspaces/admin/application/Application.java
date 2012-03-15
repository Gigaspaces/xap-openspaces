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
package org.openspaces.admin.application;

import java.util.concurrent.TimeUnit;

import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.pu.topology.ProcessingUnitDeploymentTopology;

/**
 * Describes a group of processing units that interact together as an application.
 * 
 * @author itaif
 * @since 8.0.3
 */
public interface Application {

    /**
     * @return the processing units associated with the application
     * @since 8.0.3
     */
    ProcessingUnits getProcessingUnits();
    
    /**
     * @return the name of the application.
     * @since 8.0.3
     */
    String getName();
    
    /**
     * Deploys the specified processing unit adding it to this application
     */
    ProcessingUnit deployProcessingUnit(ProcessingUnitDeploymentTopology puDeployment);
    
    /**
     * Deploys the specified processing unit adding it to this application
     */
    ProcessingUnit deployProcessingUnit(ProcessingUnitDeploymentTopology puDeployment, long timeout, TimeUnit timeUnit);
    
    /**
     * Undeploys all Processing Units that are part of the application and waits until all instances have been undeployed.
     * In case of an Elastic processing unit, also waits for containers to shutdown.
     * The processing units are undeployed in the reverse dependency order (if feeder depends on space, then space is undeployed and only then space is undeployed) 
     * 
     * <p>The undeployment process will wait indefinitely and return when all processing units have undeployed.
     * 
     * @since 8.0.6
     */
    void undeployAndWait();
    
    /**
     * Undeploys all Processing Units that are part of the application and waits until all instances have been undeployed.
     * In case of an Elastic processing unit, also waits for containers to shutdown.
     * The processing units are undeployed in the reverse dependency order (if feeder depends on space, then space is undeployed and only then space is undeployed)
     * 
     * <p>The undeployment process waits for the specified timeout and return when all processing units of the application has been undeployed or timeout expired.
     * 
*    * @return true if all processing units have undeployed, false if undeployment process is incomplete and the timeout expired.
     * @since 8.0.6
     * 
     */
    boolean undeployAndWait(long timeout, TimeUnit timeUnit);

}
