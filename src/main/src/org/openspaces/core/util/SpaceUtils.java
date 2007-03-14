package org.openspaces.core.util;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.SpaceURL;
import org.openspaces.core.GigaSpaceException;

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
     * @throws GigaSpaceException
     */
    public static IJSpace getClusterMemberSpace(IJSpace space, boolean embedded) throws GigaSpaceException {
        try {
            return space.getContainer().getSpace(space.getName(), embedded);
        } catch (Exception e) {
            throw new GigaSpaceException("Failed to find space under name [" + space.getName() + "]", e);
        }
    }

    public static String spaceUrlProperty(String propertyName) {
        return SpaceURL.PROPERTIES_SPACE_URL_ARG + "." + propertyName;
    }

}
