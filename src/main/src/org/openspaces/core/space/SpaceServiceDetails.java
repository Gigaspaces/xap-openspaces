package org.openspaces.core.space;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.DCacheSpaceImpl;
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

    public static final String ATTRIBUTE_SERVICEID = "service-id";
    public static final String ATTRIBUTE_SPACENAME = "space-name";
    public static final String ATTRIBUTE_SPACECONTAINERNAME = "space-container-name";
    public static final String ATTRIBUTE_SPACETYPE = "space-type";

    private IJSpace space;

    public SpaceServiceDetails() {
    }

    public SpaceServiceDetails(String id, IJSpace space) {
        super(id, "space", null, null, null);
        this.space = space;
        getAttributes().put(ATTRIBUTE_SERVICEID, new ServiceID(space.getReferentUuid().getMostSignificantBits(), space.getReferentUuid().getLeastSignificantBits()));
        SpaceURL spaceURL = space.getFinderURL();
        type = "embedded";
        SpaceType spaceType = SpaceType.EMBEDDED;
        if (space instanceof LocalSpaceView) {
            type = "localview";
            spaceType = SpaceType.LOCAL_VIEW;
        } else if (space instanceof DCacheSpaceImpl) {
            type = "localcache";
            spaceType = SpaceType.LOCAL_CACHE;
        } else if (SpaceUtils.isRemoteProtocol(space)) {
            type = "remote";
            spaceType = SpaceType.REMOTE;
        } else { // embedded
        }
        getAttributes().put(ATTRIBUTE_SPACETYPE, spaceType);
        getAttributes().put(ATTRIBUTE_SPACENAME, spaceURL.getSpaceName());
        getAttributes().put(ATTRIBUTE_SPACECONTAINERNAME, spaceURL.getContainerName());
        description = spaceURL.getSpaceName();
        longDescription = spaceURL.getContainerName() + ":" + spaceURL.getSpaceName();
    }

    public String getName() {
        return (String) getAttributes().get(ATTRIBUTE_SPACENAME);
    }

    public String getContainerName() {
        return (String) getAttributes().get(ATTRIBUTE_SPACECONTAINERNAME);
    }

    public ServiceID getServiceID() {
        return (ServiceID) getAttributes().get(ATTRIBUTE_SERVICEID);
    }

    public SpaceType getSpaceType() {
        return (SpaceType) getAttributes().get(ATTRIBUTE_SPACETYPE);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }
}
