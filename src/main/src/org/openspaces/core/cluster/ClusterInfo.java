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
public class ClusterInfo implements Cloneable {

    private String schema;

    private Integer instanceId;

    private Integer backupId;

    private Integer numberOfInstances;

    private Integer numberOfBackups;

    /**
     * Constructs a new cluser infor with null values on all the fields
     */
    public ClusterInfo() {

    }

    /**
     * Constructs a new Cluster info
     *
     * @param schema            The cluster schema
     * @param instanceId        The instnaceid
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
     */
    public void setInstanceId(Integer instanceId) {
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
     */
    public void setBackupId(Integer backupId) {
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

    public ClusterInfo copy() {
        ClusterInfo clusterInfo = new ClusterInfo();
        clusterInfo.setBackupId(getBackupId());
        clusterInfo.setInstanceId(getInstanceId());
        clusterInfo.setNumberOfBackups(getNumberOfBackups());
        clusterInfo.setNumberOfInstances(getNumberOfInstances());
        clusterInfo.setSchema(getSchema());
        return clusterInfo;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("schema[").append(schema).append("] ");
        sb.append("numberOfInstances[").append(numberOfInstances).append("] ");
        sb.append("numberOfBackups[").append(numberOfBackups).append("] ");
        sb.append("instanceId[").append(instanceId).append("] ");
        sb.append("backupId[").append(backupId).append("]");
        return sb.toString();
    }
}
