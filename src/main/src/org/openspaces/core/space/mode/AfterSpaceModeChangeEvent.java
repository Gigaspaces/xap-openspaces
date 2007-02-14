package org.openspaces.core.space.mode;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.j_spaces.core.IJSpace;

/**
 * A Space mode event that is raised after the space mode was changed to the
 * space mode reflected in this event.
 *
 * @author kimchy
 */
public class AfterSpaceModeChangeEvent extends AbstractSpaceModeChangeEvent {

    /**
     * Creates a new after space mode event.
     *
     * @param space     The space that changed its mode
     * @param spaceMode The current space mode
     */
    public AfterSpaceModeChangeEvent(IJSpace space, SpaceMode spaceMode) {
        super(space, spaceMode);
    }
}
