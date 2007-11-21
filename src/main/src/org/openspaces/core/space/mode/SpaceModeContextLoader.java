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

package org.openspaces.core.space.mode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.cluster.ClusterInfoBeanPostProcessor;
import org.openspaces.core.cluster.ClusterInfoPropertyPlaceholderConfigurer;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertiesAware;
import org.openspaces.core.properties.BeanLevelPropertyBeanPostProcessor;
import org.openspaces.core.properties.BeanLevelPropertyPlaceholderConfigurer;
import org.openspaces.core.util.SpaceUtils;
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
 * <p>The space mode context loader allows to assemble beans that only operate when a space is in a
 * <code>PRIMARY</code> mode which basically applies when directly working with cluster members
 * and not a clustered space proxy (since in such cases it will always be <code>PRIMARY</code>).
 *
 * <p>The new Spring application context created will have the current context as its parent, allowing
 * to use any beans defined within the current context within the loaded context.
 *
 * <p>The context loader accepts a Spring {@link org.springframework.core.io.Resource} as the location.
 * A flag called {@link #setActiveWhenPrimary(boolean)} which defaults to <code>true</code> allows
 * to control if the context will be loaded only when the cluster member moves to
 * <code>PRIMARY</code> mode.
 *
 * @author kimchy
 */
public class SpaceModeContextLoader implements ApplicationContextAware, InitializingBean, DisposableBean,
        ApplicationListener, BeanLevelPropertiesAware, ClusterInfoAware {

    protected final Log logger = LogFactory.getLog(getClass());

    private Resource location;

    private GigaSpace gigaSpace;

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
     * Allows to set the GigaSpace instance that will control (based on its Space mode - PRIMARY or BACKUP)
     * if the context will be loaded or not. Useful when more than one space is defined
     * within a Spring context.
     */
    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
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
                    if (gigaSpace != null) {
                        if (SpaceUtils.isSameSpace(spEvent.getSpace(), gigaSpace.getSpace())) {
                            try {
                                loadApplicationContext();
                            } catch (Exception e) {
                                logger.error("Failed to load context [" + location + "] when moving to primary mode", e);
                            }
                        }
                    } else {
                        try {
                            loadApplicationContext();
                        } catch (Exception e) {
                            logger.error("Failed to load context [" + location + "] when moving to primary mode", e);
                        }
                    }
                }
            } else if (applicationEvent instanceof BeforeSpaceModeChangeEvent) {
                BeforeSpaceModeChangeEvent spEvent = (BeforeSpaceModeChangeEvent) applicationEvent;
                if (!spEvent.isPrimary()) {
                    if (gigaSpace != null) {
                        if (SpaceUtils.isSameSpace(spEvent.getSpace(), gigaSpace.getSpace())) {
                            closeApplicationContext();
                        }
                    } else {
                        closeApplicationContext();
                    }
                }
            }
        }
    }

    /**
     * Loads the application context and adding specific bean factory and bean post processors.
     * Won't load an application context if one is already defined.
     */
    protected void loadApplicationContext() throws Exception {
        if (applicationContext != null) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Loading application context [" + location + "]");
        }
        applicationContext = new ResourceApplicationContext(new Resource[]{location}, parentApplicationContext);
        // add config information if provided
        if (beanLevelProperties != null) {
            applicationContext.addBeanFactoryPostProcessor(new BeanLevelPropertyPlaceholderConfigurer(beanLevelProperties));
            applicationContext.addBeanPostProcessor(new BeanLevelPropertyBeanPostProcessor(beanLevelProperties));
        }
        if (clusterInfo != null) {
            applicationContext.addBeanPostProcessor(new ClusterInfoBeanPostProcessor(clusterInfo));
        }
        applicationContext.addBeanFactoryPostProcessor(new ClusterInfoPropertyPlaceholderConfigurer(clusterInfo));
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
    protected void closeApplicationContext() {
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
