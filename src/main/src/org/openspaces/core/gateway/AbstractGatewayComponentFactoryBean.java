package org.openspaces.core.gateway;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.gigaspaces.lrmi.ProtocolAdapter;

/**
 * Base class for replication gateway components.
 * 
 * @author Idan Moyal
 * @author eitany
 * @since 8.0.3
 *
 */
public abstract class AbstractGatewayComponentFactoryBean implements DisposableBean, InitializingBean, ClusterInfoAware, ProcessingUnitInstanceAddedEventListener{

    protected final Log logger = LogFactory.getLog(getClass());
    
    private String localGatewayName;
    private GatewayLookupsFactoryBean gatewayLookups;
    private boolean startEmbeddedLus = true;
    private boolean relocateIfWrongPorts = true;
    
    private int lrmiPort;
    private int discoveryPort;    
    private String puName;
    private boolean relocated;
    private Admin admin;

    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.puName = clusterInfo.getName();
    }

    /**
     * @return The component's local gateway name used for identifying the component.
     */
    public String getLocalGatewayName() {
        return localGatewayName;
    }
    
    /**
     * Sets the component's local gateway name used for identifying the component. 
     * @param localGatewayName The local gateway name.
     */
    public void setLocalGatewayName(String localGatewayName) {
        this.localGatewayName = localGatewayName;
    }
    
    /**
     * Gets a {@link GatewayLookupsFactoryBean} instance which holds lookup information
     * for the component.
     * @return Lookup information for the component.
     */
    public GatewayLookupsFactoryBean getGatewayLookups() {
        return gatewayLookups;
    }
    
    /**
     * Sets the component's lookup information as a {@link GatewayLookupsFactoryBean} instance. 
     * @param gatewayLookups Component's lookup information.
     */
    public void setGatewayLookups(GatewayLookupsFactoryBean gatewayLookups) {
        this.gatewayLookups = gatewayLookups;
    }
    
    /**
     * Gets whether the component will start an embedded LUS service upon its initialization.
     * @return true if a LUS server will be started, false otherwise. 
     */
    public boolean isStartEmbeddedLus() {
        return startEmbeddedLus;
    }
    
    /**
     * Sets whether an embedded LUS service will be started upon the component's initialization.
     * @param startEmbeddedLus true for starting a LUS service upon initialization, false otherwise.
     */
    public void setStartEmbeddedLus(boolean startEmbeddedLus) {
        this.startEmbeddedLus = startEmbeddedLus;
    }
    
    /**
     * Gets whether this component containing processing unit instance should start and relocate it self to a new GSC if
     * the required ports for this processing unit in the currently deployed GSC are wrong.
     * @return true for self relocating, false otherwise.
     */
    public boolean isRelocateIfWrongPorts() {
        return relocateIfWrongPorts;
    }
    
    /**
     * Sets whether this component containing processing unit instance should start and relocate it self to a new GSC if
     * the required ports for this processing unit in the currently deployed GSC are wrong. 
     * @param relocateIfWrongPorts true for self relocating, false otherwise.
     */
    public void setRelocateIfWrongPorts(boolean relocateIfWrongPorts) {
        this.relocateIfWrongPorts = relocateIfWrongPorts;
    }
    
    
    public void afterPropertiesSet() throws Exception {
        //When puname is null, no relevant cluster info was injected, we are probably
        //inside integrated processing unit container so we cannot move this pu anyway.
        if (puName == null)
            return;
        //If this pu is not deployed in GSC with proper ports, start a gsc with proper ports and relocate it
        if (!checkDeployedWithProperPorts())
        {        
            admin = new AdminFactory().create();
            admin.getProcessingUnits()
            .getProcessingUnitInstanceAdded()
            .add(this);
        }
        else
        {
            afterPropertiesSetImpl();
        }
    }
    
    protected abstract void afterPropertiesSetImpl();

    public void destroy() throws Exception {
        
        destroyImpl();
        
        if (admin != null){
            admin.close();
            admin = null;
        }
    }

    protected abstract void destroyImpl();

    private boolean checkDeployedWithProperPorts() {

        initNeededPorts();        

        int currentLrmiPort = 0;
        final ProtocolAdapter<?> port =
                com.gigaspaces.lrmi.LRMIRuntime.getRuntime().getProtocolRegistry().get("NIO");
        if (port != null) {
            currentLrmiPort = port.getPort();
        }
        logger.info("LRMI port: " + lrmiPort + ", target GSC port: " + lrmiPort);

        if (currentLrmiPort == 0) {
            // LRMI layer not initialized yet - probably means that we are
            // running in the
            // IntegratedProcessingUnitContainer
            // So nothing to do here
            logger.info("Could not find the NIO protocol adapter. "
                    + "This is normal if running in an IntegratedProcessingUnitContainer");
        } else {
            if (currentLrmiPort != lrmiPort) {
                if (isRelocateIfWrongPorts())
                {
                    logger.info("This GSC is not running on the required LRMI port. Creating new GSC!");
                    // Must create new GSC and move this PU there.
                    if (!GatewayUtils.checkPortAvailable(lrmiPort)) {
                        throw new IllegalArgumentException("The required LRMI port for the new GSC(" + lrmiPort
                                + ") is not available!");
                    }
                    return false;
                }
                else
                    logger.info("This GSC is not running on the required LRMI port. Relocate if wrong ports encountered is disabled.");
            }
        }

        final int currentDiscoPort = Integer.parseInt(
                System.getProperty("com.sun.jini.reggie.initialUnicastDiscoveryPort", "0"));

        logger.info("Discovery port: " + currentDiscoPort + ", target Discovery port: " + discoveryPort);
        if (currentDiscoPort != discoveryPort) {
            if (isRelocateIfWrongPorts())
            {
                logger.info("This GSC is not running with the required Unicast Discovery port. Creating new GSC!");
                if (!GatewayUtils.checkPortAvailable(discoveryPort)) {
                    throw new IllegalArgumentException("The required discovery port for the new GSC(" + discoveryPort
                            + ") is not available!");
                }
                return false;
            }
            else
                logger.info("This GSC is not running with the required Unicast Discovery port. Relocate if wrong ports encountered is disabled.");
        }
        return true;
    }
    
    private void initNeededPorts() {
        for (GatewayLookup gatewayLookup : getGatewayLookups().getGatewayLookups()) {
            if (gatewayLookup.getGatewayName().equals(getLocalGatewayName())){
                lrmiPort = gatewayLookup.getLrmiPort();
                discoveryPort = gatewayLookup.getLusPort();
            }
        }
        throw new IllegalArgumentException("Could not locate local gateway [" + getLocalGatewayName() + "] lookup parameters");
        
    }
    
    /**
     * Invoked when a PUI is added. the implementation looks for a PUI with the specified name.
     * @param processingUnitInstance the added PUI.
     */
    public synchronized void processingUnitInstanceAdded(final ProcessingUnitInstance processingUnitInstance) {
        if (relocated) {
            // This edge case seems to happen rarely - need to sort out why
            logger.warn("PUI Added notification still active even though " +
                    "processing unit instance has been moved and admin closed!");
            return;
        }
        logger.info("PUI added to PU: " + processingUnitInstance
                .getProcessingUnit().getName());
        if (this.puName.equals(processingUnitInstance
                .getProcessingUnit().getName())) {
            new GSCForkHandler(this.lrmiPort, this.discoveryPort, processingUnitInstance).movePuToAlternativeGSC();
            admin.getProcessingUnits().getProcessingUnitInstanceAdded().remove(this);

            admin.close();
            admin = null;
            relocated = true;
        }

    }

}
