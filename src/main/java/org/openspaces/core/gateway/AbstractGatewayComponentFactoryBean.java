/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.core.gateway;

import java.io.PrintWriter;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventListener;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.properties.BeanLevelMergedPropertiesAware;
import org.openspaces.core.space.SecurityConfig;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import com.gigaspaces.internal.dump.InternalDump;
import com.gigaspaces.internal.dump.InternalDumpProcessor;
import com.gigaspaces.internal.dump.InternalDumpProcessorFailedException;
import com.gigaspaces.internal.license.LicenseManager;
import com.gigaspaces.internal.utils.StringUtils;
import com.gigaspaces.license.LicenseException;
import com.gigaspaces.lrmi.ProtocolAdapter;
import com.gigaspaces.security.directory.CredentialsProvider;
import com.gigaspaces.security.directory.DefaultCredentialsProvider;
import com.gigaspaces.security.directory.UserDetails;

/**
 * Base class for replication gateway components.
 * 
 * @author idan
 * @author eitany
 * @since 8.0.3
 *
 */
public abstract class AbstractGatewayComponentFactoryBean implements DisposableBean, InitializingBean,
        ClusterInfoAware, ProcessingUnitInstanceAddedEventListener, BeanLevelMergedPropertiesAware,
        InternalDumpProcessor, BeanNameAware {

    protected final Log logger = LogFactory.getLog(getClass());
    
    private String localGatewayName;
    private GatewayLookupsFactoryBean gatewayLookups;
    private Boolean startEmbeddedLus = null;
    private boolean relocateIfWrongPorts = true;
    
    private int communicationPort;
    private int discoveryPort;    
    private String puName;
    private boolean relocatingInvoked;
    private Admin admin;
    private boolean communicationPortIsSet;
    
    private SecurityConfig securityConfig;
    private Properties beanLevelProperties;

    private String beanName;
    
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
        //If this is not explicitly set, the default will be according to whether this sink hate gateway lookups
        //which is marking whether this sink is used on local network or on separated networks (e.g WAN).
        if (startEmbeddedLus == null)
            return gatewayLookups != null;
        return startEmbeddedLus.booleanValue();
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
    
    /**
     * @return The gateway component's communication port. 
     */
    public int getCommunicationPort() {
        return communicationPort;
    }
    
    public int getDiscoveryPort()
    {
        return discoveryPort;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }
    
    @Override
    public String getName() {
        return beanName;
    }
    
    /**
     * Sets the gateway component's communication port.
     * @param communicationPort The communication port.
     */
    public void setCommunicationPort(int communicationPort) {
        this.communicationPort = communicationPort;
        communicationPortIsSet = true;
    }
    
    /**
     * Sets the security configuration which holds login information for this component.
     * @param securityConfig The security configuration to associate with this component.
     */
    public void setSecurityConfig(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    /**
     * Initializes security configuration for this component with the provided {@link UserDetails} instance.
     * For more information see {@link #setSecurityConfig(SecurityConfig)}.
     * @param userDetails {@link UserDetails} instance to initialize security config with.
     */
    public void setUserDetails(UserDetails userDetails) {
        setCredentialsProvider(new DefaultCredentialsProvider(userDetails));
    }

    /**
     * Initializes security configuration for this component with the provided {@link UserDetails} instance.
     * For more information see {@link #setSecurityConfig(SecurityConfig)}.
     * @param credentialsProvider {@link CredentialsProvider} instance to initialize security config with.
     */
    public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.securityConfig = new SecurityConfig(credentialsProvider);
    }

    @Override
    public void setMergedBeanLevelProperties(Properties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }
    
    public void afterPropertiesSet() throws Exception {
        
        LicenseManager licenseManager = new LicenseManager();
        licenseManager.verifyLicense();
        
        if (!licenseManager.isLicensedForWAN()) 
        {
            throw new LicenseException("This license does not permits GigaSpaces WAN module. Please contact support for more details: http://www.gigaspaces.com/supportcenter");
        }
        
        // Security details provided on deployment
        if (beanLevelProperties != null) {
            SecurityConfig securityConfig = SecurityConfig.fromMarshalledProperties(beanLevelProperties);
            if (securityConfig != null)
                setSecurityConfig(securityConfig);

            // Clear since contains security credentials
            beanLevelProperties = null;
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
                    final AdminFactory adminFactory = new AdminFactory();
                    if (securityConfig != null) {
                        adminFactory.credentialsProvider(securityConfig.getCredentialsProvider());
                    }
                    admin = adminFactory.create();
                    admin.getProcessingUnits()
                    .getProcessingUnitInstanceAdded()
                    .add(this);
                    return;
                }            
            }
        }
        
        final SecurityConfig transientSecurityConfig = securityConfig;
        securityConfig = null;
        afterPropertiesSetImpl(transientSecurityConfig);
    }
    
    protected abstract void afterPropertiesSetImpl(SecurityConfig securityConfig);

    public void destroy() throws Exception {
        destroyImpl();
        if (admin != null) {
            admin.close();
        }
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

        if (isStartEmbeddedLus()) {
            final int currentDiscoPort = Integer.parseInt(
                    System.getProperty("com.sun.jini.reggie.initialUnicastDiscoveryPort", "0"));

            logger.info("Discovery port: " + currentDiscoPort + ", target Discovery port: " + discoveryPort);
            if (discoveryPort != 0 && currentDiscoPort != discoveryPort) {
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
        //Indicates this is a local gateway sink, no lookups are specified
        if (getGatewayLookups() == null)
            return;
        for (GatewayLookup gatewayLookup : getGatewayLookups().getGatewayLookups()) {
            if (gatewayLookup.getGatewayName().equals(getLocalGatewayName())) {
                if (!communicationPortIsSet)
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
        if (!relocatingInvoked) {
            if (this.puName.equals(processingUnitInstance
                    .getProcessingUnit().getName())) {
                relocatingInvoked = true;
                final GSCForkHandler gscForkHandler = new GSCForkHandler(this.communicationPort, this.discoveryPort, this.isStartEmbeddedLus(), processingUnitInstance, customJvmProperties);
                //perform blocking operation on a non-event listener thread
                ((InternalAdmin)admin).scheduleAdminOperation(new Runnable() {
                    
                    @Override
                    public void run() {
                        gscForkHandler.movePuToAlternativeGSC();
                        admin.close(); 
                    }
                });
            }
        }
    }
    
    @Override
    public void process(InternalDump dump) throws InternalDumpProcessorFailedException {
        dump.addPrefix("gateway/");
        try {
            PrintWriter writer = new PrintWriter(dump.createFileWriter(beanName + ".txt"));
            writer.println("=============================================");
            writer.println(" Gateway component type: " + getGatewayComponentTypeName());
            writer.println("=============================================");
            writer.println(dumpState());
            writer.close();
        } finally {
            dump.removePrefix();
        }
    }
    
    /**
     * @return The gateway component's type name.
     */
    protected abstract String getGatewayComponentTypeName();
    
    protected abstract String dumpState();
    
}
