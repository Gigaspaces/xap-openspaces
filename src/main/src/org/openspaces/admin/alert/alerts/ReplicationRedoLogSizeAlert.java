package org.openspaces.admin.alert.alerts;


import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.config.ReplicationRedoLogSizeAlertConfigurer;
import org.openspaces.admin.alert.events.AlertTriggeredEventListener;
import org.openspaces.admin.space.ReplicationStatus;
import org.openspaces.admin.space.SpaceInstance;

/**
 * A replication redo-log size alert, fired upon redo-log increase, indicating that packets are not
 * being sent from a source Space to it's target Space (backup or mirror). The alert is raised when
 * red-log size crosses a specified 'high' threshold. The alert is resolved when the redo-log size
 * goes below a specified 'low' threshold.
 * <p>
 * These thresholds can be configured by using the {@link ReplicationRedoLogSizeAlertConfigurer}.
 * <p>
 * This alert will be received on the call to {@link AlertTriggeredEventListener#alertTriggered(Alert)} for
 * registered listeners.
 * 
 * @author Moran Avigdor
 * @since 8.0
 */
public class ReplicationRedoLogSizeAlert extends AbstractAlert {

    private static final long serialVersionUID = 1L;
    
    public static final String HOST_ADDRESS = "host-address";
    public static final String HOST_NAME = "host-name";
    public static final String VIRTUAL_MACHINE_UID = "vm-uid";
    public static final String CPU_UTILIZATION = "cpu-utilization";
    public static final String HEAP_UTILIZATION = "heap-utilization";
    
    public static final String REPLICATION_STATUS = "replication-status";
    public static final String REDO_LOG_SIZE = "redo-log-size";
    public static final String REDO_LOG_MEMORY_SIZE = "redo-log-memory-size";
    public static final String REDO_LOG_SWAP_SIZE = "redo-log-swap-size";

    /** required by java.io.Externalizable */
    public ReplicationRedoLogSizeAlert() {
    }
    
    public ReplicationRedoLogSizeAlert(Alert alert) {
        super(alert);
    }
    
    /**
     * {@inheritDoc}
     * The component UID is equivalent to {@link SpaceInstance#getUid()}
     */
    @Override
    public String getComponentUid() {
        return super.getComponentUid();
    }
    
    /**
     * The host address of the machine that this alert corresponds to.
     * @return the host address; may be <code>null</code>.
     */
    public String getHostAddress() {
        return getProperties().get(HOST_ADDRESS);
    }
    
    /**
     * The host name of the machine that this alert corresponds to.
     * @return the host name; may be <code>null</code>.
     */
    public String getHostName() {
        return getProperties().get(HOST_NAME);
    }

    /**
     * The uid of the virtual machine that this alert corresponds to.
     * @return the virtual machine uid; may be <code>null</code>.
     */
    public String getVirtualMachineUid() {
        return getProperties().get(VIRTUAL_MACHINE_UID);
    }    
    
    /**
     * The CPU utilization reading when this alert was fired.
     * @return the CPU utilization; may be <code>null</code>.
     */
    public Double getCpuUtilization() {
        String value = getProperties().get(CPU_UTILIZATION);
        if (value == null) return null;
        return Double.valueOf(value);
    }
    
    /**
     * The heap memory utilization reading when this alert was fired.
     * @return the heap utilization; may be <code>null</code>.
     */
    public Double getHeapUtilization() {
        String value = getProperties().get(HEAP_UTILIZATION);
        if (value == null) return null;
        return Double.valueOf(value);
    }
    
    /**
     * The {@link ReplicationStatus replication status} from source to target Space.
     * @return the replication status; may be <code>null</code>.
     */
    public String getReplicationStatus() {
        return getProperties().get(REPLICATION_STATUS);
    }
    
    /**
     * The total redo-log size (for all channels) for both memory and swap.
     * @return the redo-log size; may be <code>null</code>.
     */
    public Integer getRedoLogSize() {
        String value = getProperties().get(REDO_LOG_SIZE);
        if (value == null) return null;
        return Integer.valueOf(value);
    }
    
    /**
     * The in-memory redo-log size (for all channels).
     * @return the redo-log size; may be <code>null</code>.
     */
    public Integer getRedoLogMemorySize() {
        String value = getProperties().get(REDO_LOG_MEMORY_SIZE);
        if (value == null) return null;
        return Integer.valueOf(value);
    }
    
    /**
     * The swap redo-log size (for all channels).
     * @return the redo-log size; may be <code>null</code>.
     */
    public Integer getRedoLogSwapSize() {
        String value = getProperties().get(REDO_LOG_SWAP_SIZE);
        if (value == null) return null;
        return Integer.valueOf(value);
    }
}
