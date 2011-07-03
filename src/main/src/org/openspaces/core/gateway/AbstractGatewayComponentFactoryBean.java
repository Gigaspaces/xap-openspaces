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

import com.gigaspaces.internal.license.LicenseException;
import com.gigaspaces.internal.license.LicenseManager;
import com.gigaspaces.internal.utils.StringUtils;
import com.gigaspaces.lrmi.ProtocolAdapter;

/**
 * Base class for replication gateway components.
 * 
 * @author Idan Moyal
 * @author eitany
 * @since 8.0.3
 *
 */
public abstract class AbstractGatewayComponentFactoryBean implements DisposableBean, InitializingBean, ClusterInfoAware, ProcessingUnitInstanceAddedEventListener {

    protected final Log logger = LogFactory.getLog(getClass());
    
    private String localGatewayName;
    private GatewayLookupsFactoryBean gatewayLookups;
    private boolean startEmbeddedLus = true;
    private boolean relocateIfWrongPorts = true;
    
    private int communicationPort;
    private int discoveryPort;    
    private String puName;
    private boolean relocated;
    private Admin admin;
    
    private static String customJvmProperties;
    private static final Object relocationDecisionLock = new Object();
    private static boolean relocationInProgress = false;

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

    public void setCustomJvmProperties(String jvmProperties) {
        if (customJvmProperties != null)
            throw new IllegalStateException("customJvmProperties has already been set");
        customJvmProperties = jvmProperties;
    }

    public String getCustomJvmProperties() {
        return customJvmProperties;
    }
    
    public void afterPropertiesSet() throws Exception {
        
        LicenseManager licenseManager = new LicenseManager();
        licenseManager.verifyLicense();
        
        if (!licenseManager.isLicensedForWAN()) 
        {
            throw new LicenseException("This license does not permits GigaSpaces WAN module. Please contact support for more details: http://www.gigaspaces.com/supportcenter");
        }
        
        //When puname is null, no relevant cluster info was injected, we are probably
        //inside integrated processing unit container so we cannot move this pu anyway.
        if (puName != null)
        {
            synchronized(relocationDecisionLock)
            {
                //If relocation is in progress, dont initialize this bean since the hosting pu instance is being relocated
                if (relocationInProgress)
                    return;
    
                if (!checkDeployedWithProperPorts())
                {       
                    //If this pu is not deployed in GSC with proper ports, start a gsc with proper ports and relocate it
                    relocationInProgress = true;
                    admin = new AdminFactory().create();
                    admin.getProcessingUnits()
                    .getProcessingUnitInstanceAdded()
                    .add(this);
                    return;
                }            
            }
        }
        
        afterPropertiesSetImpl();
    }
    
    protected abstract void afterPropertiesSetImpl();

    public void destroy() throws Exception {
        destroyImpl();
    }

    protected abstract void destroyImpl();

    private boolean checkDeployedWithProperPorts() {

        initNeededPorts();        

        int currentCommunicationPort = 0;
        final ProtocolAdapter<?> port =
                com.gigaspaces.lrmi.LRMIRuntime.getRuntime().getProtocolRegistry().get("NIO");
        if (port != null) {
            currentCommunicationPort = port.getPort();
        }
        logger.info("current communication port: " + currentCommunicationPort + ", target GSC port: " + communicationPort);

        if (currentCommunicationPort == 0) {
            // LRMI layer not initialized yet - probably means that we are
            // running in the
            // IntegratedProcessingUnitContainer
            // So nothing to do here
            logger.info("Could not find the NIO protocol adapter. "
                    + "This is normal if running in an IntegratedProcessingUnitContainer");
        } else {
            if (communicationPort != 0 && currentCommunicationPort != communicationPort) {
                if (isRelocateIfWrongPorts())
                {
                    logger.info("This GSC is not running on the required communication port. This instance will be relocated to GSC with required communication port.");
                    return false;
                }
                else
                    logger.info("This GSC is not running on the required communication port. Relocate if wrong ports encountered is disabled.");
            }
        }

        if (startEmbeddedLus) {
            final int currentDiscoPort = Integer.parseInt(
                    System.getProperty("com.sun.jini.reggie.initialUnicastDiscoveryPort", "0"));

            logger.info("Discovery port: " + currentDiscoPort + ", target Discovery port: " + discoveryPort);
            if (currentDiscoPort != discoveryPort) {
                if (isRelocateIfWrongPorts())
                {
                    logger.info("This GSC is not running with the required Unicast Discovery port. This instance will be relocated to GSC with required communication port.");
                    return false;
                }
                else
                    logger.info("This GSC is not running with the required Unicast Discovery port. Relocate if wrong ports encountered is disabled.");
            }
        }
        return true;
    }
    
    private void initNeededPorts() {
        StringBuilder foundGateways = null;
        for (GatewayLookup gatewayLookup : getGatewayLookups().getGatewayLookups()) {
            if (gatewayLookup.getGatewayName().equals(getLocalGatewayName())){
                communicationPort = StringUtils.hasLength(gatewayLookup.getCommunicationPort()) ? Integer.valueOf(gatewayLookup.getCommunicationPort()) : 0;
                discoveryPort = Integer.valueOf(gatewayLookup.getDiscoveryPort());
                return;
            }
            if (foundGateways == null) {
                foundGateways = new StringBuilder("found gateways: [");
                foundGateways.append(gatewayLookup.getGatewayName());                
            }
            foundGateways.append(", ");
            foundGateways.append(gatewayLookup.getGatewayName());
        }
        foundGateways.append("]");
        throw new IllegalArgumentException("Could not locate local gateway [" + getLocalGatewayName() + "] in lookup parameters - " + foundGateways.toString());
        
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
        logger.debug("PUI added: " + processingUnitInstance
                .getProcessingUnit().getName());
        if (this.puName.equals(processingUnitInstance
                .getProcessingUnit().getName())) {
            new GSCForkHandler(this.communicationPort, this.discoveryPort, this.startEmbeddedLus, processingUnitInstance, customJvmProperties).movePuToAlternativeGSC();
            admin.getProcessingUnits().getProcessingUnitInstanceAdded().remove(this);

            admin.close();
            admin = null;
            relocated = true;
        }

    }

}
