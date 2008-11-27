package org.openspaces.core.space;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.DCacheSpaceImpl;
import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.client.view.LocalSpaceView;
import net.jini.core.lookup.ServiceID;
import org.openspaces.core.util.SpaceUtils;
import org.openspaces.pu.service.ProcessingUnitServiceDetails;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A Space service defined within a processing unit.
 *
 * @author kimchy
 */
public class SpaceProcessingUnitServiceDetails implements ProcessingUnitServiceDetails, Externalizable {

    private static final long serialVersionUID = 1L;

    private String id;
    
    private String name;

    private String containerName;

    private ServiceID serviceID;

    // can be "embedded", "localview", "localcache", "remote"
    private String type;

    private SpaceType spaceType;

    public SpaceProcessingUnitServiceDetails() {
    }

    public SpaceProcessingUnitServiceDetails(String id, IJSpace space) {
        this.id = id;
        this.serviceID = new ServiceID(space.getReferentUuid().getMostSignificantBits(), space.getReferentUuid().getLeastSignificantBits());
        SpaceURL spaceURL = space.getFinderURL();
        type = "embedded";
        spaceType = SpaceType.EMBEDDED;
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
        this.name = spaceURL.getSpaceName();
        this.containerName = spaceURL.getContainerName();
    }

    public SpaceProcessingUnitServiceDetails(String id, String name, String containerName, ServiceID serviceID, String type) {
        this.id = id;
        this.name = name;
        this.containerName = containerName;
        this.serviceID = serviceID;
        this.type = type;
    }

    public String getId() {
        return this.id;
    }

    public String getServiceType() {
        return "space";
    }

    public String getDescription() {
        return name;
    }

    public String getLongDescription() {
        return containerName + ":" + name;
    }

    public String getName() {
        return name;
    }

    public String getContainerName() {
        return containerName;
    }

    public ServiceID getServiceID() {
        return serviceID;
    }

    public String getType() {
        return type;
    }

    public SpaceType getSpaceType() {
        return spaceType;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(id);
        out.writeUTF(name);
        out.writeUTF(containerName);
        out.writeLong(serviceID.getMostSignificantBits());
        out.writeLong(serviceID.getLeastSignificantBits());
        out.writeUTF(type);
        out.writeObject(spaceType);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        id = in.readUTF();
        name = in.readUTF();
        containerName = in.readUTF();
        serviceID = new ServiceID(in.readLong(), in.readLong());
        type = in.readUTF();
        spaceType = (SpaceType) in.readObject();
    }
}
