package org.openspaces.core.space.mode;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.j_spaces.core.IJSpace;
import org.springframework.context.ApplicationEvent;

/**
 * Base class for different space mode events.
 * 
 * @author kimchy
 */
public abstract class AbstractSpaceModeChangeEvent extends ApplicationEvent {

    private SpaceMode spaceMode;

    /**
     * Creates a new Space mode event.
     * 
     * @param space
     *            The space that changed its mode
     * @param spaceMode
     *            The space mode of the space
     */
    public AbstractSpaceModeChangeEvent(IJSpace space, SpaceMode spaceMode) {
        super(space);
        this.spaceMode = spaceMode;
    }

    /**
     * Returns the space that initiated this event.
     */
    public IJSpace getSpace() {
        return (IJSpace) getSource();
    }

    /**
     * The space mode is <code>NONE</code>, in other words - unknown.
     */
    public boolean isNone() {
        return spaceMode == SpaceMode.NONE;
    }

    /**
     * The space mode is <code>BACKUP</code>.
     */
    public boolean isBackup() {
        return spaceMode == SpaceMode.BACKUP;
    }

    /**
     * The space mode is <code>PRIMARY</code>.
     */
    public boolean isPrimary() {
        return spaceMode == SpaceMode.PRIMARY;
    }
}
