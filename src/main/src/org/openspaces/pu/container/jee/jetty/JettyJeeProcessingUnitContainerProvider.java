/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.pu.container.jee.jetty;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HandlerContainer;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.HandlerWrapper;
import org.mortbay.jetty.servlet.HashSessionIdManager;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.management.MBeanContainer;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoBeanPostProcessor;
import org.openspaces.core.cluster.ClusterInfoPropertyPlaceholderConfigurer;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertyBeanPostProcessor;
import org.openspaces.core.properties.BeanLevelPropertyPlaceholderConfigurer;
import org.openspaces.jee.sessions.jetty.GigaSessionIdManager;
import org.openspaces.jee.sessions.jetty.GigaSessionManager;
import org.openspaces.pu.container.CannotCreateContainerException;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.jee.JeeProcessingUnitContainerProvider;
import org.openspaces.pu.container.jee.jetty.support.JdkLogger;
import org.openspaces.pu.container.support.BeanLevelPropertiesUtils;
import org.openspaces.pu.container.support.ClusterInfoParser;
import org.openspaces.pu.container.support.ResourceApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.management.MBeanServer;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.BindException;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@link org.openspaces.pu.container.jee.JeeProcessingUnitContainerProvider} that
 * can run web applications (war files) using Jetty.
 *
 * <p>The implementation uses a specific Spring context xml to configure and load Jetty. This spring
 * context xml is different than the optional <code>pu.xml</code> (which is also loaded if exists).
 *
 * <p>The jetty xml configuration is loaded from {@link #DEFAULT_JETTY_PU} location if it exists. If
 * it does not exists, two defaults exists, one is the <code>jetty.plain.pu.xml</code> and the other
 * is <code>jetty.shared.xml</code>. By default, if nothing is passed, the <code>jetty.plain.xml<code>
 * is loaded.
 *
 * <p>The difference between plain and shared mode is only indicated by the buit in jerry spring
 * application context. The plain mode starts a jetty instance per web application instance. The
 * shared mode uses the same jetty instance for all web applications. The default is plain mode
 * as it is usually the simpler and preferred way to use it.
 *
 * <p>The web application started stores within the web servlet context several objects. It stores
 * all the beans defined within the optional <code>pu.xml</code> under their respective bean
 * names (this allows to define a space within the <code>pu.xml</code> and then get it from within
 * the servlet context). Other objects, such as the {@link org.openspaces.core.cluster.ClusterInfo},
 * {@link BeanLevelProperties}, and the acutal {@link ApplicationContext} are also passed to the
 * servlet context.
 *
 * <p>Post processing of the <code>web.xml</code> and <code>jetty-web.xml</code> is perfomed allowing
 * to use <code>${...}</code> notation within them (for example, using system proeprties, deployed
 * properties, or <code>${clusterInfo...}</code>).
 *
 * <p>JMX in jetty can be enabled by passing a deployment property {@link #JETTY_JMX_PROP}. If set
 * to <code>true</code> jetty will be configured with JMX. In plain mode, where there can be more
 * than one instnace of jetty within the same JVM, the domain each instnace will be registed under
 * will be <code>gigaspaces.jetty.${clusterInfo.name}.${clusterInfo.runningNumberOffset1}</code>.
 * In shraed mode, where there is only one instance of jetty in a single JVM, jetty JMX will be registered
 * with a domain called <code>gigaspaces.jetty</code>.
 *
 * @author kimchy
 */
public class JettyJeeProcessingUnitContainerProvider implements JeeProcessingUnitContainerProvider {

    static {
        System.setProperty("org.mortbay.log.class", JdkLogger.class.getName());
    }

    private static final Log logger = LogFactory.getLog(JettyJeeProcessingUnitContainerProvider.class);


    /**
     * The optional location where a jetty spring application context (responsible for configuring
     * jetty) will be loaded (within the processing unit). If does not exists, will load either the
     * plain or shared built in jetty configurations (controlled by {@link #JETTY_INSTANCE_PROP})
     * defaulting to plain.
     */
    public final static String DEFAULT_JETTY_PU = "/META-INF/spring/jetty.pu.xml";

    public final static String INTERNAL_JETTY_PU_PREFIX = "/org/openspaces/pu/container/jee/jetty/jetty.";

    public final static String INSTANCE_PLAIN = "plain";

    public final static String INSTANCE_SHARD = "shared";

    public static final String JETTY_LOCATION_PREFIX_SYSPROP = "com.gs.pu.jee.jetty.pu.locationPrefix";

    public static final String JETTY_INSTANCE_PROP = "jetty.instance";

    /**
     * A deploy property that controls if Jetty will store the session on the Space. Just by specifying the
     * url it will automatically enable it.
     */
    public static final String JETTY_SESSIONS_URL = "jetty.sessions.spaceUrl";

    /**
     * How often the scavanger thread will run in order to check for expired sessions. Set in
     * <b>seconds</b> and defaults to <code>60 * 5</code> seconds (5 minutes).
     */
    public static final String JETTY_SESSIONS_SCAVANGE_PERIOD = "jetty.sessions.scavangePeriod";

    /**
     * How often an actual update of a <b>non dirty</b> session will be perfomed to the Space. Set in
     * <b>seconds</b> and defaults to <code>60</code> seconds.
     */
    public static final String JETTY_SESSIONS_SAVE_PERIOD = "jetty.sessions.savePeriod";

    /**
     * The lease of the {@link org.openspaces.jee.sessions.jetty.SessionData} that is written to the Space. Set
     * in <b>seconds</b> and defaults to FOREVER.
     */
    public static final String JETTY_SESSIONS_LEASE = "jetty.sessions.lease";

    /**
     * Controls, using a deployment proeprty, the timeout value of sessions. Set in <b>minutes</b>.
     */
    public static final String JETTY_SESSIONS_TIMEOUT = "jetty.sessions.timeout";

    /**
     * The deployment property controlling if JMX is enabled or not. Defaults to <code>false</code>
     * (JMX is disabled).
     */
    public static final String JETTY_JMX_PROP = "jetty.jmx";

    private ApplicationContext parentContext;

    private List<Resource> configResources = new ArrayList<Resource>();

    private BeanLevelProperties beanLevelProperties;

    private ClusterInfo clusterInfo;

    private ClassLoader classLoader;

    private File deployPath;


    private static final ThreadLocal<ApplicationContext> currentApplicationContext = new ThreadLocal<ApplicationContext>();

    private static final ThreadLocal<ClusterInfo> currentClusterInfo = new ThreadLocal<ClusterInfo>();

    private static final ThreadLocal<BeanLevelProperties> currentBeanLevelProperties = new ThreadLocal<BeanLevelProperties>();

    /**
     * Allows to get the current application context (loaded from <code>pu.xml</code>) during web application
     * startup. Can be used to access beans defined within it (like a Space) by components loaded (such as
     * session storage). Note, this is only applicable during web application startup. It is cleared right
     * afterwards.
     */
    public static ApplicationContext getCurrentApplicationContext() {
        return currentApplicationContext.get();
    }

    /**
     * Intenrall used to set the applicationn context loaded from the <code>pu.xml</code> on a thread local
     * so components within the web container (such as session storge) will be able to access it during
     * startup time of the web application using {@link #getCurrentApplicationContext()}.
     */
    private static void setCurrentApplicationContext(ApplicationContext applicationContext) {
        currentApplicationContext.set(applicationContext);
    }

    public static ClusterInfo getCurrentClusterInfo() {
        return currentClusterInfo.get();
    }

    private static void setCurrentClusterInfo(ClusterInfo clusterInfo) {
        currentClusterInfo.set(clusterInfo);
    }

    public static BeanLevelProperties getCurrentBeanLevelProperties() {
        return currentBeanLevelProperties.get();
    }

    private static void setCurrentBeanLevelProperties(BeanLevelProperties beanLevelProperties) {
        currentBeanLevelProperties.set(beanLevelProperties);
    }

    /**
     * Sets Spring parent {@link org.springframework.context.ApplicationContext} that will be used
     * when constructing this processing unit application context.
     */
    public void setParentContext(ApplicationContext parentContext) {
        this.parentContext = parentContext;
    }

    /**
     * Sets the {@link org.openspaces.core.properties.BeanLevelProperties} that will be used to
     * configure this processing unit. When constructing the container this provider will
     * automatically add to the application context both
     * {@link org.openspaces.core.properties.BeanLevelPropertyBeanPostProcessor} and
     * {@link org.openspaces.core.properties.BeanLevelPropertyPlaceholderConfigurer} based on this
     * bean level properties.
     */
    public void setBeanLevelProperties(BeanLevelProperties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }

    /**
     * Sets the {@link org.openspaces.core.cluster.ClusterInfo} that will be used to configure this
     * processing unit. When constructing the container this provider will automatically add to the
     * application context the {@link org.openspaces.core.cluster.ClusterInfoBeanPostProcessor} in
     * order to allow injection of cluster info into beans that implement
     * {@link org.openspaces.core.cluster.ClusterInfoAware}.
     */
    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    /**
     * Sets the class loader this processing unit container will load the application context with.
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Adds a config location using Springs {@link org.springframework.core.io.Resource}
     * abstraction. This config location represents a Spring xml context.
     *
     * <p>Note, once a config location is added that default location used when no config location is
     * defined won't be used (the default location is <code>classpath*:/META-INF/spring/pu.xml</code>).
     */
    public void addConfigLocation(Resource resource) {
        this.configResources.add(resource);
    }

    /**
     * Adds a config location based on a String description using Springs
     * {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver}.
     *
     * @see org.springframework.core.io.support.PathMatchingResourcePatternResolver
     */
    public void addConfigLocation(String path) throws IOException {
        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(path);
        for (Resource resource : resources) {
            addConfigLocation(resource);
        }
    }

    /**
     * Sets the deploy path where the exploded war jetty will work with is located.
     */
    public void setDeployPath(File warPath) {
        this.deployPath = warPath;
    }

    /**
     * See the header javadoc.
     */
    public ProcessingUnitContainer createContainer() throws CannotCreateContainerException {
        if (configResources.size() == 0) {
            try {
                addConfigLocation(DEFAULT_PU_CONTEXT_LOCATION);
            } catch (IOException e) {
                throw new CannotCreateContainerException("Failed to read config files from " + DEFAULT_PU_CONTEXT_LOCATION, e);
            }
        }

        Resource jettyPuResource = new ClassPathResource(DEFAULT_JETTY_PU);
        if (!jettyPuResource.exists()) {
            String instanceProp = beanLevelProperties.getContextProperties().getProperty(JETTY_INSTANCE_PROP, INSTANCE_PLAIN);
            String defaultLocation = System.getProperty(JETTY_LOCATION_PREFIX_SYSPROP, INTERNAL_JETTY_PU_PREFIX) + instanceProp + ".pu.xml";
            jettyPuResource = new ClassPathResource(defaultLocation);
            if (!jettyPuResource.exists()) {
                throw new CannotCreateContainerException("Failed to read internal pu file [" + defaultLocation + "] as well as user defined [" + DEFAULT_JETTY_PU + "]");
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Using internal bulit in jetty pu.xml from [" + defaultLocation + "]");
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Using user specific jetty pu.xml from [" + DEFAULT_JETTY_PU + "]");
            }
        }
        addConfigLocation(jettyPuResource);

        if (clusterInfo != null) {
            ClusterInfoParser.guessSchema(clusterInfo);
        }

        Resource[] resources = configResources.toArray(new Resource[configResources.size()]);
        // create the Spring application context
        ResourceApplicationContext applicationContext = new ResourceApplicationContext(resources, parentContext);
        // add config information if provided
        if (beanLevelProperties != null) {
            applicationContext.addBeanFactoryPostProcessor(new BeanLevelPropertyPlaceholderConfigurer(beanLevelProperties, clusterInfo));
            applicationContext.addBeanPostProcessor(new BeanLevelPropertyBeanPostProcessor(beanLevelProperties));
        }
        if (clusterInfo != null) {
            applicationContext.addBeanPostProcessor(new ClusterInfoBeanPostProcessor(clusterInfo));
        }
        applicationContext.addBeanFactoryPostProcessor(new ClusterInfoPropertyPlaceholderConfigurer(clusterInfo));
        if (classLoader != null) {
            applicationContext.setClassLoader(classLoader);
        }
        // "start" the application context
        applicationContext.refresh();

        JettyHolder jettyHolder = (JettyHolder) applicationContext.getBean("jettyHolder");

        int retryPortCount = 20;
        try {
            retryPortCount = (Integer) applicationContext.getBean("retryPortCount");
        } catch (Exception e) {
            // do nothing
        }

        boolean success = false;
        for (int i = 0; i < retryPortCount; i++) {
            try {
                jettyHolder.openConnectors();
                success = true;
                break;
            } catch (BindException e) {
                try {
                    jettyHolder.closeConnectors();
                } catch (Exception e1) {
                    logger.debug(e1);
                    // ignore
                }
                for (Connector connector : jettyHolder.getServer().getConnectors()) {
                    connector.setPort(connector.getPort() + 1);
                    if (connector instanceof AbstractConnector) {
                        ((AbstractConnector) connector).setConfidentialPort(connector.getConfidentialPort() + 1);
                    }
                }
            } catch (Exception e) {
                try {
                    jettyHolder.closeConnectors();
                } catch (Exception e1) {
                    logger.debug(e1);
                    // ignore
                }
                if (e instanceof CannotCreateContainerException)
                    throw (CannotCreateContainerException) e;
                throw new CannotCreateContainerException("Failed to start jetty server", e);
            }
        }
        if (!success) {
            throw new CannotCreateContainerException("Failed to bind jetty to port with retries [" + retryPortCount + "]");
        }

        try {
            jettyHolder.start();
        } catch (Exception e) {
            try {
                jettyHolder.stop();
            } catch (Exception e1) {
                logger.debug(e1);
                // ignore
            }
            if (e instanceof CannotCreateContainerException)
                throw (CannotCreateContainerException) e;
            throw new CannotCreateContainerException("Failed to start jetty server", e);
        }

        String jmxEnabled = beanLevelProperties.getContextProperties().getProperty(JETTY_JMX_PROP, "false");
        if ("true".equals(jmxEnabled)) {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            MBeanContainer mBeanContainer = new MBeanContainer(mBeanServer);
            String domain = "gigaspaces.jetty";
            if (!jettyHolder.isSingleInstance()) {
                domain += "." + clusterInfo.getName() + "." + clusterInfo.getRunningNumberOffset1();
            }
            mBeanContainer.setDomain(domain);
            jettyHolder.getServer().getContainer().addEventListener(mBeanContainer);
            mBeanContainer.start();
        }

        try {
            BeanLevelPropertiesUtils.resolvePlaceholders(beanLevelProperties, new File(deployPath, "WEB-INF/web.xml"));
        } catch (IOException e) {
            throw new CannotCreateContainerException("Failed to resolve properties on WEB-INF/web.xml", e);
        }
        try {
            BeanLevelPropertiesUtils.resolvePlaceholders(beanLevelProperties, new File(deployPath, "WEB-INF/jetty-web.xml"));
        } catch (IOException e) {
            throw new CannotCreateContainerException("Failed to resolve properties on WEB-INF/jetty-web.xml");
        }
        try {
            BeanLevelPropertiesUtils.resolvePlaceholders(beanLevelProperties, new File(deployPath, "WEB-INF/jetty6-web.xml"));
        } catch (IOException e) {
            throw new CannotCreateContainerException("Failed to resolve properties on WEB-INF/jetty6-web.xml");
        }
        try {
            BeanLevelPropertiesUtils.resolvePlaceholders(beanLevelProperties, new File(deployPath, "WEB-INF/web-jetty.xml"));
        } catch (IOException e) {
            throw new CannotCreateContainerException("Failed to resolve properties on WEB-INF/web-jetty.xml");
        }

        try {
            setCurrentApplicationContext(applicationContext);
            setCurrentBeanLevelProperties(beanLevelProperties);
            setCurrentClusterInfo(clusterInfo);

            WebAppContext webAppContext = (WebAppContext) applicationContext.getBean("webAppContext");

            webAppContext.setExtractWAR(true);

            webAppContext.setAttribute(APPLICATION_CONTEXT_CONTEXT, applicationContext);
            webAppContext.setAttribute(CLUSTER_INFO_CONTEXT, clusterInfo);
            webAppContext.setAttribute(BEAN_LEVEL_PROPERTIES_CONTEXT, beanLevelProperties);

            // allow aliases so load balancing will work on static content
            if (!webAppContext.getInitParams().containsKey("org.mortbay.jetty.servlet.Default.aliases")) {
                webAppContext.getInitParams().put("org.mortbay.jetty.servlet.Default.aliases", "true");
            }
            // when using file mapped buffers, jetty does not release the files when closing the web application
            // resulting in not being able to deploy again the application (failure to write the file again)
            if (!webAppContext.getInitParams().containsKey("org.mortbay.jetty.servlet.Default.useFileMappedBuffer")) {
                webAppContext.getInitParams().put("org.mortbay.jetty.servlet.Default.useFileMappedBuffer", "false");
            }

            String[] beanNames = applicationContext.getBeanDefinitionNames();
            for (String beanName : beanNames) {
                webAppContext.setAttribute(beanName, applicationContext.getBean(beanName));
            }

            // automatically enable GigaSpaces Session Manager when passing the relevant property
            String sessionsSpaceUrl = beanLevelProperties.getContextProperties().getProperty(JETTY_SESSIONS_URL);
            if (sessionsSpaceUrl != null) {
                GigaSessionManager gigaSessionManager = new GigaSessionManager();
                gigaSessionManager.setSpaceUrl(sessionsSpaceUrl);

                String scavangePeriod = beanLevelProperties.getContextProperties().getProperty(JETTY_SESSIONS_SCAVANGE_PERIOD);
                if (scavangePeriod != null) {
                    gigaSessionManager.setScavengePeriod(Integer.parseInt(scavangePeriod));
                    if (logger.isDebugEnabled()) {
                        logger.debug("Setting scavenge period to [" + scavangePeriod + "] seconds");
                    }
                }
                String savePeriod = beanLevelProperties.getContextProperties().getProperty(JETTY_SESSIONS_SAVE_PERIOD);
                if (savePeriod != null) {
                    gigaSessionManager.setSavePeriod(Integer.parseInt(savePeriod));
                    if (logger.isDebugEnabled()) {
                        logger.debug("Setting save period to [" + savePeriod + "] seconds");
                    }
                }
                String lease = beanLevelProperties.getContextProperties().getProperty(JETTY_SESSIONS_LEASE);
                if (lease != null) {
                    gigaSessionManager.setLease(Long.parseLong(lease));
                    if (logger.isDebugEnabled()) {
                        logger.debug("Setting lease to [" + lease + "] milliseconds");
                    }
                }
                String sessionTimeout = beanLevelProperties.getContextProperties().getProperty(JETTY_SESSIONS_TIMEOUT);
                if (sessionTimeout != null) {
                    gigaSessionManager.setMaxInactiveInterval( Integer.parseInt(sessionTimeout) * 60 );
                    if (logger.isDebugEnabled()) {
                        logger.debug("Setting session timeout to [" + sessionTimeout + "] seconds");
                    }
                }

                GigaSessionIdManager sessionIdManager = new GigaSessionIdManager(jettyHolder.getServer());
                sessionIdManager.setWorkerName(clusterInfo.getName() + clusterInfo.getRunningNumberOffset1());
                gigaSessionManager.setIdManager(sessionIdManager);

                webAppContext.getSessionHandler().setSessionManager(gigaSessionManager);
            }

            HandlerContainer container = jettyHolder.getServer();

            Handler[] contexts = jettyHolder.getServer().getChildHandlersByClass(ContextHandlerCollection.class);
            if (contexts != null && contexts.length > 0) {
                container = (HandlerContainer) contexts[0];
            } else {
                while (container != null) {
                    if (container instanceof HandlerWrapper) {
                        HandlerWrapper wrapper = (HandlerWrapper) container;
                        Handler handler = wrapper.getHandler();
                        if (handler == null)
                            break;
                        if (handler instanceof HandlerContainer)
                            container = (HandlerContainer) handler;
                        else
                            throw new IllegalStateException("No container");
                    }
                    throw new IllegalStateException("No container");
                }
            }
            container.addHandler(webAppContext);
            if (container.isStarted() || container.isStarting()) {
                try {
                    webAppContext.start();
                } catch (Exception e) {
                    throw new CannotCreateContainerException("Failed to start web app context", e);
                }
            }
            // automatically set the worker name
            if (webAppContext.getSessionHandler().getSessionManager().getIdManager() instanceof HashSessionIdManager) {
                HashSessionIdManager sessionIdManager = (HashSessionIdManager) webAppContext.getSessionHandler().getSessionManager().getIdManager();
                if (sessionIdManager.getWorkerName() == null) {
                    sessionIdManager.setWorkerName(clusterInfo.getName() + clusterInfo.getRunningNumberOffset1());
                    if (logger.isDebugEnabled()) {
                        logger.debug("Automatically setting worker name to [" + sessionIdManager.getWorkerName() + "]");
                    }
                }
            }
            return new JettyProcessingUnitContainer(applicationContext, webAppContext, container, jettyHolder);
        } catch (Exception e) {
            try {
                jettyHolder.stop();
            } catch (Exception e1) {
                logger.debug("Failed to stop jetty after an error occured, ignoring", e);
            }
            throw new CannotCreateContainerException("Failed to start web application", e);
        } finally {
            setCurrentApplicationContext(null);
            setCurrentBeanLevelProperties(null);
            setCurrentClusterInfo(null);
        }
    }
}
