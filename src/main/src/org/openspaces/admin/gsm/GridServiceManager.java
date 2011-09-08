/*

 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.admin.gsm;

import java.util.concurrent.TimeUnit;

import org.openspaces.admin.AgentGridComponent;
import org.openspaces.admin.LogProviderGridComponent;
import org.openspaces.admin.dump.DumpProvider;
import org.openspaces.admin.memcached.MemcachedDeployment;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitAlreadyDeployedException;
import org.openspaces.admin.pu.ProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.ElasticStatefulProcessingUnitDeployment;
import org.openspaces.admin.pu.elastic.ElasticStatelessProcessingUnitDeployment;
import org.openspaces.admin.space.ElasticSpaceDeployment;
import org.openspaces.admin.space.SpaceDeployment;

/**
 * A Grid Service Manager is a manager for {@link org.openspaces.admin.pu.ProcessingUnit} deployments
 * (acting either as primary or backups for a certain processing unit deployment). It also knows
 * which {@link org.openspaces.admin.gsc.GridServiceContainer}s are around to be able to create
 * {@link org.openspaces.admin.pu.ProcessingUnitInstance} on them.
 *
 * @author kimchy
 */
public interface GridServiceManager extends AgentGridComponent, LogProviderGridComponent, DumpProvider {

    /**
     * Undeploys the processing unit based on its name.
     * 
     * {@code
     *  // wait for the lookup event of the processing unit by name.
     *  ProcessingUnit pu = admin.getProcessingUnits().waitFor("mypuname");
     *  
     *  // wait for the lookup event of the grid service manager that manages the processing unit.
     *  pu.waitForManaged();
     *  
     *  // undeploy the processing unit.
     *  pu.undeploy();
     * }
     * 
     */
    void undeploy(String processingUnitName);

    /**
     * Deploys a processing unit based on the processing unit deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait indefinitely and return the actual processing unit that can be used.
     * 
     * @throws ProcessingUnitAlreadyDeployedException - processing unit with the same name has already been deployed.
     */
    ProcessingUnit deploy(ProcessingUnitDeployment deployment) throws ProcessingUnitAlreadyDeployedException;

    /**
     * Deploys a processing unit based on the processing unit deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait for the given timeout and return the actual processing unit that can be used.
     * 
     * @throws ProcessingUnitAlreadyDeployedException - processing unit with the same name has already been deployed.
     */
    ProcessingUnit deploy(ProcessingUnitDeployment deployment, long timeout, TimeUnit timeUnit) throws ProcessingUnitAlreadyDeployedException;

    /**
     * Deploys a space based on the space deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait indefinitely and return the actual processing unit that can be used.
     *
     * <p>Note, deploying just a space is simply deploying a built in processing unit that starts
     * just an embedded space.
     */
    ProcessingUnit deploy(SpaceDeployment deployment) throws ProcessingUnitAlreadyDeployedException;

    /**
     * Deploys a space based on the space deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait for the given timeout and return the actual processing unit that can be used.
     *
     * <p>Note, deploying just a space is simply deploying a built in processing unit that starts
     * just an embedded space.
     */
    ProcessingUnit deploy(SpaceDeployment deployment, long timeout, TimeUnit timeUnit) throws ProcessingUnitAlreadyDeployedException;

    /**
     * Deploys a memcached based on the space deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait indefinitely and return the actual processing unit that can be used.
     *
     * <p>Note, deploying just a memcached is simply deploying a built in processing unit that starts / connects
     * to a space (holding the memcached entries) and exposing the memcached protocol.
     */
    ProcessingUnit deploy(MemcachedDeployment deployment) throws ProcessingUnitAlreadyDeployedException;

    /**
     * Deploys a memcached based on the space deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait for the given timeout and return the actual processing unit that can be used.
     *
     * <p>Note, deploying just a memcached is simply deploying a built in processing unit that starts / connects
     * to a space (holding the memcached entries) and exposing the memcached protocol.
     */
    ProcessingUnit deploy(MemcachedDeployment deployment, long timeout, TimeUnit timeUnit) throws ProcessingUnitAlreadyDeployedException;

    /**
     * Deploys an elastic space based on the space deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait indefinitely and return the actual processing unit that can be used.
     *
     * <p>Note, deploying just a space is simply deploying a built in processing unit that starts
     * just an embedded space.
     */
    ProcessingUnit deploy(ElasticSpaceDeployment deployment) throws ProcessingUnitAlreadyDeployedException;
    
    /**
     * Deploys an elastic space based on the space deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait for the given timeout and return the actual processing unit that can be used.
     *
     * <p>Note, deploying just a space is simply deploying a built in processing unit that starts
     * just an embedded space.
     */
    ProcessingUnit deploy(ElasticSpaceDeployment deployment, long timeout, TimeUnit timeUnit) throws ProcessingUnitAlreadyDeployedException;

    /**
     * Deploys an elastic processing unit that has an embedded space based on the processing unit deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait indefinitely and return the actual processing unit that can be used.
     * 
     * @throws ProcessingUnitAlreadyDeployedException - processing unit with the same name has already been deployed.
     */
    ProcessingUnit deploy(ElasticStatefulProcessingUnitDeployment deployment) throws ProcessingUnitAlreadyDeployedException;
    
    /**
     * Deploys an elastic processing unit that has an embedded space based on the processing unit deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait for the given timeout and return the actual processing unit that can be used.
     * 
     * @throws ProcessingUnitAlreadyDeployedException - processing unit with the same name has already been deployed.
     */
    ProcessingUnit deploy(ElasticStatefulProcessingUnitDeployment deployment, long timeout, TimeUnit timeUnit) throws ProcessingUnitAlreadyDeployedException;
    
    /**
     * Deploys an elastic processing unit that does not have an embedded space based on the processing unit deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait indefinitely and return the actual processing unit that can be used.
     * 
     * @throws ProcessingUnitAlreadyDeployedException - processing unit with the same name has already been deployed.
     */
    ProcessingUnit deploy(ElasticStatelessProcessingUnitDeployment deployment) throws ProcessingUnitAlreadyDeployedException;
    
    /**
     * Deploys an elastic processing unit that does not have an embedded space based on the processing unit deployment information on the given grid
     * service manager (it will act as the primary GSM for the deployed processing unit).
     *
     * <p>The deployment process will wait for the given timeout and return the actual processing unit that can be used.
     * 
     * @throws ProcessingUnitAlreadyDeployedException - processing unit with the same name has already been deployed.
     */
    ProcessingUnit deploy(ElasticStatelessProcessingUnitDeployment deployment, long timeout, TimeUnit timeUnit) throws ProcessingUnitAlreadyDeployedException;
    
    boolean isDeployed( String processingUnitName );    
}
