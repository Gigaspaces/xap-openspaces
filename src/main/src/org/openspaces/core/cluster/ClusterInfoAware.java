package org.openspaces.core.cluster;

/**
 * <p>Allows for beans implementing this interface to be injected with {@link org.openspaces.core.cluster.ClusterInfo}.
 *
 * <p>Note, the cluster information is obtained externally from the applicaiton context which means that this feature
 * need to be supported by specific containers (and is not supported by plain Spring application context). This
 * means that beans that implement {@link org.openspaces.core.cluster.ClusterInfoAware} should take into account
 * the fact that the cluster info provided might be null.
 *
 * @author kimchy
 */
public interface ClusterInfoAware {

    /**
     * <p>Sets the cluster information.
     *
     * <p>Note, the cluster information is obtained externally from the applicaiton context which means that this feature
     * need to be supported by specific containers (and is not supported by plain Spring application context). This
     * means that beans that implement {@link org.openspaces.core.cluster.ClusterInfoAware} should take into account
     * the fact that the cluster info provided might be null.
     *
     * @param clusterInfo
     */
    void setClusterInfo(ClusterInfo clusterInfo);
}
