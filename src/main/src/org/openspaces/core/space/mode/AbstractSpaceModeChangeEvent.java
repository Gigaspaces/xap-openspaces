package org.openspaces.core.space.mode;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.j_spaces.core.IJSpace;
import org.springframework.context.ApplicationEvent;

/**
 * @author kimchy
 */
public abstract class AbstractSpaceModeChangeEvent extends ApplicationEvent {

    private SpaceMode spaceMode;

    public AbstractSpaceModeChangeEvent(IJSpace space, SpaceMode spaceMode) {
        super(space);
        this.spaceMode = spaceMode;
    }

    public IJSpace getSpace() {
        return (IJSpace) getSource();
    }

    public boolean isNone() {
        return spaceMode == SpaceMode.NONE;
    }

    public boolean isBackup() {
        return spaceMode == SpaceMode.BACKUP;
    }

    public boolean isPrimary() {
        return spaceMode == SpaceMode.PRIMARY;
    }
}
