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

package org.openspaces.admin.space;

import org.openspaces.admin.pu.ProcessingUnitDeployment;

/**
 * A deployment of a pure {@link org.openspaces.admin.space.Space} processing unit (comes built in
 * under <code>[GS ROOT]/deploy/templates/datagrid</code>).
 *
 * @author kimchy
 * @see org.openspaces.admin.gsm.GridServiceManager#deploy(SpaceDeployment)
 * @see org.openspaces.admin.gsm.GridServiceManagers#deploy(SpaceDeployment)
 */
public class SpaceDeployment {

    private final ProcessingUnitDeployment deployment;

    private final String spaceName;

    /**
     * Constructs a new Space deployment with the space name that will be created (it will also
     * be the processing unit name).
     */
    public SpaceDeployment(String spaceName) {
        this.spaceName = spaceName;
        this.deployment = new ProcessingUnitDeployment("/templates/datagrid");
        this.deployment.name(spaceName);
        this.deployment.setContextProperty("dataGridName", spaceName);
    }

    /**
     * Retruns the Space name of the deployment.
     */
    public String getSpaceName() {
        return spaceName;
    }

    /**
     * A convenient method allowing to easily configure the space deployment to deploy a
     * paritioned topology with <code>numberOfParitions</code> instances each with
     * <code>numberOfBackups</code>.
     *
     * <p>Exactly the same like calling <code>clusterSchema("partitioned-sync2backup")</code>,
     * followed by <code>numberOfInstances(numberOfParitions)</code> and <code>numberOfBackups(numberOfBackups)</code>.
     *
     * @param numberOfParitions The number of partitions
     * @param numberOfBackups   The number of backups
     * @return This space deployment
     */
    public SpaceDeployment partitioned(int numberOfParitions, int numberOfBackups) {
        clusterSchema("partitioned-sync2backup");
        numberOfInstances(numberOfParitions);
        numberOfBackups(numberOfBackups);
        return this;
    }

    /**
     * A convenient method allowing to easily configure the space deployment to deploy a
     * replicated topology with <code>numberOfInstances</code> instances.
     *
     * <p>Exactly the same like calling <code>clusterSchema("replicated")</code>,
     * followed by <code>numberOfInstances(numberOfInstances)</code> and <code>numberOfBackups(0)</code>.
     *
     * @param numberOfInstances The number of instances to form the replicated space
     * @return This space deployment
     */
    public SpaceDeployment replicated(int numberOfInstances) {
        clusterSchema("replicated");
        numberOfInstances(numberOfInstances);
        numberOfBackups(0);
        return this;
    }

    /**
     * Sets the cluster schema of the Space.
     *
     * @see #partitioned(int, int)
     * @see #replicated(int) 
     */
    public SpaceDeployment clusterSchema(String clusterSchema) {
        deployment.clusterSchema(clusterSchema);
        return this;
    }

    /**
     * Sets the number of instances of the space deployment.
     */
    public SpaceDeployment numberOfInstances(int numberOfInstances) {
        deployment.numberOfInstances(numberOfInstances);
        return this;
    }

    /**
     * Sets the number of backups per instance of the space deployment.
     */
    public SpaceDeployment numberOfBackups(int numberOfBackups) {
        deployment.numberOfBackups(numberOfBackups);
        return this;
    }

    /**
     * Sets the maximum number of instances per virtual machine.
     *
     * <p>On partitioned topology with backups topology, controls that a primary and a backup won't run
     * on the same virtual machine if set to <code>1</code>.
     *
     * <p>On a non partitioned with backups topology, controls the maximum number of instances running on
     * the same virtual machine.
     */
    public SpaceDeployment maxInstancesPerVM(int maxInstancesPerVM) {
        deployment.maxInstancesPerVM(maxInstancesPerVM);
        return this;
    }

    /**
     * Sets the maximum number of instances per machine.
     *
     * <p>On partitioned topology with backups topology, controls that a primary and a backup won't run
     * on the same machine if set to <code>1</code>.
     *
     * <p>On a non partitioned with backups topology, controls the maximum number of instances running on
     * the same machine.
     */
    public SpaceDeployment maxInstancesPerMachine(int maxInstancesPerMachine) {
        deployment.maxInstancesPerMachine(maxInstancesPerMachine);
        return this;
    }

    /**
     * Sets a context deploy time property overriding any <code>${...}</code> defined within a processing
     * unit configuration.
     */
    public SpaceDeployment setContextProperty(String key, String value) {
        deployment.setContextProperty(key, value);
        return this;
    }

    /**
     * Transforms the space deplyoment to a processing unit deployment (it is a processing unit after all,
     * that simply starts an embedded space). 
     */
    public ProcessingUnitDeployment toProcessingUnitDeployment() {
        return deployment;
    }
}
