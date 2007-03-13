package org.openspaces.core.cluster;

/**
 * Allows for beans implementing this interface to be injected with {@link ClusterInfo}.
 * 
 * <p>
 * Note, the cluster information is obtained externally from the applicaiton context which means
 * that this feature need to be supported by specific containers (and is not supported by plain
 * Spring application context). This means that beans that implementations of
 * {@link ClusterInfoAware} should take into account the fact that the cluster info provided might
 * be null.
 * 
 * @author kimchy
 */
public interface ClusterInfoAware {

    /**
     * <p>
     * Sets the cluster information.
     * 
     * <p>
     * Note, the cluster information is obtained externally from the applicaiton context which means
     * that this feature need to be supported by specific containers (and is not supported by plain
     * Spring application context). This means that beans that implement {@link ClusterInfoAware}
     * should take into account the fact that the cluster info provided might be null.
     * 
     * @param clusterInfo
     *            The cluster infromation to be injected
     */
    void setClusterInfo(ClusterInfo clusterInfo);
}
