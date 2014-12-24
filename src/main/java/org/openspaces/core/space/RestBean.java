package org.openspaces.core.space;

import com.gigaspaces.internal.utils.StringUtils;
import com.j_spaces.core.IJSpace;
import com.j_spaces.kernel.Environment;
import net.jini.core.discovery.LookupLocator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.pu.container.CannotCreateContainerException;
import org.openspaces.pu.container.jee.JeeServiceDetails;
import org.openspaces.pu.container.jee.JeeType;
import org.openspaces.pu.container.jee.stats.RequestStatisticsFilter;
import org.openspaces.pu.service.ServiceDetails;
import org.openspaces.pu.service.ServiceDetailsProvider;
import org.openspaces.pu.service.ServiceMonitors;
import org.openspaces.pu.service.ServiceMonitorsProvider;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.servlet.DispatcherType;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.EnumSet;

/**
 * Created by yohana on 12/9/14.
 */
public class RestBean implements InitializingBean, ClusterInfoAware, DisposableBean, ServiceDetailsProvider, ServiceMonitorsProvider {
    protected Log logger = LogFactory.getLog(getClass());

    private Server server;

    private GigaSpace gigaspace;

    private String spaceName;

    private String groups;

    private String locators;

    private String port;

    int jettyPort;

    private FilterHolder filterHolder;

    private ClusterInfo clusterInfo;

    private WebAppContext webAppContext;

    private boolean jettyStarted = false;

    @Override
    public void destroy() {
        if (jettyStarted) {
            logger.info("Stopping rest service");
            try {
                webAppContext.stop();
            } catch (Exception e) {
                logger.error("Unable to stop web context", e);
            }
            try {
                server.stop();
            } catch (Exception e) {
                logger.error("Unable to stop rest service", e);
            }

            server.destroy();
        }
    }

    public void setPort(String port) {
        this.port = port;
    }

    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaspace = gigaSpace;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public void setLocators(String locators) {
        this.locators = locators;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        int runningNumber = clusterInfo.getRunningNumber();
        try {
            jettyPort = Integer.valueOf(this.port);
        } catch (NumberFormatException e) {
            throw new CannotCreateContainerException("Port should be number");
        }
        jettyPort += runningNumber;
        server = new Server();
        server.setStopAtShutdown(true);
        server.setGracefulShutdown(1000);

        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(jettyPort);
        server.setConnectors(new Connector[]{connector});

        String ispaceName, igroups, ilocators;

        if (gigaspace == null && spaceName == null) {
            throw new CannotCreateContainerException("Either giga-space or space-name attribute should be specified.");
        }
        if (gigaspace != null && spaceName != null) {
            throw new CannotCreateContainerException("Either giga-space or space-name attribute can be specified but not both.");
        }

        if (spaceName != null) {
            ispaceName = spaceName;
            //TODO validate groups and locators ?
            igroups = (groups == null ? null : groups);
            ilocators = (locators == null ? null : locators);
        } else {
            IJSpace space = gigaspace.getSpace();
            ispaceName = space.getName();
            String[] lookupgroups = space.getFinderURL().getLookupGroups();
            if (lookupgroups == null || lookupgroups.length == 0) {
                igroups = null;
            } else {
                igroups = StringUtils.join(lookupgroups, ",", 0, lookupgroups.length);
            }

            LookupLocator[] lookuplocators = space.getFinderURL().getLookupLocators();
            if (lookuplocators == null || lookuplocators.length == 0) {
                ilocators = null;
            } else {
                ilocators = "";
                for (int i=0 ; i<lookuplocators.length;i++) {
                    ilocators+= lookuplocators[i].getHost()+":"+lookuplocators[i].getPort();
                    if (i != (lookuplocators.length - 1)) {
                        ilocators += ",";
                    }
                }
            }
        }

        logger.info("Starting REST service on port [" + jettyPort + "]");
        webAppContext = new WebAppContext();
        filterHolder = new FilterHolder(RequestStatisticsFilter.class);
        webAppContext.addFilter(filterHolder, "/", EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST));
        webAppContext.setContextPath("/");
        webAppContext.setWar(Environment.getHomeDirectory() + "/lib/platform/rest/RESTData.war");
        webAppContext.setInitParameter("port", port);
        webAppContext.setInitParameter("spaceName", ispaceName);
        if (igroups != null && !igroups.equalsIgnoreCase("null")) {
            logger.debug("Applying groups "+igroups);
            webAppContext.setInitParameter("lookupGroups", igroups);
        }

        if (ilocators != null && !ilocators.equalsIgnoreCase("null")) {
            logger.debug("Applying locators "+ilocators);
            webAppContext.setInitParameter("lookupLocators", ilocators);
        }
        server.setHandler(webAppContext);
        try {
            server.start();
            jettyStarted = true;
            logger.info("REST service is started");
        } catch (Exception e) {
            logger.error("Unable to start rest service on port [" + jettyPort + "]", e);
            throw new CannotCreateContainerException("Unable to start rest server on port [" + jettyPort + "]", e);
        }
    }

    @Override
    public ServiceDetails[] getServicesDetails() {
        String restIP = System.getenv("NIC_ADDR");
        if (restIP == null) {
            try {
                restIP = Inet4Address.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                logger.error("Unable to get host address for rest service", e);
            }
        }
        //TODO check if restIP is null

        JeeServiceDetails details;
        details = new JeeServiceDetails(restIP, jettyPort, 0, "/", false, "jetty", JeeType.CUSTOM);
        return new ServiceDetails[]{details};
    }

    @Override
    public ServiceMonitors[] getServicesMonitors() {
        if (jettyStarted && filterHolder != null) {
            RequestStatisticsFilter filter = ((RequestStatisticsFilter) filterHolder.getFilter());
            if (filter == null) {
                logger.debug("Unable to find a running Filter");
                return new ServiceMonitors[0];
            } else {
                return filter.getServicesMonitors();
            }
        } else {
            return new ServiceMonitors[0];
        }
    }

    @Override
    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

}
