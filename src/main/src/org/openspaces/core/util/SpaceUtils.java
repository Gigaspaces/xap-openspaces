package org.openspaces.core.util;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.JSpaceProxy;
import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.cluster.JSpaceClusteredProxy;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * A set of {@link IJSpace} utilites.
 * 
 * @author kimchy
 */
public abstract class SpaceUtils {

    /**
     * Returns a proxy space to the specified space name. In case of clustered proxy to a space,
     * will return an acutal cluster member proxy (i.e. not cluster aware). If the proxy does not
     * point to a clusered space, will return the same space.
     * 
     * @param space
     *            The space to get the cluster member space from.
     * @param embedded
     *            If <code>true</code> and embedded (collocated) proxy is returned. Otherwise, a
     *            regular proxy (which contains a remote reference) is returned.
     * @return A cluster member of the specified space
     * @throws DataAccessException
     */
    public static IJSpace getClusterMemberSpace(IJSpace space, boolean embedded) throws DataAccessException {
        try {
            return space.getContainer().getSpace(space.getName(), embedded);
        } catch (Exception e) {
            throw new DataAccessResourceFailureException("Failed to find space under name [" + space.getName() + "]", e);
        }
    }

    public static boolean isRemoteProtocol(IJSpace space) {
        return !space.isEmbedded();
    }

    public static String spaceUrlProperty(String propertyName) {
        return SpaceURL.PROPERTIES_SPACE_URL_ARG + "." + propertyName;
    }

    public static boolean isSameSpace(IJSpace space1, IJSpace space2) throws DataAccessException {
        if ((space1 instanceof JSpaceProxy) && (space2 instanceof JSpaceProxy)) {
            return space1.equals(space2);
        }
        if ((space1 instanceof JSpaceClusteredProxy) && (space2 instanceof JSpaceClusteredProxy)) {
            return space1.equals(space2);
        }
        if ((space1 instanceof JSpaceClusteredProxy) || (space2 instanceof JSpaceClusteredProxy)) {
            return getClusterMemberSpace(space1, true).equals(getClusterMemberSpace(space2, true));
        }
        return false;
    }

}
