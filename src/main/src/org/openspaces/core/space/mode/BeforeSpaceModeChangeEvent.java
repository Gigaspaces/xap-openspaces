package org.openspaces.core.space.mode;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.j_spaces.core.IJSpace;

/**
 * @author kimchy
 */
public class BeforeSpaceModeChangeEvent extends AbstractSpaceModeChangeEvent {

    public BeforeSpaceModeChangeEvent(IJSpace space, SpaceMode spaceMode) {
        super(space, spaceMode);
    }
}
