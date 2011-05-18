package org.openspaces.core;

/**
 * This exeception indicates the redo log reached its planned capacity and operations should be blocked until
 * the redolog size is reduced
 * {@link com.gigaspaces.cluster.replication.RedoLogCapacityExceededException}
 * 
 * @author	eitany
 * @since	7.1
 */
public class RedoLogCapacityExceededException extends ResourceCapacityExceededException {

    private static final long serialVersionUID = 234800445050248452L;

    private final com.gigaspaces.cluster.replication.RedoLogCapacityExceededException e;

    public RedoLogCapacityExceededException(com.gigaspaces.cluster.replication.RedoLogCapacityExceededException e) {
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
