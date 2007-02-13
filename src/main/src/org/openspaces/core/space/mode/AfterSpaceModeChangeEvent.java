package org.openspaces.core.space.mode;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.j_spaces.core.IJSpace;

/**
 * @author kimchy
 */
public class AfterSpaceModeChangeEvent extends AbstractSpaceModeChangeEvent {

    public AfterSpaceModeChangeEvent(IJSpace space, SpaceMode spaceMode) {
        super(space, spaceMode);
    }
}
