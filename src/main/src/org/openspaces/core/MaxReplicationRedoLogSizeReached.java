package org.openspaces.core;

import org.springframework.dao.DataAccessException;

/**
 * This exeception indicates the redo log reached its planned capacity and operations should be blocked until
 * the redolog size is reduced
 * {@link com.gigaspaces.cluster.replication.MaxReplicationRedoLogSizeReached}
 * 
 * @author	eitany
 * @since	7.1
 */
public class MaxReplicationRedoLogSizeReached extends DataAccessException {

    private final com.gigaspaces.cluster.replication.MaxReplicationRedoLogSizeReached e;

    public MaxReplicationRedoLogSizeReached(com.gigaspaces.cluster.replication.MaxReplicationRedoLogSizeReached e) {
        super(e.getMessage(), e);
        this.e = e;
    }
    
    /**
     * @return name of the target that the redo log is kept for
     */
    public String getTargetName(){
        return e.getTargetName();
    }
    
    /**
     * @return name of the space that is keeping the redo log
     */
    public String getSpaceName(){
        return e.getSpaceName();
    }


}
