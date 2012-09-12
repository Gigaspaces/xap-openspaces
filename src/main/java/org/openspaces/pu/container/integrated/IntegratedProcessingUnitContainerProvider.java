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

package org.openspaces.pu.container.integrated;

import java.io.File;
import java.io.IOException;
import java.rmi.MarshalledObject;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoBeanPostProcessor;
import org.openspaces.core.cluster.ClusterInfoPropertyPlaceholderConfigurer;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertyBeanPostProcessor;
import org.openspaces.core.properties.BeanLevelPropertyPlaceholderConfigurer;
import org.openspaces.pu.container.CannotCreateContainerException;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainerProvider;
import org.openspaces.pu.container.support.ClusterInfoParser;
import org.openspaces.pu.container.support.CompoundProcessingUnitContainer;
import org.openspaces.pu.container.support.ResourceApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.gigaspaces.security.directory.User;
import com.gigaspaces.security.directory.UserDetails;
import com.j_spaces.core.Constants;
import com.j_spaces.core.client.SpaceURL;

/**
 * An {@link org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainer} provider. An
 * integrated processing unit container can be used to run a processing unit within an existing
 * environment. An example of what this existing environment will provide is the classpath that the
 * processing unit will run with. Examples for using the integrated processing unit container can be
 * integration tests or running the processing unit from within an IDE.
 *
 * <p>At its core the integrated processing unit container is built around Spring
 * {@link org.springframework.context.ApplicationContext} configured based on a set of config
 * locations.
 *
 * <p>The provider allows for programmatic configuration of different processing unit aspects. It
 * allows to configure where the processing unit Spring context xml descriptors are located (by
 * default it uses <code>classpath*:/META-INF/spring/pu.xml</code>). It also allows to set
 * {@link org.openspaces.core.properties.BeanLevelProperties} and
 * {@link org.openspaces.core.cluster.ClusterInfo} that will be injected to beans configured within
 * the processing unit.
 *
 * <p>For a runnable "main" processing unit container please see
 * {@link org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainer#main(String[])}.
 *
 * @author kimchy
 */
public class IntegratedProcessingUnitContainerProvider implements ApplicationContextProcessingUnitContainerProvider {

    private static final Log logger = LogFactory.getLog(IntegratedProcessingUnitContainerProvider.class);

    private ApplicationContext parentContext;

    private final List<Resource> configResources = new ArrayList<Resource>();

    private BeanLevelProperties beanLevelProperties;

    private ClusterInfo clusterInfo;

    private ClassLoader classLoader;

    private UserDetails userDetails;

    private Boolean secured;

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

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    public void setUserDetails(String username, String password) {
        this.userDetails = new User(username, password);
    }

    public void setSecured(Boolean secured) {
        this.secured = secured;
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
     * Creates a new {@link IntegratedProcessingUnitContainer} based on the configured parameters.
     * 
     * <p>
     * If {@link #addConfigLocation(org.springframework.core.io.Resource)} or
     * {@link #addConfigLocation(String)} were used, the Spring xml context will be read based on
     * the provided locations. If no config location was provided the default config location will
     * be <code>classpath*:/META-INF/spring/pu.xml</code>.
     * 
     * <p>
     * If {@link #setBeanLevelProperties(org.openspaces.core.properties.BeanLevelProperties)} is set
     * will use the configured bean level properties in order to configure the application context
     * and specific beans within it based on properties. This is done by adding
     * {@link org.openspaces.core.properties.BeanLevelPropertyBeanPostProcessor} and
     * {@link org.openspaces.core.properties.BeanLevelPropertyPlaceholderConfigurer} to the
     * application context.
     * 
     * <p>
     * If {@link #setClusterInfo(org.openspaces.core.cluster.ClusterInfo)} is set will use it to
     * inject {@link org.openspaces.core.cluster.ClusterInfo} into beans that implement
     * {@link org.openspaces.core.cluster.ClusterInfoAware}.
     * 
     * @return An {@link IntegratedProcessingUnitContainer} instance or an
     *         {@link CompoundProcessingUnitContainer} in case of a clustered processing unit
     *         without a specific instance Id.
     * @throws CannotCreateContainerException
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
                    addConfigLocation(new FileSystemResource(DEFAULT_FS_PU_CONTEXT_LOCATION));
                    for (Resource resource : configResources) {
                        if (!resource.exists()) {
                            throw new CannotCreateContainerException("No explicit config location, tried [" + DEFAULT_PU_CONTEXT_LOCATION + "], [" + DEFAULT_FS_PU_CONTEXT_LOCATION + "] (relative to working director), [pu.config], and no configuration found");
                        }
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("No explicit config location, default location [" + DEFAULT_PU_CONTEXT_LOCATION + "] has no configuration, loading from [" + DEFAULT_FS_PU_CONTEXT_LOCATION + "] relative to working directory [" + new File(".").getAbsolutePath() + "]");
                    } else {
                        logger.debug("No explicit config location, defaulting to [" + DEFAULT_PU_CONTEXT_LOCATION + "]");
                    }
                }
            } catch (IOException e) {
                throw new CannotCreateContainerException("Failed to read config files from " + DEFAULT_PU_CONTEXT_LOCATION, e);
            }
        }
        if (clusterInfo != null) {
            ClusterInfoParser.guessSchema(clusterInfo);
        }

        // in case we don't have a cluster info specific members
        if (clusterInfo != null && clusterInfo.getInstanceId() == null) {
            ClusterInfo origClusterInfo = clusterInfo;
            List<ProcessingUnitContainer> containers = new ArrayList<ProcessingUnitContainer>();
            for (int i = 0; i < clusterInfo.getNumberOfInstances(); i++) {
                ClusterInfo containerClusterInfo = clusterInfo.copy();
                containerClusterInfo.setInstanceId(i + 1);
                containerClusterInfo.setBackupId(null);
                setClusterInfo(containerClusterInfo);
                containers.add(createContainer());
                if (clusterInfo.getNumberOfBackups() != null) {
                    for (int j = 0; j < clusterInfo.getNumberOfBackups(); j++) {
                        containerClusterInfo = containerClusterInfo.copy();
                        containerClusterInfo.setBackupId(j + 1);
                        setClusterInfo(containerClusterInfo);
                        containers.add(createContainer());
                    }
                }
            }
            setClusterInfo(origClusterInfo);
            return new CompoundProcessingUnitContainer(containers.toArray(new ProcessingUnitContainer[containers.size()]));
        }

        // handle security
        if (userDetails != null) {
            try {
                beanLevelProperties.getContextProperties().put(Constants.Security.USER_DETAILS, new MarshalledObject(userDetails));
            } catch (IOException e) {
                throw new CannotCreateContainerException("Failed to marhsall user details", e);
            }
        } else if (secured != null) {
            beanLevelProperties.getContextProperties().setProperty(SpaceURL.SECURED, "true");
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

        return new IntegratedProcessingUnitContainer(applicationContext);
    }
}
