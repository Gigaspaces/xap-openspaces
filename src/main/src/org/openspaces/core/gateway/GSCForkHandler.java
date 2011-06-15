package org.openspaces.core.gateway;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.gsa.GridServiceAgent;
import org.openspaces.admin.gsa.GridServiceContainerOptions;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.pu.ProcessingUnitInstance;
import org.springframework.util.StringUtils;

/**
 * A class responsible for forking a new GSC for the WAN Gateway, and relocating
 * this PU instance (the gateway) to the new container. Forking is performed
 * with the admin api, or by calling the GSC script directly.
 * 
 * @author barakme
 * @author eitany
 * @since 8.0.3
 */
public class GSCForkHandler {
	private static final int ADMIN_GSC_STARTUP_TIMEOUT_SECS = 30;

	protected final Log logger = LogFactory.getLog(AbstractGatewayComponentFactoryBean.class);

	private final ProcessingUnitInstance pui;
	private final int lrmiPort;
	private final int discoveryPort;

	private static final String LRMI_PORT_PROPERTY_TEMPLATE =
			"-Dcom.gs.transport_protocol.lrmi.bind-port=<LRMI_PORT>";

	private static final String DISCOVERY_PORT_PROPERTY_TEMPLATE =
			"-Dcom.sun.jini.reggie.initialUnicastDiscoveryPort=<DISCOVERY_PORT>";

	public static final String AGENT_EXT_JAVA_OPTIONS =
			LRMI_PORT_PROPERTY_TEMPLATE + " "
					+ DISCOVERY_PORT_PROPERTY_TEMPLATE;

    private final boolean startEmbeddedLus;
    private final String customJvmProperties;

	/****
	 * 
	 * Constructor.
	 * @param admin An active Admin instance used to discover the GSA.
	 * @param lrmiPort the target GSC port.
	 * @param discoveryPort the target discovery port.
	 * @param startEmbeddedLus 
	 * @param pui the PU instance object of the current PU.  
	 * @param customJvmProperties 
	 * @param useScript true if GSC should be created with script instead of GSA.
	 */
	public GSCForkHandler(int lrmiPort, int discoveryPort,
			boolean startEmbeddedLus, ProcessingUnitInstance pui, String customJvmProperties) {

		this.lrmiPort = lrmiPort;
        this.startEmbeddedLus = startEmbeddedLus;
		this.pui = pui;
		this.discoveryPort = discoveryPort;
        this.customJvmProperties = customJvmProperties;
	}	

	private String createDiscoveryPortProperty(final int discoveryPort) {
		return DISCOVERY_PORT_PROPERTY_TEMPLATE
				.replace("<DISCOVERY_PORT>", Integer.toString(discoveryPort));

	}

	private String createLrmiPortProperty(final int gscPort) {
		return LRMI_PORT_PROPERTY_TEMPLATE
				.replace("<LRMI_PORT>", Integer.toString(gscPort));

	}

	private String[] createGSCExtraCommandLineArguments() {
	    List<String> arguments = new LinkedList<String>();
	    arguments.add(createLrmiPortProperty(lrmiPort));
	    if (startEmbeddedLus)
	        arguments.add(createDiscoveryPortProperty(discoveryPort));
	    if (StringUtils.hasLength(customJvmProperties))
	        arguments.addAll(Arrays.asList(customJvmProperties.split(" ")));
        return arguments.toArray(new String[arguments.size()]);
	}

	/***********
	 * This is the main entry point of this class. This method creates a new GSC
	 * and moves this PUI to the new GSC.
	 */
	public void movePuToAlternativeGSC() {

		if (!GatewayUtils.checkPortAvailable(lrmiPort)) {
			throw new IllegalArgumentException("The required LRMI port for the new GSC(" + lrmiPort + ") is not available!");
		}

		GridServiceAgent gsa = null;
		logger.info("Looking up GSA on local machine");
		gsa = findLocalGSA();
		if(gsa == null) {
			logger.error("Could not find local GSA. Cannot start alternative GSC");
			throw new IllegalStateException("Could not find local GSA. Cannot start alternative GSC");
		}

		GridServiceContainer gsc = null;

		logger.info("Found local GSA - starting new GSC");
		gsc = createGSCWithGsa(gsa);
		logger.info("Created new GSC: " + gsc.getUid());

		pui.relocateAndWait(gsc);

	}

	private GridServiceContainer createGSCWithGsa(final GridServiceAgent gsa) {
		GridServiceContainer gsc;

		// set up the GSC parameters
		final GridServiceContainerOptions gsco = new GridServiceContainerOptions();
		final String[] props = createGSCExtraCommandLineArguments();

		for (final String prop : props) {
			gsco.vmInputArgument(prop);
		}
		
		logger.info("starting GSC with parameters: " + Arrays.toString(props));

		// start the GSC
		gsc = gsa.startGridServiceAndWait(gsco, ADMIN_GSC_STARTUP_TIMEOUT_SECS, TimeUnit.SECONDS);
		if (gsc == null) {
			// Did not receive the GSC in the response. Double check in the
			// Admin API
			final GridServiceContainer[] containers = gsa.getMachine().getGridServiceContainers().getContainers();
			for (final GridServiceContainer gridServiceContainer : containers) {
				final String port =
						gridServiceContainer.getVirtualMachine()
								.getDetails()
								.getSystemProperties()
								.get("com.gs.transport_protocol.lrmi.bind-port");

				if ((port != null) && port.equals(Integer.toString(lrmiPort))) {
					gsc = gridServiceContainer;
				}

			}
		}

		if (gsc == null) {
			throw new IllegalStateException("Failed to create new GSC for gateway");
		}
		return gsc;
	}

	private GridServiceAgent findLocalGSA() {

		final GridServiceContainer gsc = this.pui.getGridServiceContainer();
		if (gsc == null) {
			logger.error("Error - could not find the GSC that this PUI belongs to");
			return null;
		}

		final GridServiceAgent gsa = gsc.getGridServiceAgent();
		return gsa;

	}
	
}
