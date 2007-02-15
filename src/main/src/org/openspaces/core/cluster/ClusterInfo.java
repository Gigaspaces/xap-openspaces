package org.openspaces.core.cluster;

/**
 * <p>Holds cluster related information. Beans within the Spring context (or procesing unit context) can use this
 * bean (by implementing {@link org.openspaces.core.cluster.ClusterInfoAware} in order to be informed of their
 * specific cluster insance deployment.
 *
 * <p>Note, the cluster information is obtained externally from the applicaiton context which means that this feature
 * need to be supported by specific containers (and is not supported by plain Spring application context). This
 * means that beans that implement {@link org.openspaces.core.cluster.ClusterInfoAware} should take into account
 * the fact that the cluster info provided might be null.
 *
 * <p>The absence (<code>null</code> value) of a certain cluster information property means that it was not set.
 *
 * @author kimchy
 */
public class ClusterInfo {

    private String schema;

    private Integer instanceId;

    private Integer backupId;

    private Integer numberOfInstances;

    private Integer numberOfBackups;

    /**
     * Returns the schema the cluster operates under. Usually maps to a Space cluster schema. Can have
     * <code>null</code> value which means that it was not set.
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
     * Returns the instance id of the specific cluster member. Can have <code>null</code> value which means
     * that it was not set and should not be taken into account.
     */
    public Integer getInstanceId() {
        return instanceId;
    }

    /**
     * Sets the instance id of the specific cluster member. Can have <code>null</code> value which means
     * that it was not set and should not be taken into account.
     */
    public void setInstanceId(Integer instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Returns the backup id of the specific cluster member. Can have <code>null</code> value which means
     * that it was not set and should not be taken into account.
     */
    public Integer getBackupId() {
        return backupId;
    }

    /**
     * Sets the backup id of the specific cluster member. Can have <code>null</code> value which means
     * that it was not set and should not be taken into account.
     */
    public void setBackupId(Integer backupId) {
        this.backupId = backupId;
    }

    /**
     * Returns the number of primary instances that are running within the cluster. Note, this are the number of
     * primary instances. Each instance might also have one or more backups that are set by
     * {@link #setNumberOfBackups(Integer)}. Can have <code>null</code> value which means
     * that it was not set and should not be taken into account.
     */
    public Integer getNumberOfInstances() {
        return numberOfInstances;
    }

    /**
     * Sets the number of primary instances that are running within the cluster. Note, this are the number of
     * primary instances. Each instance might also have one or more backups that are set by
     * {@link #setNumberOfBackups(Integer)}. Can have <code>null</code> value which means
     * that it was not set and should not be taken into account.
     */
    public void setNumberOfInstances(Integer numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }

    /**
     * Returns the number of backups that each primary instance will have in a cluster. Can have <code>null</code>
     * value which means that it was not set and should not be taken into account.
     */
    public Integer getNumberOfBackups() {
        return numberOfBackups;
    }

    /**
     * Sets the number of backups that each primary instance will have in a cluster. Can have <code>null</code>
     * value which means that it was not set and should not be taken into account.
     */
    public void setNumberOfBackups(Integer numberOfBackups) {
        this.numberOfBackups = numberOfBackups;
    }
}
