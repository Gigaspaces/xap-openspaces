package org.openspaces.core.cluster;

/**
 * Holds cluster related information. Beans within the Spring context (or procesing unit context)
 * can use this bean (by implementing {@link ClusterInfoAware}) in order to be informed of their
 * specific cluster instance deployment.
 * 
 * <p>
 * Note, the cluster information is obtained externally from the applicaiton context which means
 * that this feature need to be supported by specific containers (and is not supported by plain
 * Spring application context). This means that beans that implement {@link ClusterInfoAware} should
 * take into account the fact that the cluster info provided might be null.
 * 
 * <p>
 * Naturally, this information can be used by plain Spring application context by constructing this
 * class using Spring and providing it as a parameter to {@link ClusterInfoBeanPostProcessor} which
 * is also configured within Spring application context. Note, if the same application context will
 * later be deployed into a container that provides cluster information, extra caution need to be
 * taken to resolve clashes. The best solution would be to define the cluster info within a differnt
 * Spring xml context, and excluding it when deploying the full context to a cluster info aware
 * container.
 * 
 * <p>
 * The absence (<code>null</code> value) of a certain cluster information property means that it
 * was not set.
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
}
