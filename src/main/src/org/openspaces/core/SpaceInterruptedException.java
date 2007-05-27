package org.openspaces.core;

import org.springframework.dao.DataAccessException;

/**
 * @author kimchy
 */
public class SpaceInterruptedException extends DataAccessException {

    public SpaceInterruptedException(String msg, InterruptedException e) {
        super(msg, e);
    }
}
