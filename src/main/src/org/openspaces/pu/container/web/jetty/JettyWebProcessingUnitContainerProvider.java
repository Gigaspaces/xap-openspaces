package org.openspaces.pu.container.web.jetty;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HandlerContainer;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.HandlerWrapper;
import org.mortbay.jetty.webapp.WebAppContext;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoBeanPostProcessor;
import org.openspaces.core.cluster.ClusterInfoPropertyPlaceholderConfigurer;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertyBeanPostProcessor;
import org.openspaces.core.properties.BeanLevelPropertyPlaceholderConfigurer;
import org.openspaces.pu.container.CannotCreateContainerException;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.support.ClusterInfoParser;
import org.openspaces.pu.container.support.ResourceApplicationContext;
import org.openspaces.pu.container.web.WebApplicationContextProcessingUnitContainerProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kimchy
 */
public class JettyWebProcessingUnitContainerProvider implements WebApplicationContextProcessingUnitContainerProvider {

    private static final Log logger = LogFactory.getLog(JettyWebProcessingUnitContainerProvider.class);

    private ApplicationContext parentContext;

    private List<Resource> configResources = new ArrayList<Resource>();

    private BeanLevelProperties beanLevelProperties;

    private ClusterInfo clusterInfo;

    private ClassLoader classLoader;

    private File warPath;

    private File warTempPath;


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

    public void setWarPath(File warPath) {
        this.warPath = warPath;
    }

    public void setWarTempPath(File warTempPath) {
        this.warTempPath = warTempPath;
    }

    /**
     *
     */
    public ProcessingUnitContainer createContainer() throws CannotCreateContainerException {
        if (configResources.size() == 0) {
            try {
                addConfigLocation(DEFAULT_PU_CONTEXT_LOCATION);
                boolean foundValidResource = false;
                for (Resource resource : configResources) {
                    if (resource.exists()) {
                        foundValidResource = true;
                        break;
                    }
                }
                if (!foundValidResource) {
                    Resource resource = new ClassPathResource("/org/openspaces/pu/container/web/jetty/jetty.pu.xml", JettyWebProcessingUnitContainerProvider.class);
                    if (!resource.exists()) {
                        throw new CannotCreateContainerException("Faield to read default pu file [/org/openspaces/pu/container/web/jetty.pu.xml]");
                    }
                    addConfigLocation(resource);
                }
            } catch (IOException e) {
                throw new CannotCreateContainerException("Failed to read config files from " + DEFAULT_PU_CONTEXT_LOCATION, e);
            }
        }
        if (clusterInfo != null) {
            ClusterInfoParser.guessSchema(clusterInfo);
        }

        Resource[] resources = configResources.toArray(new Resource[configResources.size()]);
        // create the Spring application context
        ResourceApplicationContext applicationContext = new ResourceApplicationContext(resources, parentContext);
        // add config information if provided
        if (beanLevelProperties != null) {
            applicationContext.addBeanFactoryPostProcessor(new BeanLevelPropertyPlaceholderConfigurer(beanLevelProperties));
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

        Server server = (Server) applicationContext.getBean("jetty");

        WebAppContext webAppContext = new WebAppContext();

        String contextPath = clusterInfo.getName();
        try {
            contextPath = (String) applicationContext.getBean("contextPath");
        } catch (BeansException e) {
            // not here, ok
        }
        if (contextPath == null) {
            contextPath = "/";
        }
        // TODO for some reason, it does not work with jetty if using anything except for "/" context
        contextPath = "/";
        webAppContext.setContextPath(contextPath);

        webAppContext.setTempDirectory(warTempPath);
        webAppContext.setWar(warPath.getAbsolutePath());
        
        webAppContext.setExtractWAR(true);
        webAppContext.setCopyWebDir(false);

        webAppContext.setAttribute(APPLICATION_CONTEXT_CONTEXT, applicationContext);
        webAppContext.setAttribute(CLUSTER_INFO_CONTEXT, clusterInfo);
        webAppContext.setAttribute(BEAN_LEVEL_PROPERTIES_CONTEXT, beanLevelProperties);

        HandlerContainer container = server;

        Handler[] contexts = server.getChildHandlersByClass(ContextHandlerCollection.class);
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

        return new JettyWebProcessingUnitContainer(applicationContext, webAppContext, container, server);
    }
}