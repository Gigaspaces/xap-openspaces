package org.openspaces.admin.internal.pu;

import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.support.InternalGridComponent;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.ProcessingUnitPartition;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.pu.container.servicegrid.PUServiceBean;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author kimchy
 */
public interface InternalProcessingUnitInstance extends ProcessingUnitInstance, InternalGridComponent {

    ServiceID getServiceID();

    ServiceID getGridServiceContainerServiceID();

    void setProcessingUnit(ProcessingUnit processingUnit);

    void setGridServiceContainer(GridServiceContainer gridServiceContainer);

    void setProcessingUnitPartition(ProcessingUnitPartition processingUnitPartition);

    /**
     * Adds a space instance only if it is one that the processing unit has started.
     */
    void addSpaceInstanceIfMatching(SpaceInstance spaceInstance);

    void removeSpaceInstance(String uid);

    PUServiceBean getPUServiceBean();

    Future<Object> invoke(String serviceBeanName, Map<String, Object> namedArgs);


}
