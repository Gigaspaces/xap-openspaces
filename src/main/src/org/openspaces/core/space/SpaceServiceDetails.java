package org.openspaces.core.space;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.DCacheSpaceImpl;
import com.j_spaces.core.client.ISpaceProxy;
import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.client.view.LocalSpaceView;
import net.jini.core.lookup.ServiceID;
import org.openspaces.core.util.SpaceUtils;
import org.openspaces.pu.service.PlainServiceDetails;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A Space service defined within a processing unit.
 *
 * @author kimchy
 */
public class SpaceServiceDetails extends PlainServiceDetails {

    public static final class Attributes {
        public static final String SERVICEID = "service-id";
        public static final String SPACENAME = "space-name";
        public static final String SPACECONTAINERNAME = "space-container-name";
        public static final String SPACETYPE = "space-type";
        public static final String CLUSTERED = "clustered";
        public static final String URL = "url";
    }

    private IJSpace space;

    public SpaceServiceDetails() {
    }

    public SpaceServiceDetails(IJSpace space) {
        this(null, space);
    }

    public SpaceServiceDetails(String id, IJSpace space) {
        super(id, "space", null, null, null);
        this.space = space;
        getAttributes().put(Attributes.SERVICEID, new ServiceID(space.getReferentUuid().getMostSignificantBits(), space.getReferentUuid().getLeastSignificantBits()));
        SpaceURL spaceURL = space.getFinderURL();
        serviceSubType = "embedded";
        SpaceType spaceType = SpaceType.EMBEDDED;
        if (space instanceof LocalSpaceView) {
            serviceSubType = "localview";
            spaceType = SpaceType.LOCAL_VIEW;
        } else if (space instanceof DCacheSpaceImpl) {
            serviceSubType = "localcache";
            spaceType = SpaceType.LOCAL_CACHE;
        } else if (SpaceUtils.isRemoteProtocol(space)) {
            serviceSubType = "remote";
            spaceType = SpaceType.REMOTE;
        } else { // embedded
        }
        getAttributes().put(Attributes.SPACETYPE, spaceType);
        getAttributes().put(Attributes.SPACENAME, spaceURL.getSpaceName());
        getAttributes().put(Attributes.SPACECONTAINERNAME, spaceURL.getContainerName());
        getAttributes().put(Attributes.CLUSTERED, ((ISpaceProxy) space).isClustered());
        description = spaceURL.getSpaceName();
        longDescription = spaceURL.getContainerName() + ":" + spaceURL.getSpaceName();
        getAttributes().put(Attributes.URL, space.getFinderURL().toString());

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

    public boolean isClustered() {
        return (Boolean) getAttributes().get(Attributes.CLUSTERED);
    }

    public String getUrl() {
        return (String) getAttributes().get(Attributes.URL);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }
}
