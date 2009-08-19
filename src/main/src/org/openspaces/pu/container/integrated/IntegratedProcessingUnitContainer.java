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

import com.gigaspaces.admin.cli.RuntimeInfo;
import com.gigaspaces.logger.GSLogConfigLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.pu.container.CannotCloseContainerException;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainer;
import org.openspaces.pu.container.support.BeanLevelPropertiesParser;
import org.openspaces.pu.container.support.ClusterInfoParser;
import org.openspaces.pu.container.support.CommandLineParser;
import org.openspaces.pu.container.support.ConfigLocationParser;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

/**
 * The integrated processing unit container wraps Spring
 * {@link org.springframework.context.ApplicationContext ApplicationContext}. It is created using
 * {@link org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider IntegratedProcessingUnitContainerProvider}.
 * <p/>
 * <p>An integrated processing unit container can be used to run a processing unit within an existing
 * environment. An example of what this existing environment will provide is the classpath that the
 * processing unit will run with. Examples for using the integrated processing unit container can be
 * integration tests or running the processing unit from within an IDE.
 * <p/>
 * <p>The integrated processing unit container also provides a a main method ({@link #main(String[])}
 * which uses the
 * {@link org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider} and the
 * provided parameters create itself. Please see the javadoc for the main method for a full list of
 * the possible parameters values.
 *
 * @author kimchy
 */
public class IntegratedProcessingUnitContainer implements ApplicationContextProcessingUnitContainer {

    private static final Log logger = LogFactory.getLog(IntegratedProcessingUnitContainer.class);

    private final ApplicationContext applicationContext;

    /**
     * Constructs a new integrated processing unit container based on the provided Spring
     * {@link org.springframework.context.ApplicationContext}.
     */
    public IntegratedProcessingUnitContainer(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Returns the spring application context this processing unit container wraps.
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Closes the processing unit container by destroying the Spring application context.
     */
    public void close() throws CannotCloseContainerException {
        if (applicationContext instanceof DisposableBean) {
            try {
                ((DisposableBean) applicationContext).destroy();
            } catch (Exception e) {
                throw new CannotCloseContainerException("Failed to close container with application context ["
                        + applicationContext + "]", e);
            }
        }
    }

    /**
     * Allows to run the integrated processing unit container. Uses the
     * {@link org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider} and
     * the parameters provided in order to configure it.
     * <p/>
     * <p/>
     * The following parameters are allowed:
     * <ul>
     * <li><b>-config [configLocation]</b>: Allows to add a Spring application context config
     * location. See
     * {@link org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider#addConfigLocation(String)}.
     * This is an optional parameter and it can be provided multiple times.</li>
     * <li><b>-properties [beanName] [properties]</b>: Allows to inject
     * {@link org.openspaces.core.properties.BeanLevelProperties}, see
     * {@link org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider#setBeanLevelProperties(org.openspaces.core.properties.BeanLevelProperties)}.
     * [beanName] is optional, if not used, the properties will set the
     * {@link org.openspaces.core.properties.BeanLevelProperties#setContextProperties(java.util.Properties)}.
     * If used, will inject properties only to the bean registered under the provided beanName
     * within the Spring context (see
     * {@link org.openspaces.core.properties.BeanLevelProperties#setBeanProperties(String,java.util.Properties)}).
     * The [properties] can either start with <code>embed://</code> which mean they will be
     * provided within the command line (for example:
     * <code>embed://propName1=propVal1;propName2=propVal2</code>) or they can follow Spring
     * {@link org.springframework.core.io.Resource} lookup based on URL syntax or Spring extended
     * <code>classpath</code> prefix (see
     * {@link org.springframework.core.io.DefaultResourceLoader}).</li>
     * <li><b>-cluster [cluster parameters]</b>: Allows to configure
     * {@link org.openspaces.core.cluster.ClusterInfo}, see
     * {@link org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider#setClusterInfo(org.openspaces.core.cluster.ClusterInfo)}.</li>
     * The following parameters are allowed: <code>total_members=1,1</code> (1,1 is an example
     * value), <code>id=1</code> (1 is an example value), <code>backup_id=1</code> (1 is an
     * example value) and <code>schema=primary_backup</code> (primary_backup is an example value).
     * No parameter is required. For more information regarding the Space meaning of this parameters
     * please consult GigaSpaces reference documentation within the Space URL section.
     * </ul>
     */
    public static void main(String[] args) throws Exception {
        GSLogConfigLoader.getLoader();
        System.setProperty("com.gs.printRuntimeInfo", "false");
        logger.info("Starting processing unit(s) with " + Arrays.toString(args));
        if (logger.isInfoEnabled()) {
            logger.info(RuntimeInfo.getEnvironmentInfo());
        }
        try {
            final ProcessingUnitContainer container = createContainer(args);
            logger.info("Processing unit(s) started successfully");

            // Use the MAIN thread as the non daemon thread to keep it alive
            final Thread mainThread = Thread.currentThread();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    try {
                        logger.info("Shutting down processing unit(s)");
                        container.close();
                    } finally {
                        mainThread.interrupt();
                    }
                }
            });
            while (!mainThread.isInterrupted()) {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                    // do nothing, simply exit
                }
            }
        } catch (Exception e) {
            printUsage();
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    public static ProcessingUnitContainer createContainer(String[] args) throws Exception {
        CommandLineParser.Parameter[] params = CommandLineParser.parse(args);

        IntegratedProcessingUnitContainerProvider provider = new IntegratedProcessingUnitContainerProvider();
        provider.setBeanLevelProperties(BeanLevelPropertiesParser.parse(params));
        provider.setClusterInfo(ClusterInfoParser.parse(params));
        ConfigLocationParser.parse(provider, params);

        String userName = null;
        String password = null;
        for (CommandLineParser.Parameter param : params) {
            if (param.getName().equals("user")) {
                userName = param.getArguments()[0];
            } else if (param.getName().equals("password")) {
                password = param.getArguments()[0];
            } else if (param.getName().equals("secured")) {
                provider.setSecured(Boolean.parseBoolean(param.getArguments()[0]));
            }
        }
        if (userName != null && password != null) {
            provider.setUserDetails(userName, password);
        }

        return provider.createContainer();
    }

    public static void printUsage() {
        System.out.println("Usage: [-cluster ...] [-properties ...] [-user xxx -password yyy] [-secured true/false]");
        System.out.println("    -cluster [cluster properties]: Allows specify cluster parameters");
        System.out.println("             schema=partitioned  : The cluster schema to use");
        System.out.println("             total_members=1,1   : The number of instances and number of backups to use");
        System.out.println("             id=1                : The instance id of this processing unit");
        System.out.println("             backup_id=1         : The backup id of this processing unit");
        System.out.println("    -properties [properties-loc] : Location of context level properties");
        System.out.println("    -properties [bean-name] [properties-loc] : Location of properties used applied only for a specified bean");
        System.out.println("    -user x -password y          : Configures a secured processing unit propagated with the supplied user and password");
        System.out.println("    -secured true                : Configures a secured processing unit (implicit when using -user/-password)");
        System.out.println("");
        System.out.println("");
        System.out.println("Some Examples:");
        System.out.println("1. -cluster schema=partitioned total_members=2 id=1");
        System.out.println("    - Starts a processing unit with a partitioned cluster schema of two members with instance id 1");
        System.out.println("2. -cluster schema=partitioned total_members=2 id=2");
        System.out.println("    - Starts a processing unit with a partitioned cluster schema of two members with instance id 2");
        System.out.println("3. -cluster schema=partitioned-sync2backup total_members=2,1 id=1 backup_id=1");
        System.out.println("    - Starts a processing unit with a partitioned sync2backup cluster schema of two members with two members each with one backup with instance id of 1 and backup id of 1");
        System.out.println("4. -properties file://config/context.properties -properties space1 file://config/space1.properties");
        System.out.println("    - Starts a processing unit called data-processor using context level properties called context.properties and bean level properties called space1.properties applied to bean named space1");
        System.out.println("5. -properties embed://prop1=value1 -properties space1 embed://prop2=value2;prop3=value3");
        System.out.println("    - Starts a processing unit called data-processor using context level properties with a single property called prop1 with value1 and bean level properties with two properties");
    }
}
