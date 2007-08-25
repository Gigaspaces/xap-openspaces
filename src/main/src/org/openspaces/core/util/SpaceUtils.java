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

package org.openspaces.core.util;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.ISpaceProxy;
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
     * @param space The space to get the cluster member space from.
     * @return A cluster member of the specified space
     * @throws DataAccessException
     */
    public static IJSpace getClusterMemberSpace(IJSpace space) throws DataAccessException {
        try {
            return ((ISpaceProxy) space).getSpace(space.getName());
        } catch (Exception e) {
            throw new DataAccessResourceFailureException("Failed to find space under name [" + space.getName() + "]", e);
        }
    }

    /**
     * Returns <code>true</code> if the Space uses a remote protocol.
     */
    public static boolean isRemoteProtocol(IJSpace space) {
        if (space.getFinderURL() == null) {
            // assume this is an embedded Space
            return false;
        }
        String protocol = space.getFinderURL().getProtocol();
        return protocol.equals(SpaceURL.JINI_PROTOCOL) || protocol.equals(SpaceURL.RMI_PROTOCOL);
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
            return getClusterMemberSpace(space1).equals(getClusterMemberSpace(space2));
        }
        return false;
    }

}
