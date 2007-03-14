package org.openspaces.core.space.mode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.cluster.ClusterInfoBeanPostProcessor;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertiesAware;
import org.openspaces.core.properties.BeanLevelPropertyBeanPostProcessor;
import org.openspaces.core.properties.BeanLevelPropertyPlaceholderConfigurer;
import org.openspaces.pu.container.support.ResourceApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;

/**
 * A Space mode based Spring context loader allows to load Spring application context if the Space
 * is in <code>PRIMARY</code> mode.
 * 
 * <p>
 * The space mode context loader allows to assemble beans that only operate when a space is in a
 * <code>PRIMARY</code> mode which basically applies when directly working with cluster members
 * and not a clsutered space proxy (since in such cases it will always be <code>PRIMARY</code>).
 * 
 * <p>
 * The context loader accepts a Spring {@link org.springframework.core.io.Resource} as the location.
 * A flag called {@link #setActiveWhenPrimary(boolean)} which defaults to <code>true</code> allows
 * to control if the context will be loaded only when the cluster member moves to
 * <code>PRIMARY</code> mode.
 * 
 * @author kimchy
 */
public class SpaceModeContextLoader implements ApplicationContextAware, InitializingBean, DisposableBean,
        ApplicationListener, BeanLevelPropertiesAware, ClusterInfoAware {

    private static final Log logger = LogFactory.getLog(SpaceModeContextLoader.class);

    private Resource location;

    private boolean activeWhenPrimary = true;

    private ApplicationContext parentApplicationContext;

    private BeanLevelProperties beanLevelProperties;

    private ClusterInfo clusterInfo;

    private ResourceApplicationContext applicationContext;

    /**
     * The location of the Spring xml context application to be loaded.
     */
    public void setLocation(Resource location) {
        this.location = location;
    }

    /**
     * Controls if the Spring context will be loaded when the space cluster member moves to
     * <code>PRIMARY</code> mode. Defaults to <code>true</code>.
     */
    public void setActiveWhenPrimary(boolean activeWhenPrimary) {
        this.activeWhenPrimary = activeWhenPrimary;
    }

    /**
     * Used to pass the {@link BeanLevelProperties} to the newly created application context.
     */
    public void setBeanLevelProperties(BeanLevelProperties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
    }

    /**
     * Used to pass {@link ClusterInfo} to the newly created application context.
     */
    public void setClusterInfo(ClusterInfo clusterInfo) {
        this.clusterInfo = clusterInfo;
    }

    /**
     * Injected by Spring and used as the parent application context for the newly created
     * application context.
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.parentApplicationContext = applicationContext;
    }

    public void afterPropertiesSet() throws Exception {
        if (!activeWhenPrimary) {
            loadApplicationContext();
        }
    }

    public void destroy() throws Exception {
        closeApplicationContext();
    }

    /**
     * If {@link #setActiveWhenPrimary(boolean)} is set to <code>true</code> (the default) will
     * listens for {@link AfterSpaceModeChangeEvent} and load an application context if received.
     */
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        // TODO maybe match on the space itself, and verify that we are not being notified of the
        // wrong space
        if (activeWhenPrimary) {
            if (applicationEvent instanceof AfterSpaceModeChangeEvent) {
                AfterSpaceModeChangeEvent spEvent = (AfterSpaceModeChangeEvent) applicationEvent;
                if (spEvent.isPrimary()) {
                    try {
                        loadApplicationContext();
                    } catch (Exception e) {
                        logger.error("Failed to load context [" + location + "] when moving to primary mode", e);
                    }
                }
            } else if (applicationEvent instanceof BeforeSpaceModeChangeEvent) {
                BeforeSpaceModeChangeEvent spEvent = (BeforeSpaceModeChangeEvent) applicationEvent;
                if (!spEvent.isPrimary()) {
                    closeApplicationContext();
                }
            }
        }
    }

    /**
     * Loads the application context and adding specific bean factory and bean post processors.
     * Won't load an application context if one is already defined.
     */
    private void loadApplicationContext() throws Exception {
        if (applicationContext != null) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Loading application context [" + location + "]");
        }
        applicationContext = new ResourceApplicationContext(new Resource[] { location }, parentApplicationContext);
        // add config information if provided
        if (beanLevelProperties != null) {
            applicationContext.addBeanFactoryPostProcessor(new BeanLevelPropertyPlaceholderConfigurer(
                    beanLevelProperties));
            applicationContext.addBeanPostProcessor(new BeanLevelPropertyBeanPostProcessor(beanLevelProperties));
        }
        if (clusterInfo != null) {
            applicationContext.addBeanPostProcessor(new ClusterInfoBeanPostProcessor(clusterInfo));
        }
        try {
            applicationContext.refresh();
        } catch (Exception e) {
            closeApplicationContext();
            throw e;
        }
    }

    /**
     * Closes the application context. Won't close that application context if one is not defined.
     */
    private void closeApplicationContext() {
        if (applicationContext != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Closing application context [" + location + "]");
            }
            try {
                applicationContext.close();
            } finally {
                applicationContext = null;
            }
        }
    }
}
