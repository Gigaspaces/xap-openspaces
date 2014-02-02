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

package org.openspaces.pu.container.servicegrid;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.gigaspaces.grid.zone.ZoneHelper;
import com.gigaspaces.internal.dump.InternalDump;
import com.gigaspaces.internal.dump.InternalDumpProcessor;
import com.gigaspaces.internal.dump.InternalDumpProcessorFailedException;
import com.gigaspaces.internal.io.BootIOUtils;
import com.gigaspaces.internal.jvm.JVMDetails;
import com.gigaspaces.internal.jvm.JVMHelper;
import com.gigaspaces.internal.jvm.JVMStatistics;
import com.gigaspaces.internal.os.OSDetails;
import com.gigaspaces.internal.os.OSHelper;
import com.gigaspaces.internal.os.OSStatistics;
import com.gigaspaces.lrmi.LRMIMonitoringDetails;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOInfoHelper;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import com.gigaspaces.management.entry.JMXConnection;
import com.gigaspaces.security.service.SecurityResolver;
import com.gigaspaces.start.Locator;
import com.gigaspaces.start.SystemBoot;
import com.j_spaces.core.IJSpace;
import com.j_spaces.core.admin.IInternalRemoteJSpaceAdmin;
import com.j_spaces.core.admin.RuntimeHolder;
import com.j_spaces.core.admin.StatisticsAdmin;
import com.j_spaces.core.client.SpaceURL;
import com.j_spaces.core.filters.StatisticsHolder;
import com.j_spaces.jmx.util.JMXUtilities;
import com.j_spaces.kernel.ClassLoaderHelper;
import com.j_spaces.kernel.Environment;
import net.jini.core.lookup.ServiceID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.boot.BootUtil;
import org.jini.rio.boot.CommonClassLoader;
import org.jini.rio.boot.PUZipUtils;
import org.jini.rio.boot.ServiceClassLoader;
import org.jini.rio.boot.SharedServiceData;
import org.jini.rio.core.JSBInstantiationException;
import org.jini.rio.core.SLA;
import org.jini.rio.core.ServiceLevelAgreements;
import org.jini.rio.core.jsb.ServiceBeanContext;
import org.jini.rio.jsb.ServiceBeanAdapter;
import org.jini.rio.watch.Calculable;
import org.jini.rio.watch.GaugeWatch;
import org.jini.rio.watch.Watch;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.cluster.ClusterInfoAware;
import org.openspaces.core.cluster.ClusterInfoPropertyPlaceholderConfigurer;
import org.openspaces.core.cluster.MemberAliveIndicator;
import org.openspaces.core.cluster.ProcessingUnitUndeployingListener;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.core.properties.BeanLevelPropertiesAware;
import org.openspaces.core.space.SpaceServiceDetails;
import org.openspaces.core.space.SpaceType;
import org.openspaces.core.util.ClassLoaderUtils;
import org.openspaces.core.util.PlaceholderReplacer;
import org.openspaces.core.util.PlaceholderReplacer.PlaceholderResolutionException;
import org.openspaces.interop.DotnetProcessingUnitContainerProvider;
import org.openspaces.pu.container.CannotCreateContainerException;
import org.openspaces.pu.container.ClassLoaderAwareProcessingUnitContainerProvider;
import org.openspaces.pu.container.DeployableProcessingUnitContainerProvider;
import org.openspaces.pu.container.ProcessingUnitContainer;
import org.openspaces.pu.container.ProcessingUnitContainerProvider;
import org.openspaces.pu.container.UndeployingEventProcessingUnitContainer;
import org.openspaces.pu.container.integrated.IntegratedProcessingUnitContainerProvider;
import org.openspaces.pu.container.jee.context.BootstrapWebApplicationContextListener;
import org.openspaces.pu.container.jee.jetty.JettyJeeProcessingUnitContainerProvider;
import org.openspaces.pu.container.servicegrid.jmxs.SecuredPUExtension;
import org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainer;
import org.openspaces.pu.container.spi.ApplicationContextProcessingUnitContainerProvider;
import org.openspaces.pu.container.support.BeanLevelPropertiesUtils;
import org.openspaces.pu.container.support.ClusterInfoParser;
import org.openspaces.pu.container.support.WebsterFile;
import org.openspaces.pu.service.InvocableService;
import org.openspaces.pu.service.InvocableServiceLookupFailureException;
import org.openspaces.pu.service.ServiceDetails;
import org.openspaces.pu.service.ServiceDetailsProvider;
import org.openspaces.pu.service.ServiceMonitors;
import org.openspaces.pu.service.ServiceMonitorsProvider;
import org.openspaces.pu.sla.monitor.ApplicationContextMonitor;
import org.openspaces.pu.sla.monitor.Monitor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.UnknownHostException;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author kimchy
 */
public class PUServiceBeanImpl extends ServiceBeanAdapter implements PUServiceBean, InternalDumpProcessor {

    private static final Log logger = LogFactory.getLog(PUServiceBeanImpl.class);

    private volatile ProcessingUnitContainer container;

    private int clusterGroup;

    private Integer instanceId;

    private Integer backupId;

    private volatile List<WatchTask> watchTasks = new ArrayList<WatchTask>();

    private volatile ScheduledExecutorService executorService;

    private volatile Callable<Boolean>[] memberAliveIndicators;

    private volatile Callable[] undeployingEventListeners;

    private volatile File deployPath;

    private volatile ClusterInfo clusterInfo;

    private volatile PUDetails puDetails;

    final private Collection<Callable> serviceMonitors = Collections.synchronizedCollection(new ArrayList<Callable>());

    final private Collection<InternalDumpProcessor> dumpProcessors = Collections.synchronizedCollection(new ArrayList<InternalDumpProcessor>());

    final private Map<String, InvocableService> invocableServiceMap = new ConcurrentHashMap<String, InvocableService>();

    private volatile boolean stopping = false;

    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");
    
    public PUServiceBeanImpl() {
        super();
    }

    /*
     * @see org.jini.rio.jsb.ServiceBeanAdapter#initializeJMX(java.lang.Object)
     */
    @Override
    protected void initializeJMX(Object mbean) throws Exception {
        if (SecurityResolver.isSecurityEnabled()) {
            mbean = new SecuredPUExtension(mbean);
        }
        super.initializeJMX(mbean);
    }

    @Override
    protected Object createProxy() {
        return PUServiceBeanProxy.getInstance((PUServiceBean) getExportedProxy(), getUuid());
    }

    @Override
    protected Object doStart(ServiceBeanContext context) throws Exception {
        this.context = context;
        org.openspaces.pu.sla.SLA sla = getSLA(context);
        if (sla.getMonitors() != null) {
            for (Monitor monitor : sla.getMonitors()) {
                String watchName = monitor.getName();
                Watch watch = new GaugeWatch(watchName, context.getConfiguration());
                if (watch.getWatchDataSource() == null) {
                    throw new JSBInstantiationException("Failed to add SLA monitor [" + watchName + "] for processing unit [" + getServiceBeanContext().getServiceElement().getName() + "]");
                }
                watch.getWatchDataSource().setSize(monitor.getHistorySize());
                context.getWatchRegistry().register(watch);
                watchTasks.add(new WatchTask(monitor, watch));
            }
        }

        String springXML = (String) context.getInitParameter("pu");
        clusterGroup = Integer.parseInt((String) context.getInitParameter("clusterGroup"));
        String sInstanceId = (String) context.getInitParameter("instanceId");
        if (sInstanceId != null) {
            instanceId = Integer.valueOf(sInstanceId);
        }
        String sBackupId = (String) context.getInitParameter("backupId");
        if (sBackupId != null) {
            backupId = Integer.valueOf(sBackupId);
        }

        ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            if (logger.isDebugEnabled()) {
                logger.debug(logMessage("contextClassLoader="+contextClassLoader+" origClassLoader="+origClassLoader));
            }
            
            startPU(springXML);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                StringBuilder classpath = new StringBuilder();
                if (ClassLoaderUtils.isClassLoaderProblem(e)) {
                    classpath.append(ClassLoaderUtils.getClassPathString(classLoader));
                }
                logger.debug(logMessage(
                     "Failed to start PU with xml [" + springXML + "] " + 
                     "Thread.currentThread().getContextClassLoader()="+classLoader + " " + classpath), e);
            }
            try {
                stopPU();
            } catch (Exception e1) {
                logger.debug(logMessage("Failed to destroy PU after failed start, ignoring"), e1);
            }
            cleanClassLoaders();
            throw new JSBInstantiationException("Failed to start processing unit [" + getServiceBeanContext().getServiceElement().getName() + "]", e, false);
        } finally {
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }

        try {
            // we set the context class loader so we export with it (note, if it is a web app class loader, we already set it)
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            return super.doStart(context);
        } finally {
            Thread.currentThread().setContextClassLoader(origClassLoader);
        }
    }

    private boolean isOnGsmHost() throws MalformedURLException, RemoteException, UnknownHostException {
        logger.debug("Determining if managing GSM is on the same host as this GSC");

        String deployHost = context.getServiceBeanManager().getOperationalStringManager().getDeployHost();
        logger.debug("Deploy host is " + deployHost);

        InetAddress localHost = InetAddress.getLocalHost();
        logger.debug("Local host is " + localHost);
        return deployHost.equals(localHost.getHostAddress());
    }

    
    /**
     * Override initialize to perform additional initialization
     */
    @Override
    public void initialize(ServiceBeanContext context) throws Exception {

        /* Initialize super class */
        super.initialize(context);

        /* Get the JMX Service URL */
        String jmxServiceURL = SystemBoot.getJMXServiceURL();

        if(jmxServiceURL != null) {
            String hostName = BootUtil.getHostAddress();
            JMXConnection jmxConnection = JMXUtilities.createJMXConnectionAttribute(
                    jmxServiceURL, context.getServiceElement().getName(), hostName );
            addAttribute( jmxConnection );
        }
    }

    @Override
    public void stop(boolean force) {
        // make sure to clean the web app class loader
        if (clusterInfo != null) {
            SharedServiceData.removeWebAppClassLoader(clusterInfo.getUniqueName());
            SharedServiceData.removeMemberAliveIndicator(clusterInfo.getUniqueName());
            SharedServiceData.removeUndeployingEventListeners(clusterInfo.getUniqueName());
            SharedServiceData.removeServiceDetails(clusterInfo.getUniqueName());
            SharedServiceData.removeServiceMonitors(clusterInfo.getUniqueName());
            SharedServiceData.removeDumpProcessors(clusterInfo.getUniqueName());
        }

        ClassLoader origClassLoader = Thread.currentThread().getContextClassLoader();
        stopping = true;
        try {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            logger.info(logMessage("Stopping ..."));
            stopPU();
            logger.info(logMessage("Stopped"));
        } finally {
            stopping = false;
            Thread.currentThread().setContextClassLoader(origClassLoader);
            this.serviceMonitors.clear();
            this.memberAliveIndicators = null;
            if (watchRegistry != null) {
                for (WatchTask watchTask : watchTasks) {
                    watchRegistry.deregister(watchTask.getWatch());
                }
            }
            watchTasks.clear();
        }

        super.stop(force);
    }

    private void startPU(String springXml) throws IOException, ClassNotFoundException {
        if (logger.isDebugEnabled()) {
            logger.debug(logMessage("Starting PU with [" + springXml + "]"));
        }

        String puName = (String) context.getInitParameter("puName");
        String puPath = (String) context.getInitParameter("puPath");
        String codeserver = context.getExportCodebase();

        org.openspaces.pu.sla.SLA sla = getSLA(getServiceBeanContext());

        Integer instanceId = this.instanceId;
        Integer backupId = this.backupId;

        // Derive instanceId and backupId if not explicitly set
        if (instanceId == null) {
            boolean hasBackups = sla.getNumberOfBackups() > 0;
            if (hasBackups) {
                instanceId = clusterGroup;
                //the first instance is primary so no backupid
                if (context.getServiceBeanConfig().getInstanceID().intValue() > 1) {
                    backupId = (context.getServiceBeanConfig().getInstanceID().intValue() - 1);
                }
            } else {
                instanceId = context.getServiceBeanConfig().getInstanceID().intValue();
            }
        }

        //set cluster info
        clusterInfo = new ClusterInfo();
        String clusterSchema = sla.getClusterSchema();
        if (clusterSchema != null) {
            clusterInfo.setSchema(clusterSchema);
            int slaMax = getSLAMax(context);
            int numberOfInstances = Math.max(slaMax, sla.getNumberOfInstances());
            clusterInfo.setNumberOfInstances(numberOfInstances);
        } else {
            clusterInfo.setNumberOfInstances(sla.getNumberOfInstances());
        }
        clusterInfo.setNumberOfBackups(sla.getNumberOfBackups());
        clusterInfo.setInstanceId(instanceId);
        clusterInfo.setBackupId(backupId);
        clusterInfo.setName(puName);

        ClusterInfoParser.guessSchema(clusterInfo);

        logger.info(logMessage("ClusterInfo [" + clusterInfo + "]"));

        MarshalledObject beanLevelPropertiesMarshObj =
                (MarshalledObject) getServiceBeanContext().getInitParameter("beanLevelProperties");
        BeanLevelProperties beanLevelProperties;
        if (beanLevelPropertiesMarshObj != null) {
            beanLevelProperties = (BeanLevelProperties) beanLevelPropertiesMarshObj.get();
            logger.info(logMessage("BeanLevelProperties " + beanLevelProperties));
        } else {
            beanLevelProperties = new BeanLevelProperties();
        }
        beanLevelProperties.getContextProperties().putAll(ClusterInfoPropertyPlaceholderConfigurer.createProperties(clusterInfo));

        // set a generic work location that can be used by container providers
        File workLocation = new File(System.getProperty("com.gs.work", Environment.getHomeDirectory() + "/work"));
        workLocation.mkdirs();

        beanLevelProperties.getContextProperties().setProperty("com.gs.work", workLocation.getAbsolutePath());

        boolean downloadPU = false;
        //create PU Container
        ProcessingUnitContainerProvider factory;
        // identify if this is a web app
        InputStream webXml = null;
        try {
            webXml = new URL(codeserver + puPath + "/WEB-INF/web.xml").openStream();
        } catch (IOException e) {
            // does not exists
        }
        // identify if this is a .NET one
        InputStream puConfig = null;
        try {
            puConfig = new URL(codeserver + puPath + "/pu.config").openStream();
        } catch (IOException e) {
            // does not exists
        }
        // identify if this is a .NET interop one
        InputStream puInteropConfig = null;
        try {
            puInteropConfig = new URL(codeserver + puPath + "/pu.interop.config").openStream();
        } catch (IOException e) {
            // does not exists
        }
        String processingUnitContainerProviderClass;
        if (webXml != null) {
            webXml.close();
            downloadPU = true;
            String jeeContainer = beanLevelProperties.getContextProperties().getProperty("jee.container", "jetty");
            String[] classesToLoad = null;
            if ("jetty".equals(jeeContainer)) {
                // pre load the jetty server class so the static shutdown thread will be loaded under it
                classesToLoad = new String[]{"org.eclipse.jetty.server.Server"};
            }
            // setup class loaders correcly
            try {
                Thread.currentThread().setContextClassLoader(CommonClassLoader.getInstance());
                ((ServiceClassLoader) contextClassLoader).setParentClassLoader(SharedServiceData.getJeeClassLoader(jeeContainer, classesToLoad));
            } catch (Exception e) {
                throw new CannotCreateContainerException("Failed to configure JEE class loader", e);
            } finally {
                Thread.currentThread().setContextClassLoader(contextClassLoader);
            }

            String className = StringUtils.capitalize(jeeContainer) + "JeeProcessingUnitContainerProvider";
            processingUnitContainerProviderClass = "org.openspaces.pu.container.jee." + jeeContainer + "." + className;
        } else if (puConfig != null) {
            puConfig.close();
            downloadPU = true;
            processingUnitContainerProviderClass = DotnetProcessingUnitContainerProvider.class.getName();
        } else if (puInteropConfig != null) {
            puInteropConfig.close();
            downloadPU = true;
            processingUnitContainerProviderClass = IntegratedProcessingUnitContainerProvider.class.getName();
        } else {
            processingUnitContainerProviderClass = IntegratedProcessingUnitContainerProvider.class.getName();
            if (beanLevelProperties.getContextProperties().getProperty("pu.download", "true").equalsIgnoreCase("true")) {
                downloadPU = true;
            }
        }

        if (beanLevelProperties != null) {
            processingUnitContainerProviderClass = beanLevelProperties.getContextProperties().getProperty(ProcessingUnitContainerProvider.CONTAINER_CLASS_PROP, processingUnitContainerProviderClass);
        }

        if (downloadPU) {
            String deployName = puName + "_" + clusterInfo.getRunningNumberOffset1();

            String deployedProcessingUnitsLocation = workLocation.getAbsolutePath() + "/processing-units";

            int uuid = Math.abs(new Random().nextInt());
            
            deployPath = new File(deployedProcessingUnitsLocation + "/" + deployName.replace('.', '_') + "_" + uuid);
            FileSystemUtils.deleteRecursively(deployPath);
            deployPath.mkdirs();

            // backward compatible
            beanLevelProperties.getContextProperties().setProperty("jee.deployPath", deployPath.getAbsolutePath());
            beanLevelProperties.getContextProperties().setProperty("dotnet.deployPath", deployPath.getAbsolutePath());

            beanLevelProperties.getContextProperties().setProperty(DeployableProcessingUnitContainerProvider.CONTEXT_PROPERTY_DEPLOY_PATH, deployPath.getAbsolutePath());

            try {
                if (isOnGsmHost()) {
                    copyPu(puPath, deployPath);
                } else {
                    long size = downloadAndExtractPU(puName, puPath, codeserver, deployPath,
                            new File(deployedProcessingUnitsLocation));
                    logDownloadSize(size);
                }
            } catch (MalformedURLException mle) {
                logger.warn("Could not determine if GSC and GSM are on the same host", mle);
                // fallbacל to download
                long size = downloadAndExtractPU(puName, puPath, codeserver, deployPath,
                        new File(deployedProcessingUnitsLocation));
                logDownloadSize(size);
            } catch (UnknownHostException unhe) {
                logger.warn("Could not determine if GSC and GSM are on the same host", unhe);
                // fallbacל to download
                long size = downloadAndExtractPU(puName, puPath, codeserver, deployPath,
                        new File(deployedProcessingUnitsLocation));
                logDownloadSize(size);
            } catch (RemoteException re) {
                logger.warn("Could not determine if GSC and GSM are on the same host", re);
                // fallbacל to download
                long size = downloadAndExtractPU(puName, puPath, codeserver, deployPath,
                        new File(deployedProcessingUnitsLocation));
                logDownloadSize(size);
            }

            // go over listed files that needs to be resolved with properties
            for (Map.Entry entry : beanLevelProperties.getContextProperties().entrySet()) {
                String key = (String) entry.getKey();
                if (key.startsWith("com.gs.resolvePlaceholder")) {
                    String path = (String) entry.getValue();
                    File input = new File(deployPath, path);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Resolving placeholder for file [" + input.getAbsolutePath() + "]");
                    }
                    BeanLevelPropertiesUtils.resolvePlaceholders(beanLevelProperties, input);
                }
            }
        }

        boolean sharedLibEnabled;
        if (beanLevelProperties.getContextProperties().containsKey("pu.shared-lib.enable")) {
            sharedLibEnabled = beanLevelProperties.getContextProperties().getProperty("pu.shared-lib").equals("true");
        } else {
            sharedLibEnabled = System.getProperty("com.gs.pu.shared-lib.enable", "false").equals("true");
        }

        final boolean disableManifestClassPathJars = Boolean.getBoolean("com.gs.pu.manifest.classpath.disable");
        final boolean disableManifestClassPathCommonPuJars = Boolean.getBoolean("com.gs.pu.manifest.classpath.common.disable");

        // this is used to inject the manifest jars to the webapp classloader (if exists)
        List<URL> manifestClassPathJars = new ArrayList<URL>();
        
        CommonClassLoader commonClassLoader = CommonClassLoader.getInstance();
        // handles class loader libraries
        if (downloadPU) {
            List<URL> libUrls = new ArrayList<URL>();
            File libDir = new File(deployPath, "lib");
            if (libDir.exists()) {
                File[] libFiles = BootIOUtils.listFiles(libDir);
                for (File libFile : libFiles) {
                    libUrls.add(libFile.toURI().toURL());
                }
            }
            
            if (!disableManifestClassPathJars) {
                File manifestFile = new File(deployPath, JarFile.MANIFEST_NAME);
                
                if (manifestFile.isFile()) {
                    try {
                        InputStream manifestStream = new FileInputStream(manifestFile);
                        manifestClassPathJars = getManifestClassPathJars(puName, manifestStream);
                        libUrls.addAll(manifestClassPathJars);
                    } catch (IOException e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn(failedReadingManifest(puName), e);
                        }
                    }
                }
                
            }
            
            // add to common class loader
            List<URL> sharedlibUrls = new ArrayList<URL>();
            File sharedlibDir = new File(deployPath, "shared-lib");
            if (sharedlibDir.exists()) {
                File[] sharedlibFiles = BootIOUtils.listFiles(sharedlibDir);
                for (File sharedlibFile : sharedlibFiles) {
                    sharedlibUrls.add(sharedlibFile.toURI().toURL());
                }
            }
            sharedlibDir = new File(deployPath, "WEB-INF/shared-lib");
            if (sharedlibDir.exists()) {
                File[] sharedlibFiles = BootIOUtils.listFiles(sharedlibDir);
                for (File sharedlibFile : sharedlibFiles) {
                    sharedlibUrls.add(sharedlibFile.toURI().toURL());
                }
            }

            if (sharedLibEnabled) {
                ((ServiceClassLoader) contextClassLoader).setSlashPath(deployPath.toURI().toURL());
                ((ServiceClassLoader) contextClassLoader).setLibPath(libUrls.toArray(new URL[libUrls.size()]));
                if (logger.isDebugEnabled()) {
                    logger.debug(logMessage("Service Class Loader " + Arrays.toString(((ServiceClassLoader) contextClassLoader).getURLs())));
                }

                commonClassLoader.addComponent(puName, sharedlibUrls.toArray(new URL[sharedlibUrls.size()]));
                if (logger.isDebugEnabled()) {
                    logger.debug(logMessage("Common Class Loader " + sharedlibUrls));
                }
            } else {
                if (sharedlibUrls.size() > 0) {
                    logger.warn("Using old 'shared-lib' directory, will add jars under it as if it was 'lib'");
                }
                libUrls.addAll(sharedlibUrls);

                // add pu-common jar files
                String gsLibOpt = System.getProperty(Locator.GS_LIB_OPTIONAL);
                String gsPuCommon = System.getProperty("com.gs.pu-common", gsLibOpt + "pu-common");

                final String gsLibOptSecurity = System.getProperty(Locator.GS_LIB_OPTIONAL_SECURITY, gsLibOpt + "security");
                libUrls.addAll(Arrays.asList(BootUtil.toURLs(new String[] {gsPuCommon, gsLibOptSecurity})));

                if (ScalaIdentifier.isScalaLibInClassPath()) {
                    String gsLibPlatform = System.getProperty(Locator.GS_LIB_PLATFORM);
                    // note that we assume BootUtil.toURLs does not work recursively here
                    // i.e, only gs-openspaces-scala.jar will be added and not all the files under /lib
                    String gsLibPlatformScala = gsLibPlatform + "scala";
                    libUrls.addAll(Arrays.asList(BootUtil.toURLs(new String[] {gsLibPlatformScala})));
                }
                
                if (!disableManifestClassPathJars &&
                    !disableManifestClassPathCommonPuJars) {
                    URLClassLoader urlClassLoader = new URLClassLoader(BootUtil.toURLs(new String[] {gsPuCommon}), null /* parent */);
                    InputStream puCommonManifestMF = urlClassLoader.getResourceAsStream(JarFile.MANIFEST_NAME);
                    if (puCommonManifestMF != null) {
                        List<URL> manifestClassPathComonPuJars = getManifestClassPathJars(puName, puCommonManifestMF);
                        manifestClassPathJars.addAll(manifestClassPathComonPuJars);
                        libUrls.addAll(manifestClassPathComonPuJars);
                    }
                }

                ((ServiceClassLoader) contextClassLoader).setSlashPath(deployPath.toURI().toURL());
                ((ServiceClassLoader) contextClassLoader).setLibPath(libUrls.toArray(new URL[libUrls.size()]));
                if (logger.isDebugEnabled()) {
                    logger.debug(logMessage("Service Class Loader " + Arrays.toString(((ServiceClassLoader) contextClassLoader).getURLs())));
                }
            }
            try {
                prepareWebApplication(deployPath, clusterInfo, beanLevelProperties);
            } catch (Exception e) {
                throw new CannotCreateContainerException("Failed to bootstrap web applciation", e);
            }
        } else {
            // add to service class loader
            List<URL> libUrls = new ArrayList<URL>();
            WebsterFile libDir = new WebsterFile(new URL(codeserver + puPath + "/lib"));
            File[] libFiles = libDir.listFiles();
            for (int i = 0; i < libFiles.length; i++) {
                libUrls.add(new URL(codeserver + puPath + "/lib/" + libFiles[i].getName()));
            }

            if (!disableManifestClassPathJars) {
                InputStream manifestStream = readManifestFromCodeServer(puName, puPath, codeserver, workLocation);
                if (manifestStream != null) {
                    manifestClassPathJars = getManifestClassPathJars(puName, manifestStream);
                    libUrls.addAll(manifestClassPathJars);
                }
            }
            
            // add to common class loader
            WebsterFile sharedlibDir = new WebsterFile(new URL(codeserver + puPath + "/shared-lib"));
            File[] sharedlibFiles = sharedlibDir.listFiles();
            List<URL> sharedlibUrls = new ArrayList<URL>();
            for (File sharedlibFile : sharedlibFiles) {
                sharedlibUrls.add(new URL(codeserver + puPath + "/shared-lib/" + sharedlibFile.getName()));
            }
            sharedlibDir = new WebsterFile(new URL(codeserver + puPath + "/WEB-INF/shared-lib"));
            sharedlibFiles = sharedlibDir.listFiles();
            for (File sharedlibFile : sharedlibFiles) {
                sharedlibUrls.add(new URL(codeserver + puPath + "/WEB-INF/shared-lib/" + sharedlibFile.getName()));
            }

            if (sharedLibEnabled) {
                ((ServiceClassLoader) contextClassLoader).setSlashPath(new URL(codeserver + puPath + "/"));
                ((ServiceClassLoader) contextClassLoader).setLibPath(libUrls.toArray(new URL[libUrls.size()]));
                if (logger.isDebugEnabled()) {
                    logger.debug(logMessage("Service Class Loader " + Arrays.toString(((ServiceClassLoader) contextClassLoader).getURLs())));
                }

                commonClassLoader.addComponent(puName, sharedlibUrls.toArray(new URL[sharedlibUrls.size()]));
                if (logger.isDebugEnabled()) {
                    logger.debug(logMessage("Common Class Loader " + sharedlibUrls));
                }
            } else {
                if (sharedlibUrls.size() > 0) {
                    logger.warn("Using old 'shared-lib' directory, will add jars under it as if it was 'lib'");
                }
                libUrls.addAll(sharedlibUrls);
                ((ServiceClassLoader) contextClassLoader).setSlashPath(new URL(codeserver + puPath + "/"));
                ((ServiceClassLoader) contextClassLoader).setLibPath(libUrls.toArray(new URL[libUrls.size()]));
                if (logger.isDebugEnabled()) {
                    logger.debug(logMessage("Service Class Loader " + Arrays.toString(((ServiceClassLoader) contextClassLoader).getURLs())));
                }
            }
        }

        // handle mule os if there is one class loader
        try {
            contextClassLoader.loadClass("org.mule.api.MuleContext");
            ((ServiceClassLoader) contextClassLoader).addURLs(BootUtil.toURLs(new String[]
                    {
                            Environment.getHomeDirectory() + "/lib/optional/openspaces/mule-os.jar"
                    }));
        } catch (Throwable e) {
            // no mule
        }

        factory = createContainerProvider(processingUnitContainerProviderClass);
        if (factory instanceof DeployableProcessingUnitContainerProvider) {
            ((DeployableProcessingUnitContainerProvider) factory).setDeployPath(deployPath);
        }
        if (factory instanceof ClassLoaderAwareProcessingUnitContainerProvider) {
            ((ClassLoaderAwareProcessingUnitContainerProvider) factory).setClassLoader(contextClassLoader);
        }
        if (factory instanceof JettyJeeProcessingUnitContainerProvider) {
            ((JettyJeeProcessingUnitContainerProvider) factory).setManifestUrls(manifestClassPathJars);
        }
        
        // only load the spring xml file if it is not a web application (if it is a web application, we will load it with the Bootstrap servlet context loader)
        if (webXml == null && factory instanceof ApplicationContextProcessingUnitContainerProvider) {
            if (StringUtils.hasText(springXml)) {
                // GS-9350: if this is a processing unit with gateway declarations, always try to
                // re-load the pu.xml to support "hot-deploy" (refresh)
                if (springXml.contains("os-gateway:")) {
                    String deployPath = beanLevelProperties.getContextProperties().getProperty("deployPath");
                    if (StringUtils.hasText(deployPath)) {
                        String newSpringXml = readFile(deployPath+"/META-INF/spring/pu.xml");
                        if (StringUtils.hasText(newSpringXml)) {
                            springXml = newSpringXml; //override with new one
                        }
                    }
                }
                Resource resource = new ByteArrayResource(springXml.getBytes());
                ((ApplicationContextProcessingUnitContainerProvider) factory).addConfigLocation(resource);
            }
        }
        if (factory instanceof ClusterInfoAware) {
            ((ClusterInfoAware) factory).setClusterInfo(clusterInfo);
        }
        if (factory instanceof BeanLevelPropertiesAware) {
            ((BeanLevelPropertiesAware) factory).setBeanLevelProperties(beanLevelProperties);
        }

        container = factory.createContainer();

        // set the context class loader to the web app class loader if there is one
        // this menas that from now on, and the exported service, will use the context class loader
        ClassLoader webAppClassLoader = SharedServiceData.removeWebAppClassLoader(clusterInfo.getUniqueName());
        if (webAppClassLoader != null) {
            contextClassLoader = webAppClassLoader;
        }
        Thread.currentThread().setContextClassLoader(contextClassLoader);

        buildMembersAliveIndicators();
        buildUndeployingEventListeners();
        buildDumpProcessors();

        ArrayList<Object> serviceDetails = buildServiceDetails();

        buildServiceMonitors();

        buildInvocableServices();

        this.puDetails = new PUDetails(context.getParentServiceID(), clusterInfo, beanLevelProperties, serviceDetails.toArray(new Object[serviceDetails.size()]));

        if (container instanceof ApplicationContextProcessingUnitContainer) {
            ApplicationContext applicationContext = ((ApplicationContextProcessingUnitContainer) container).getApplicationContext();

            // inject the application context to all the monitors and schedule them
            // currently use the number of threads in relation to the number of monitors
            int numberOfThreads = watchTasks.size() / 5;
            if (numberOfThreads == 0) {
                numberOfThreads = 1;
            }
            executorService = Executors.newScheduledThreadPool(numberOfThreads);
            for (WatchTask watchTask : watchTasks) {
                if (watchTask.getMonitor() instanceof ApplicationContextMonitor) {
                    ((ApplicationContextMonitor) watchTask.getMonitor()).setApplicationContext(applicationContext);
                }
                executorService.scheduleAtFixedRate(watchTask, watchTask.getMonitor().getPeriod(), watchTask.getMonitor().getPeriod(), TimeUnit.MILLISECONDS);
            }
        }
    }

    private long copyPu(String puPath, File puWorkFolder) throws IOException {
        String puDeployPath = context.getServiceBeanManager().getOperationalStringManager().getDeployPath() + "/" + puPath;
        File src = new File(puDeployPath);
        logger.info("Copying from GSM [" + puDeployPath + "] to " + "[" + deployPath + "] ...");
        FileSystemUtils.copyRecursively(src, puWorkFolder);
        return src.getTotalSpace();
    }

    private void logDownloadSize(long size) {
        if (logger.isInfoEnabled()) {
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(2);
            String suffix = "kb";
            float factor = 1024;
            if (size > 1024 * 1024) {
                suffix = "mb";
                factor = 1024 * 1024;
            }
            logger.info("Downloaded [" + nf.format(size / factor) + suffix + "] to [" + deployPath + "]");
        }
    }

    private InputStream readManifestFromCodeServer(String puName, String puPath, String codeserver, File workLocation) {
        try {
            URL manifestURL = new URL(codeserver + puPath + "/" + JarFile.MANIFEST_NAME);
            return manifestURL.openStream();
        } catch (IOException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(failedReadingManifest(puName), e);
            }
            return null;
        }
    }

    private List<URL> getManifestClassPathJars(String puName, InputStream manifestStream) {
        List<URL> exportedManifestUrls = new ArrayList<URL>();
        try {
            
            Manifest manifest = new Manifest(manifestStream);
            
            String manifestClasspathLibs = manifest.getMainAttributes().getValue("Class-Path");
            if (manifestClasspathLibs != null) {
                String[] manifestLibFiles = StringUtils.tokenizeToStringArray(manifestClasspathLibs, " ");
                for (String fileName : manifestLibFiles) {

                    try {
                        fileName = PlaceholderReplacer.replacePlaceholders(System.getenv(), fileName);
                    } catch (PlaceholderResolutionException e) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Could not resolve manifest classpath entry: " + fileName + 
                                    " of processing unit " + puName, e);
                        }
                        continue;
                    }
                    
                    URL url = null;
                    try {
                        url = new URL(fileName);
                        if (!"file".equals(url.getProtocol())) {
                            if (logger.isWarnEnabled()) {
                                logger.warn("Only file protocol is supported in urls, found : '" + fileName +
                                        "' in manifest classpath of processing unit " + puName);
                            }
                            continue;
                        }
                    } catch (MalformedURLException e) {
                        // moving on, checking if file path has been provided
                    }
                    
                    File file;
                    if (url != null) {
                        try {
                            file = new File(url.toURI());
                        } catch (URISyntaxException e) {
                            if (logger.isWarnEnabled()) {
                                logger.warn("Invalid url: " + url + " provided in pu " +
                                        puName + " manifest classpath, ignoring entry", e);
                            }
                            continue;
                        }
                    } else {
                        file = new File(fileName);
                    }
                    
                    if (!file.isAbsolute()) {
                        file = new File(Environment.getHomeDirectory(), fileName);
                    }
                    
                    if (!file.isFile()) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("Did not find manifest classpath entry: " + file.getAbsolutePath() + 
                                    " for processing unit: " + puName + ", ignoring entry.");
                        }
                        continue;
                    }
                    
                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding " + file.getAbsolutePath() + 
                                " read from MANIFEST.MF to processing unit: " + puName + " classpath");
                    }
                    
                    URL urlToAdd = file.toURI().toURL();
                    exportedManifestUrls.add(urlToAdd);
                }
            }
        } catch (IOException e) {
            if (logger.isWarnEnabled()) {
                logger.warn(failedReadingManifest(puName), e);
            }
        } finally {
            try {
                manifestStream.close();
            } catch (IOException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Failed closing manifest input stream.", e);
                }
            }
        }
        return exportedManifestUrls;
    }
    
    private static String failedReadingManifest(String puName) {
        return "Could not read MANIFEST.MF in processing unit: , if you have no Class-Path defined in a MANIFEST.MF" +
               ", you can safely ignore this message." + puName;
    }
    
    private static String readFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        FileInputStream fis = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line).append(LINE_SEPARATOR);
        }
        reader.close();
        fis.close();
        return buffer.toString();
    }


    protected void buildInvocableServices() {
        if (container instanceof InvocableService) {
            invocableServiceMap.put("__container__", (InvocableService) container);
        }
        //not always true (e.g. DotNet and JEE PU container)
        if (container instanceof ApplicationContextProcessingUnitContainer) {
            ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) ((ApplicationContextProcessingUnitContainer) container).getApplicationContext();
            final Map<String, InvocableService> appContextInvocableServiceMap = applicationContext.getBeansOfType(InvocableService.class);
            logger.info("registering invocable services: " + appContextInvocableServiceMap);
            invocableServiceMap.putAll(appContextInvocableServiceMap);
        }
    }

    private void buildServiceMonitors() {
        if (container instanceof ServiceMonitorsProvider) {
            serviceMonitors.add(new Callable() {
                public Object call() throws Exception {
                    return ((ServiceMonitorsProvider) container).getServicesMonitors();
                }
            });
        }

        if (container instanceof ApplicationContextProcessingUnitContainer) {
            ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) ((ApplicationContextProcessingUnitContainer) container).getApplicationContext();
            final Map<String, ServiceMonitorsProvider> monitorsMap = applicationContext.getBeansOfType(ServiceMonitorsProvider.class);
            serviceMonitors.add(new Callable() {
                public Object call() throws Exception {
                    ArrayList<ServiceMonitors> retMonitors = new ArrayList<ServiceMonitors>();
                    for (ServiceMonitorsProvider serviceMonitorsProvider : monitorsMap.values()) {
                        ServiceMonitors[] monitors = serviceMonitorsProvider.getServicesMonitors();
                        if (monitors != null) {
                            for (ServiceMonitors mon : monitors) {
                                retMonitors.add(mon);
                            }
                        }
                    }
                    return retMonitors.toArray(new Object[retMonitors.size()]);
                }
            });
        }

        List<Callable> sharedMonitors = SharedServiceData.removeServiceMonitors(clusterInfo.getUniqueName());
        if (sharedMonitors != null) {
            serviceMonitors.addAll(sharedMonitors);
        }
    }

    private void buildDumpProcessors() {
        if (container instanceof InternalDumpProcessor) {
            dumpProcessors.add((InternalDumpProcessor) container);
        }

        if (container instanceof ApplicationContextProcessingUnitContainer) {
            ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) ((ApplicationContextProcessingUnitContainer) container).getApplicationContext();
            final Map<String, InternalDumpProcessor> dumpProcessorsMap = applicationContext.getBeansOfType(InternalDumpProcessor.class);
            dumpProcessors.addAll(dumpProcessorsMap.values());
        }

        List<Object> sharedDumpProcessors = SharedServiceData.removeDumpProcessors(clusterInfo.getUniqueName());
        if (sharedDumpProcessors != null) {
            for (Object dumpProcesosr : sharedDumpProcessors) {
                if (dumpProcesosr instanceof InternalDumpProcessor) {
                    dumpProcessors.add((InternalDumpProcessor) dumpProcesosr);
                }
            }
        }
    }

    private ArrayList<Object> buildServiceDetails() {
        ArrayList<Object> serviceDetails = new ArrayList<Object>();

        if (container instanceof ServiceDetailsProvider) {
            ServiceDetails[] details = ((ServiceDetailsProvider) container).getServicesDetails();
            if (details != null) {
                for (ServiceDetails detail : details) {
                    serviceDetails.add(detail);
                }
            }
        }

        if (container instanceof ApplicationContextProcessingUnitContainer) {
            ConfigurableApplicationContext applicationContext = (ConfigurableApplicationContext) ((ApplicationContextProcessingUnitContainer) container).getApplicationContext();
            Map<String, ServiceDetailsProvider> map = applicationContext.getBeansOfType(ServiceDetailsProvider.class);
            for (ServiceDetailsProvider serviceDetailsProvider : map.values()) {
                ServiceDetails[] details = serviceDetailsProvider.getServicesDetails();
                if (details != null) {
                    for (ServiceDetails detail : details) {
                        serviceDetails.add(detail);
                    }
                }
            }
        }

        List<Callable> serviceDetailsProvider = SharedServiceData.removeServiceDetails(clusterInfo.getUniqueName());
        if (serviceDetailsProvider != null) {
            for (Callable serProvider : serviceDetailsProvider) {
                try {
                    Object[] details = (Object[]) serProvider.call();
                    Collections.addAll(serviceDetails, details);
                } catch (Exception e) {
                    logger.error("Failed to add service details from custom provider", e);
                }
            }
        }
        return serviceDetails;
    }

    private void buildUndeployingEventListeners() {
        ArrayList<Callable<?>> undeployingListeners = new ArrayList<Callable<?>>();
        if (container instanceof ApplicationContextProcessingUnitContainer) {
            ApplicationContext applicationContext = ((ApplicationContextProcessingUnitContainer) container).getApplicationContext();
            Map<String, ProcessingUnitUndeployingListener> map = applicationContext.getBeansOfType(ProcessingUnitUndeployingListener.class);
            for (final ProcessingUnitUndeployingListener listener : map.values()) {
                undeployingListeners.add(new Callable<Object>() {
                    public Object call() throws Exception {
                        listener.processingUnitUndeploying();
                        return null;
                    }
                });
            }
        }

        List<Callable> list = SharedServiceData.removeUndeployingEventListeners(clusterInfo.getUniqueName());
        if (list != null) {
            for (Callable c : list) {
                undeployingListeners.add(c);
            }
        }
        undeployingEventListeners = undeployingListeners.toArray(new Callable[undeployingListeners.size()]);
    }

    private void buildMembersAliveIndicators() {
        // Handle Member Alive Indicators
        ArrayList<Callable<Boolean>> maIndicators = new ArrayList<Callable<Boolean>>();
        if (container instanceof ApplicationContextProcessingUnitContainer) {
            ApplicationContext applicationContext = ((ApplicationContextProcessingUnitContainer) container).getApplicationContext();
            Map<String, MemberAliveIndicator> map = applicationContext.getBeansOfType(MemberAliveIndicator.class);
            for (final MemberAliveIndicator memberAliveIndicator : map.values()) {
                if (memberAliveIndicator.isMemberAliveEnabled()) {
                    maIndicators.add(new Callable<Boolean>() {
                        public Boolean call() throws Exception {
                            return memberAliveIndicator.isAlive();
                        }
                    });
                }
            }
        }

        List<Callable<Boolean>> memberAliveIndicatorProvider = SharedServiceData.removeMemberAliveIndicator(clusterInfo.getUniqueName());
        if (memberAliveIndicatorProvider != null) {
            for (Callable<Boolean> c : memberAliveIndicatorProvider) {
                maIndicators.add(c);
            }
        }
        memberAliveIndicators = maIndicators.toArray(new Callable[maIndicators.size()]);
    }

    private org.openspaces.pu.sla.SLA getSLA(ServiceBeanContext context) throws IOException, ClassNotFoundException {
        MarshalledObject slaMarshObj = (MarshalledObject) context.getInitParameter("sla");
        return (org.openspaces.pu.sla.SLA) slaMarshObj.get();
    }

    private void stopPU() {
        // make sure to clean shared services
        if (clusterInfo != null) {
            SharedServiceData.removeWebAppClassLoader(clusterInfo.getUniqueName());
            SharedServiceData.removeMemberAliveIndicator(clusterInfo.getUniqueName());
            SharedServiceData.removeUndeployingEventListeners(clusterInfo.getUniqueName());
            SharedServiceData.removeServiceDetails(clusterInfo.getUniqueName());
            SharedServiceData.removeServiceMonitors(clusterInfo.getUniqueName());
            SharedServiceData.removeDumpProcessors(clusterInfo.getUniqueName());
        }

        serviceMonitors.clear();
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
        if (container != null) {
            try {
                container.close();
            } catch (Exception e) {
                logger.warn(logMessage("Failed to close"), e);
            } finally {
                container = null;
                undeployingEventListeners = null;
                memberAliveIndicators = null;
                puDetails = null;
            }
        }

        // clean the deploy path directory
        if (deployPath != null) {
            boolean deleted = false;
            for (int i = 0; i < 2; i++) {
                deleted = FileSystemUtils.deleteRecursively(deployPath);
                if (deleted) {
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
            if (!deleted) {
                String message = "Failed to delete deployed processing unit from [" + deployPath + "]";
                logger.warn(message);
            }
        }
    }

    @Override
    public void undeployEvent() {
        super.undeployEvent();
        if (container instanceof UndeployingEventProcessingUnitContainer) {
            ((UndeployingEventProcessingUnitContainer) container).processingUnitUndeploying();
        }
        for (Callable c : undeployingEventListeners) {
            try {
                c.call();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    public boolean isMemberAliveEnabled() {
        return (memberAliveIndicators != null && memberAliveIndicators.length > 0);
    }

    public boolean isAlive() throws Exception {
        if (stopping) {
            // when we are stopping, don't check for member alive since the system might be in
            // inconsistent state and we should not "bother" it with checking for liveness
            return true;
        }
        if (memberAliveIndicators == null || memberAliveIndicators.length == 0) {
            return true;
        }
        boolean alive = false;
        for (Callable<Boolean> memberAliveIndicator : memberAliveIndicators) {
            alive = memberAliveIndicator.call();
            if (!alive) {
                break;
            }
        }
        return alive;
    }

    public IJSpace getSpaceDirect(ServiceID serviceID) throws RemoteException {
        if (puDetails == null) {
            throw new RemoteException("processing unit shutdown");
        }
        for (Object serviceDetails : puDetails.getDetails()) {
            if (serviceDetails.getClass().getName().equals(SpaceServiceDetails.class.getName())) {
                try {
                    Method spaceType = serviceDetails.getClass().getMethod("getSpaceType");
                    if (spaceType.invoke(serviceDetails).toString().equals(SpaceType.EMBEDDED.toString())) {
                        Method spaceServiceId = serviceDetails.getClass().getMethod("getServiceID");
                        if (spaceServiceId.invoke(serviceDetails).equals(serviceID)) {
                            Field spaceDetails = serviceDetails.getClass().getDeclaredField("directSpace");
                            spaceDetails.setAccessible(true);
                            return (IJSpace) spaceDetails.get(serviceDetails);
                        }
                    }
                } catch (Exception e) {
                    throw new RemoteException("Failed to get space", e);
                }
            }
        }
        return null;
    }

    public RuntimeHolder getSpaceRuntimeHolder(ServiceID serviceID) throws RemoteException {
        if (puDetails == null) {
            throw new RemoteException("processing unit shutdown");
        }
        for (Object serviceDetails : puDetails.getDetails()) {
            if (serviceDetails.getClass().getName().equals(SpaceServiceDetails.class.getName())) {
                try {
                    Method spaceType = serviceDetails.getClass().getMethod("getSpaceType");
                    if (spaceType.invoke(serviceDetails).toString().equals(SpaceType.EMBEDDED.toString())) {
                        Method spaceServiceId = serviceDetails.getClass().getMethod("getServiceID");
                        if (spaceServiceId.invoke(serviceDetails).equals(serviceID)) {
                            Field spaceDetails = serviceDetails.getClass().getDeclaredField("directSpaceAdmin");
                            spaceDetails.setAccessible(true);
                            IInternalRemoteJSpaceAdmin spaceAdmin = (IInternalRemoteJSpaceAdmin) spaceDetails.get(serviceDetails);
                            return spaceAdmin.getRuntimeHolder();
                        }
                    }
                } catch (Exception e) {
                    throw new RemoteException("Failed to get runtime holder", e);
                }
            }
        }
        return null;
    }

    public StatisticsHolder getSpaceStatisticsHolder(ServiceID serviceID) throws RemoteException {
        if (puDetails == null) {
            throw new RemoteException("processing unit shutdown");
        }
        for (Object serviceDetails : puDetails.getDetails()) {
            if (serviceDetails.getClass().getName().equals(SpaceServiceDetails.class.getName())) {
                try {
                    Method spaceType = serviceDetails.getClass().getMethod("getSpaceType");
                    if (spaceType.invoke(serviceDetails).toString().equals(SpaceType.EMBEDDED.toString())) {
                        Method spaceServiceId = serviceDetails.getClass().getMethod("getServiceID");
                        if (spaceServiceId.invoke(serviceDetails).equals(serviceID)) {
                            Field spaceDetails = serviceDetails.getClass().getDeclaredField("directSpaceAdmin");
                            spaceDetails.setAccessible(true);
                            IInternalRemoteJSpaceAdmin spaceAdmin = (IInternalRemoteJSpaceAdmin) spaceDetails.get(serviceDetails);
                            return ((StatisticsAdmin) spaceAdmin).getHolder();
                        }
                    }
                } catch (Exception e) {
                    throw new RemoteException("Failed to get statistics holder", e);
                }
            }
        }
        return null;
    }

    public SpaceURL[] listSpacesURLs() throws RemoteException {
        if (puDetails == null) {
            throw new RemoteException("processing unit shutdown");
        }
        List<SpaceURL> spaceUrls = new ArrayList<SpaceURL>();
        for (Object serviceDetails : puDetails.getDetails()) {
            if (serviceDetails.getClass().getName().equals(SpaceServiceDetails.class.getName())) {
                try {
                    Method spaceType = serviceDetails.getClass().getMethod("getSpaceType");
                    if (spaceType.invoke(serviceDetails).toString().equals(SpaceType.EMBEDDED.toString())) {
                        Field spaceDetails = serviceDetails.getClass().getDeclaredField("space");
                        spaceDetails.setAccessible(true);
                        IJSpace space = (IJSpace) spaceDetails.get(serviceDetails);
                        spaceUrls.add(space.getFinderURL());
                    }
                } catch (Exception e) {
                    throw new RemoteException("Failed to get space url", e);
                }
            }
        }
        return spaceUrls.toArray(new SpaceURL[spaceUrls.size()]);
    }

    public SpaceMode[] listSpacesModes() throws RemoteException {
        if (puDetails == null) {
            throw new RemoteException("processing unit shutdown");
        }
        List<SpaceMode> spacesModes = new ArrayList<SpaceMode>();
        for (Object serviceDetails : puDetails.getDetails()) {
            if (serviceDetails.getClass().getName().equals(SpaceServiceDetails.class.getName())) {
                try {
                    Method spaceType = serviceDetails.getClass().getMethod("getSpaceType");
                    if (spaceType.invoke(serviceDetails).toString().equals(SpaceType.EMBEDDED.toString())) {
                        Field spaceDetails = serviceDetails.getClass().getDeclaredField("space");
                        spaceDetails.setAccessible(true);
                        IJSpace space = (IJSpace) spaceDetails.get(serviceDetails);
                        spacesModes.add(((IInternalRemoteJSpaceAdmin) space.getAdmin()).getSpaceMode());
                    }
                } catch (Exception e) {
                    throw new RemoteException("Failed to get space mode", e);
                }
            }
        }
        return spacesModes.toArray(new SpaceMode[spacesModes.size()]);
    }

    public PUMonitors getPUMonitors() throws RemoteException {
        ArrayList<Object> monitors = new ArrayList<Object>();
        synchronized (serviceMonitors) {
            for (Callable call : serviceMonitors) {
                try {
                    Collections.addAll(monitors, (Object[]) call.call());
                } catch (Exception e) {
                    logger.error(logMessage("Failed to get monitor information, ignoring it"), e);
                }
            }
        }
        return new PUMonitors(monitors.toArray(new Object[monitors.size()]));
    }

    public PUDetails getPUDetails() throws RemoteException {
        return this.puDetails;
    }

    public String getPresentationName() throws RemoteException {
        if (puDetails == null) {
            throw new RemoteException("processing unit shutdown");
        }
        return this.puDetails.getPresentationName();
    }

    public ClusterInfo getClusterInfo() throws RemoteException {
        return this.clusterInfo;
    }

    public Object[] listServiceDetails() throws RemoteException {
        if (puDetails == null) {
            return new ServiceDetails[0];
        }
        return puDetails.getDetails();
    }

    private int getMaxServiceCount(String[] args) {
        int count = -1;
        for (String arg : args) {
            if (arg.indexOf("ScalingPolicyHandler.MaxServices") != -1) {
                StringTokenizer tok = new StringTokenizer(arg, " =");
                /* first token is "ScalingPolicyHandler.MaxServices" */
                tok.nextToken();
                String value = tok.nextToken();
                try {
                    count = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return (count);
    }

    private int getSLAMax(ServiceBeanContext context) {
        int max = -1;
        ServiceLevelAgreements slas =
                context.getServiceElement().getServiceLevelAgreements();
        SLA[] spaceSLAs = slas.getServiceSLAs();
        for (SLA spaceSLA : spaceSLAs) {
            int count =
                    getMaxServiceCount(spaceSLA.getConfigArgs());
            if (count != -1) {
                max = count;
                break;
            }
        }
        return max;
    }

    private String logMessage(String message) {
        return message;
    }

    private static class WatchTask implements Runnable {

        private final Monitor monitor;

        private final Watch watch;

        public WatchTask(Monitor monitor, Watch watch) {
            this.monitor = monitor;
            this.watch = watch;
        }

        public Monitor getMonitor() {
            return monitor;
        }

        public Watch getWatch() {
            return watch;
        }

        public void run() {
            watch.addWatchRecord(new Calculable(watch.getId(), monitor.getValue(), System.currentTimeMillis()));
        }
    }

    private ProcessingUnitContainerProvider createContainerProvider(String containerProviderType) {
        try {
            return (ProcessingUnitContainerProvider) ClassUtils.forName(containerProviderType, contextClassLoader).newInstance();
        } catch (Exception e) {
            throw new CannotCreateContainerException("Failed to create a new instance of container [" + containerProviderType + "]", e);
        }
    }

    private long downloadAndExtractPU(String puName, String puPath, String codeserver, File path, File tempPath) {
        URL url = null;
        //use 3 retries to acquire URL connection
        //it seems that after network disconnection, GSC which gets instantiation request has a stale connection to GSM
        for (int i=3; i>0; --i) {
            try {
                url = new URL(context.getServiceBeanManager().getOperationalStringManager().getDeployURL() + "/" + puPath);
                break;
            } catch (Exception e) {
                if (i==1) {
                    //throw exception on last retry attempt
                    throw new CannotCreateContainerException("Failed to construct URL to download processing unit", e);
                }
            }
        }

        if (logger.isInfoEnabled()) {
            logger.info("Downloading from GSM [" + url.toExternalForm() + "] to [" + deployPath + "] ...");
        }

        try {
            return PUZipUtils.downloadProcessingUnit(puName, url, path, tempPath);
        } catch (Exception e) {
            throw new CannotCreateContainerException("Failed to download processing unit [" + puName + "]", e);
        }
    }

    private void prepareWebApplication(File deployPath, ClusterInfo clusterInfo, BeanLevelProperties beanLevelProperties) throws Exception {
        if (!(new File(deployPath, "WEB-INF/web.xml").exists())) {
            return;
        }
        BootstrapWebApplicationContextListener.prepareForBoot(deployPath, clusterInfo, beanLevelProperties);

        // if we download, we can delete JSpaces and jini jars from the WEB-INF/lib (they are already in the
        // common class loader).
        File webInfLib = new File(deployPath, "WEB-INF/lib");
        webInfLib.mkdirs();
        if (webInfLib.exists()) {
            String gsRequired = System.getProperty(Locator.GS_LIB_REQUIRED);
            String gsOptional = System.getProperty(Locator.GS_LIB_OPTIONAL);
            String gsPlatform = System.getProperty(Locator.GS_LIB_PLATFORM);
            try {
                FileSystemUtils.copyRecursively(new File(gsRequired), new File(deployPath, "WEB-INF/lib"));
                FileSystemUtils.copyRecursively(new File(gsOptional + "/spring"), new File(deployPath, "WEB-INF/lib"));
                FileSystemUtils.copyRecursively(new File(gsOptional + "/security"), new File(deployPath, "WEB-INF/lib"));
                if (ScalaIdentifier.isScalaLibInClassPath()) {
                    FileSystemUtils.copyRecursively(new File(gsPlatform + "/scala/gs-openspaces-scala.jar"), new File(deployPath, "WEB-INF/lib/gs-openspaces-scala.jar"));
                }
                logger.debug(logMessage("Added spring jars to web application"));
            } catch (IOException e) {
                // don't copy it
            }
            File[] wenInfJars = BootIOUtils.listFiles(webInfLib);
            ArrayList<String> deleted = new ArrayList<String>();
            for (File webInfJarFile : wenInfJars) {
                boolean delete = false;
                if (webInfJarFile.getName().startsWith("gs-runtime")) {
                    delete = true;
                }
                if (delete) {
                    deleted.add(webInfJarFile.getName());
                    webInfJarFile.delete();
                }
            }
            if (!deleted.isEmpty()) {
                logger.debug(logMessage("Deleted the following jars from the web application: " + deleted));
            }
        }
    }

    public NIODetails getNIODetails() throws RemoteException {
        return NIOInfoHelper.getDetails();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        return NIOInfoHelper.getNIOStatistics();
    }
    
    @Override
    public void enableLRMIMonitoring() throws RemoteException {
        NIOInfoHelper.enableMonitoring();
    }
    
    @Override
    public void disableLRMIMonitoring() throws RemoteException {
        NIOInfoHelper.disableMonitoring();
    }
    
    @Override
    public LRMIMonitoringDetails fetchLRMIMonitoringDetails() throws RemoteException {
        return NIOInfoHelper.fetchMonitoringDetails();
    }

    public long getCurrentTimestamp() throws RemoteException {
        return System.currentTimeMillis();
    }

    public OSDetails getOSDetails() throws RemoteException {
        return OSHelper.getDetails();
    }

    public OSStatistics getOSStatistics() throws RemoteException {
        return OSHelper.getStatistics();
    }

    public JVMDetails getJVMDetails() throws RemoteException {
        return JVMHelper.getDetails();
    }

    public JVMStatistics getJVMStatistics() throws RemoteException {
        return JVMHelper.getStatistics();
    }

    public void runGc() throws RemoteException {
        System.gc();
    }

    public String[] getZones() throws RemoteException {
        return ZoneHelper.getSystemZones();
    }

    public String getName() {
        return puDetails.getPresentationName();
    }

    public void process(InternalDump dump) throws InternalDumpProcessorFailedException {
        ClassLoader prevClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoaderHelper.setContextClassLoader(contextClassLoader, true);
        try {
            String prefix = "processing-units/" + clusterInfo.getName() + "/" + clusterInfo.getInstanceId();
            if (clusterInfo.getBackupId() != null) {
                prefix += "_" + clusterInfo.getBackupId();
            }
            prefix += "/";
            dump.addPrefix(prefix);
            try {
                String springXML = (String) context.getInitParameter("pu");
                if (springXML != null) {
                    PrintWriter writer = new PrintWriter(dump.createFileWriter("pu.xml"));
                    writer.print(springXML);
                    writer.close();
                }
                synchronized (dumpProcessors) {
                    for (InternalDumpProcessor dumpProcessor : dumpProcessors) {
                        try {
                            dumpProcessor.process(dump);
                        } catch (Exception e) {
                            logger.warn("Failed to generate dump for [" + dumpProcessor + "]", e);
                        }
                    }
                }
            } finally {
                dump.removePrefix();
            }
        } finally {
            ClassLoaderHelper.setContextClassLoader(prevClassLoader, true);
        }
    }

    public Object invoke(String serviceBeanName, Map<String, Object> namedArgs) throws RemoteException {
        InvocableService invocableService = invocableServiceMap.get(serviceBeanName);
        if (invocableService == null) {
            throw new InvocableServiceLookupFailureException("Could not find an InvocableService with name " + serviceBeanName);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("invoking service " + serviceBeanName + " with args " + namedArgs);
        }
        return invocableService.invoke(namedArgs);
    }
    
    public boolean isStopping() {
        return stopping;
    }
}
