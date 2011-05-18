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

package org.openspaces.core.cluster;

import java.io.Serializable;

/**
 * Holds cluster related information. Beans within the Spring context (or processing unit context)
 * can use this bean (by implementing {@link ClusterInfoAware}) in order to be informed of their
 * specific cluster instance deployment.
 *
 * <p>
 * Note, the cluster information is obtained externally from the application context which means
 * that this feature need to be supported by specific containers (and is not supported by plain
 * Spring application context). This means that beans that implement {@link ClusterInfoAware} should
 * take into account the fact that the cluster info provided might be null.
 *
 * <p>
 * Naturally, this information can be used by plain Spring application context by constructing this
 * class using Spring and providing it as a parameter to {@link ClusterInfoBeanPostProcessor} which
 * is also configured within Spring application context. Note, if the same application context will
 * later be deployed into a container that provides cluster information, extra caution need to be
 * taken to resolve clashes. The best solution would be to define the cluster info within a different
 * Spring xml context, and excluding it when deploying the full context to a cluster info aware
 * container.
 *
 * <p>
 * The absence (<code>null</code> value) of a certain cluster information property means that it
 * was not set.
 *
 * @author kimchy
 */
public class ClusterInfo implements Cloneable, Serializable {

    private static final long serialVersionUID = -128705742407213814L;

    private String schema;

    private Integer instanceId;

    private Integer backupId;

    private Integer numberOfInstances;

    private Integer numberOfBackups;

    private String name;

    /**
     * Constructs a new cluster info with null values on all the fields
     */
    public ClusterInfo() {

    }

    /**
     * Constructs a new Cluster info
     *
     * @param schema            The cluster schema
     * @param instanceId        The instance id
     * @param backupId          The backupId (can be <code>null</code>)
     * @param numberOfInstances Number of instances
     * @param numberOfBackups   Number Of backups (can be <code>null</code>)
     */
    public ClusterInfo(String schema, Integer instanceId, Integer backupId, Integer numberOfInstances, Integer numberOfBackups) {
        this.schema = schema;
        this.instanceId = instanceId;
        this.backupId = backupId;
        this.numberOfInstances = numberOfInstances;
        this.numberOfBackups = numberOfBackups;
    }

    /**
     * Returns the schema the cluster operates under. Usually maps to a Space cluster schema. Can
     * have <code>null</code> value which means that it was not set.
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Sets the schema the cluster operates under. Usually maps to a Space cluster schema. Can have
     * <code>null</code> value which means that it was not set.
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * Returns the instance id of the specific cluster member. Can have <code>null</code> value
     * which means that it was not set and should not be taken into account.
     */
    public Integer getInstanceId() {
        return instanceId;
    }

    /**
     * Sets the instance id of the specific cluster member. Can have <code>null</code> value which
     * means that it was not set and should not be taken into account.
     * @throws IllegalArgumentException if value not greater than zero.
     */
    public void setInstanceId(Integer instanceId) {
        if (instanceId!=null && instanceId.intValue() < 1) {
            throw new IllegalArgumentException("Cluster member instance-id should be greater than zero");
        }
        this.instanceId = instanceId;
    }

    /**
     * Returns the backup id of the specific cluster member. Can have <code>null</code> value
     * which means that it was not set and should not be taken into account.
     */
    public Integer getBackupId() {
        return backupId;
    }

    /**
     * Sets the backup id of the specific cluster member. Can have <code>null</code> value which
     * means that it was not set and should not be taken into account.
     * @throws IllegalArgumentException if value not greater than zero.
     */
    public void setBackupId(Integer backupId) {
        if (backupId!=null && backupId.intValue() < 1) {
            throw new IllegalArgumentException("Cluster member backup-id should be greater than zero");
        }
        this.backupId = backupId;
    }

    /**
     * Returns the number of primary instances that are running within the cluster. Note, this are
     * the number of primary instances. Each instance might also have one or more backups that are
     * set by {@link #setNumberOfBackups(Integer) numberOfBackups}. Can have <code>null</code>
     * value which means that it was not set and should not be taken into account.
     */
    public Integer getNumberOfInstances() {
        return numberOfInstances;
    }

    /**
     * Sets the number of primary instances that are running within the cluster. Note, this are the
     * number of primary instances. Each instance might also have one or more backups that are set
     * by {@link #setNumberOfBackups(Integer) numberOfBackups}. Can have <code>null</code> value
     * which means that it was not set and should not be taken into account.
     */
    public void setNumberOfInstances(Integer numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }

    /**
     * Returns the number of backups that each primary instance will have in a cluster. Can have
     * <code>null</code> value which means that it was not set and should not be taken into
     * account.
     */
    public Integer getNumberOfBackups() {
        return numberOfBackups;
    }

    /**
     * Sets the number of backups that each primary instance will have in a cluster. Can have
     * <code>null</code> value which means that it was not set and should not be taken into
     * account.
     */
    public void setNumberOfBackups(Integer numberOfBackups) {
        this.numberOfBackups = numberOfBackups;
    }

    /**
     * Returns the logical name of the cluster.
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns a "running" number represented by the cluster info.
     * Some examples:
     *
     * 1. NumberOfInstances=2, numberOfBackups=0, instanceId=1: 0.
     * 2. NumberOfInstances=2, numberOfBackups=0, instanceId=2: 1.
     * 3. NumberOfInstances=2, numberOfBackups=1, instanceId=1, backupId=0: 0.
     * 4. NumberOfInstances=2, numberOfBackups=1, instanceId=1, backupId=1: 1.
     * 5. NumberOfInstances=2, numberOfBackups=1, instanceId=2, backupId=0: 2.
     * 6. NumberOfInstances=2, numberOfBackups=1, instanceId=2, backupId=1: 3.
     */
    public int getRunningNumber() {
        //Can have null value which means that it was not set and should not be taken into account.
        if (getNumberOfInstances() == null) {
            return 0;
        }
        if (getNumberOfInstances() == 0) {
            if (getInstanceId() != null) 
                return getInstanceId(); //GS-8737: stateless pu deployed with zero instances but later incremented
            else
                return 0;
        }
        if (getInstanceId() == null || getInstanceId() == 0) {
            return 0;
        }
        if (getNumberOfBackups() == null || getNumberOfBackups() == 0) {
            return getInstanceId() - 1;
        }
        return ((getInstanceId() - 1) * (getNumberOfBackups() + 1)) + (getBackupId() == null ? 0 : getBackupId());
    }

    /**
     * Returns a "running" number: {@link #getRunningNumber()} + 1.
     */
    public int getRunningNumberOffset1() {
        return getRunningNumber() + 1;
    }

    /**
     * Returns a String suffix that can be used to discriminate instances. Uses [instanceId]_[backupId] if there
     * is a backupId. If there is none, uses [instanceId].
     */
    public String getSuffix() {
        StringBuilder retVal = new StringBuilder();
        if (getInstanceId() != null) {
            retVal.append(getInstanceId().toString());
        }
        if (getBackupId() != null) {
            retVal.append("_").append(getBackupId().toString());
        }
        return retVal.toString();
    }

    /**
     * Returns a unique name of the processing unit. Returns [name]_[suffix]
     */
    public String getUniqueName() {
        return getName() + "_" + getSuffix();
    }

    public ClusterInfo copy() {
        ClusterInfo clusterInfo = new ClusterInfo();
        clusterInfo.setBackupId(getBackupId());
        clusterInfo.setInstanceId(getInstanceId());
        clusterInfo.setNumberOfBackups(getNumberOfBackups());
        clusterInfo.setNumberOfInstances(getNumberOfInstances());
        clusterInfo.setSchema(getSchema());
        clusterInfo.setName(getName());
        return clusterInfo;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("name[").append(name).append("] ");
        sb.append("schema[").append(schema).append("] ");
        sb.append("numberOfInstances[").append(numberOfInstances).append("] ");
        sb.append("numberOfBackups[").append(numberOfBackups).append("] ");
        sb.append("instanceId[").append(instanceId).append("] ");
        sb.append("backupId[").append(backupId).append("]");
        return sb.toString();
    }
}
