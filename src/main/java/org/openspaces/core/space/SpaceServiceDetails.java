/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.core.space;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.RemoteException;

import net.jini.core.lookup.ServiceID;

import org.openspaces.core.util.SpaceUtils;
import org.openspaces.pu.service.PlainServiceDetails;

import com.gigaspaces.internal.client.cache.localcache.LocalCacheContainer;
import com.gigaspaces.internal.client.cache.localview.LocalViewContainer;
import com.gigaspaces.internal.client.spaceproxy.ISpaceProxy;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;
import com.j_spaces.core.admin.IRemoteJSpaceAdmin;
import com.j_spaces.core.admin.SpaceConfig;
import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.cluster.ClusterPolicy;
import com.j_spaces.core.cluster.ReplicationPolicy;

/**
 * A Space service defined within a processing unit.
 *
 * @author kimchy
 */
public class SpaceServiceDetails extends PlainServiceDetails {

    private static final long serialVersionUID = -8974518417753189880L;
    
    public static final String SERVICE_TYPE = "space";

    public static final class Attributes {
        public static final String SERVICEID = "service-id";
        public static final String SPACENAME = "space-name";
        public static final String SPACECONTAINERNAME = "space-container-name";
        public static final String SPACETYPE = "space-type";
        public static final String CLUSTERED = "clustered";
        public static final String URL = "url";
        public static final String SPACE_URL = "spaceUrl";
        public static final String MIRROR = "mirror";
        public static final String LOCALGATEWAYNAME = "local-site-name";
    }

    private IJSpace space;

    private IJSpace directSpace;

    private IInternalRemoteJSpaceAdmin directSpaceAdmin;

    public SpaceServiceDetails() {
    }

    public SpaceServiceDetails(IJSpace space) {
        this(null, space);
    }

    public SpaceServiceDetails(String id, IJSpace space) {
        super(id, SERVICE_TYPE, null, null, null);
        this.space = space;
        getAttributes().put(Attributes.SERVICEID, new ServiceID(space.getReferentUuid().getMostSignificantBits(), space.getReferentUuid().getLeastSignificantBits()));
        SpaceURL spaceURL = space.getFinderURL();
        serviceSubType = "embedded";
        SpaceType spaceType = SpaceType.EMBEDDED;
        getAttributes().put(Attributes.MIRROR, false);
        if (space instanceof LocalViewContainer) {
            serviceSubType = "localview";
            spaceType = SpaceType.LOCAL_VIEW;
        } else if (space instanceof LocalCacheContainer) {
            serviceSubType = "localcache";
            spaceType = SpaceType.LOCAL_CACHE;
        } else if (SpaceUtils.isRemoteProtocol(space)) {
            serviceSubType = "remote";
            spaceType = SpaceType.REMOTE;
        } else { // embedded
            try {
                SpaceConfig spaceConfig = ((IRemoteJSpaceAdmin) space.getAdmin()).getConfig();
				if (spaceConfig.isMirrorServiceEnabled()) {
                    getAttributes().put(Attributes.MIRROR, true);
                }
				ClusterPolicy clusterPolicy = spaceConfig.getClusterPolicy();
				ReplicationPolicy replicationPolicy = clusterPolicy != null? clusterPolicy.getReplicationPolicy() : null;
				if (clusterPolicy != null && replicationPolicy != null && replicationPolicy.getGatewaysPolicy() != null)
					getAttributes().put(Attributes.LOCALGATEWAYNAME, replicationPolicy.getGatewaysPolicy().getLocalSiteName());
            } catch (RemoteException e) {
                getAttributes().put(Attributes.MIRROR, false);
            }
            try {
                directSpace = ((ISpaceProxy) space).getClusterMember();
                directSpaceAdmin = (IInternalRemoteJSpaceAdmin) directSpace.getAdmin();
            } catch (Exception e) {
                // no direct space???
            }
        }
        getAttributes().put(Attributes.SPACETYPE, spaceType);
        getAttributes().put(Attributes.SPACENAME, spaceURL.getSpaceName());
        getAttributes().put(Attributes.SPACECONTAINERNAME, spaceURL.getContainerName());
        getAttributes().put(Attributes.CLUSTERED, ((ISpaceProxy) space).isClustered());
        description = spaceURL.getSpaceName();
        longDescription = spaceURL.getContainerName() + ":" + spaceURL.getSpaceName();
        getAttributes().put(Attributes.URL, space.getFinderURL().toString());
        getAttributes().put(Attributes.SPACE_URL, space.getFinderURL());

        if (id == null) {
            this.id = serviceSubType + ":" + spaceURL.getSpaceName();
        }
    }

    public String getName() {
        return (String) getAttributes().get(Attributes.SPACENAME);
    }

    public String getContainerName() {
        return (String) getAttributes().get(Attributes.SPACECONTAINERNAME);
    }

    public ServiceID getServiceID() {
        return (ServiceID) getAttributes().get(Attributes.SERVICEID);
    }

    public SpaceType getSpaceType() {
        return (SpaceType) getAttributes().get(Attributes.SPACETYPE);
    }

    public Boolean isMirror() {
        return (Boolean) getAttributes().get(Attributes.MIRROR);
    }

    public Boolean isClustered() {
        return (Boolean) getAttributes().get(Attributes.CLUSTERED);
    }

    public String getUrl() {
        return (String) getAttributes().get(Attributes.URL);
    }

    public SpaceURL getSpaceUrl() {
        return (SpaceURL) getAttributes().get(Attributes.SPACE_URL);
    }
    
    public String getLocalGatewayName() {
    	return (String) getAttributes().get(Attributes.LOCALGATEWAYNAME);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }
}
