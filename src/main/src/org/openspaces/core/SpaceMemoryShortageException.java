package org.openspaces.core;

import com.j_spaces.core.MemoryShortageException;
import org.springframework.dao.DataAccessException;

/**
 * This Exception indicates that the space server process reached
 * the predefined percentage usage ratio. Wraps
 * {@link com.j_spaces.core.MemoryShortageException MemoryShortageException}.
 *
 * @author kimchy
 */
public class SpaceMemoryShortageException extends DataAccessException {

    private MemoryShortageException e;

    public SpaceMemoryShortageException(MemoryShortageException e) {
        super(e.getMessage(), e);
        this.e = e;
    }

    // TODO Expose getSpaceName
}
