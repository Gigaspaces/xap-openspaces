package org.openspaces.core.space.mode;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.j_spaces.core.IJSpace;

/**
 * A Space mode event that is raised before the space mode is changed to the space mode reflected in
 * this event.
 * 
 * @author kimchy
 */
public class BeforeSpaceModeChangeEvent extends AbstractSpaceModeChangeEvent {

    private static final long serialVersionUID = 1517730321537539772L;

    /**
     * Creates a new before space mode event.
     * 
     * @param space
     *            The space that changed its mode
     * @param spaceMode
     *            The current space mode (the one that it will change to)
     */
    public BeforeSpaceModeChangeEvent(IJSpace space, SpaceMode spaceMode) {
        super(space, spaceMode);
    }
}
