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

package org.openspaces.pu.container.servicegrid.deploy;

import com.gigaspaces.grid.gsm.GSM;
import com.gigaspaces.logger.GSLogConfigLoader;
import com.j_spaces.core.Constants;
import com.j_spaces.kernel.SecurityPolicyLoader;
import net.jini.core.lookup.ServiceItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jini.rio.core.ClassBundle;
import org.jini.rio.core.OperationalString;
import org.jini.rio.core.ServiceElement;
import org.jini.rio.core.ServiceLevelAgreements;
import org.jini.rio.core.ServiceProvisionListener;
import org.jini.rio.core.ThresholdValues;
import org.jini.rio.monitor.DeployAdmin;
import org.jini.rio.opstring.OpString;
import org.jini.rio.opstring.OpStringLoader;
import org.jini.rio.resources.servicecore.ServiceAdmin;
import org.openspaces.core.cluster.ClusterInfo;
import org.openspaces.core.properties.BeanLevelProperties;
import org.openspaces.pu.container.support.BeanLevelPropertiesParser;
import org.openspaces.pu.container.support.ClusterInfoParser;
import org.openspaces.pu.container.support.CommandLineParser;
import org.openspaces.pu.sla.InstanceSLA;
import org.openspaces.pu.sla.Policy;
import org.openspaces.pu.sla.RelocationPolicy;
import org.openspaces.pu.sla.SLA;
import org.openspaces.pu.sla.ScaleUpPolicy;
import org.openspaces.pu.sla.requirement.HostRequirement;
import org.openspaces.pu.sla.requirement.RangeRequirement;
import org.openspaces.pu.sla.requirement.Requirement;
import org.openspaces.pu.sla.requirement.SystemRequirement;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 */
public class Deploy {

    private static final Log logger = LogFactory.getLog(Deploy.class);

    private DeployAdmin deployAdmin;

    private String[] groups;

    private int lookupTimeout = 5000;

    public DeployAdmin getDeployAdmin() throws GSMNotFoundException {
        if (deployAdmin == null) {
            GSM gsm = null;
            ServiceItem result = ServiceFinder.find(null, GSM.class, lookupTimeout, getGroups());
            if (result != null) {
                try {
                    result = (ServiceItem) new MarshalledObject(result).get();
                    gsm = (GSM) result.service;
                } catch (Exception e) {
                    throw new GSMNotFoundException(getGroups(), lookupTimeout, e);
                }
            }
            if (gsm == null) {
                throw new GSMNotFoundException(getGroups(), lookupTimeout);
            }
            try {
                deployAdmin = (DeployAdmin) gsm.getAdmin();
            } catch (RemoteException e) {
                throw new GSMNotFoundException(getGroups(), lookupTimeout);
            }
        }

        return deployAdmin;
    }

    public void setDeployAdmin(DeployAdmin deployAdmin) {
        this.deployAdmin = deployAdmin;
    }

    public String[] getGroups() {
        if (groups == null) {
            String groupsProperty = java.lang.System.getProperty("com.gs.jini_lus.groups");
            if (groupsProperty != null) {
                StringTokenizer tokenizer = new StringTokenizer(groupsProperty);
                int count = tokenizer.countTokens();
                groups = new String[count];
                for (int i = 0; i < count; i++) {
                    groups[i] = tokenizer.nextToken();
                }
            } else {
                groups = new String[]{Constants.LookupManager.LOOKUP_GROUP_DEFAULT};
            }
        }
        return groups;
    }

    public void setGroups(String[] groups) {
        this.groups = groups;
    }

    public void setLookupTimeout(int lookupTimeout) {
        this.lookupTimeout = lookupTimeout;
    }

    public void deploy(String[] args) throws Exception {
        deploy(args, null);
    }

    public void deploy(String[] args, ServiceProvisionListener listener) throws Exception {
        OperationalString opString = buildOperationalString(args);
        /* Map result = */
        if (listener != null) {
            deployAdmin.deploy(opString, listener);
        } else {
            deployAdmin.deploy(opString);
        }
    }

    public OperationalString buildOperationalString(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("The pu name must be defined");
        }
        String puPath = args[args.length - 1];
        int index = puPath.lastIndexOf('/');
        index = index == -1 ? 0 : index;
        String puName = puPath.substring(index);
        // override pu name allows to change the actual pu name deployed from the on under deploy directory
        String overridePuName = puName;

        CommandLineParser.Parameter[] params = CommandLineParser.parse(args, args.length - 1);

        // check if we have a groups parameter and timeout parameter
        for (CommandLineParser.Parameter param : params) {
            if (param.getName().equalsIgnoreCase("groups")) {
                setGroups(param.getArguments());
            }
            if (param.getName().equalsIgnoreCase("timeout")) {
                setLookupTimeout(Integer.valueOf(param.getArguments()[0]));
            }
            if (param.getName().equalsIgnoreCase("override-name")) {
                overridePuName = param.getArguments()[0];
            }
        }

        String[] groups = getGroups();
        if (logger.isInfoEnabled()) {
            if (groups != null) {
                logger.info("Deploying [" + puName + "] with name [" + overridePuName + "] and groups " + Arrays.asList(groups));
            } else {
                logger.info("Deploying [" + puName + "] with name [" + overridePuName + "] and default groups");
            }
        }
        //get codebase from service
        String codeserver = getCodebase(getDeployAdmin());
        if (logger.isDebugEnabled()) {
            logger.debug("Usign codeserver [" + codeserver + "]");
        }

        //list remote files, only works with webster
        URL root = new URL(codeserver);
        HTTPFileSystemView view = new HTTPFileSystemView(root);
        File puHome = view.createFileObject(puPath);

        //get list of all jars
        File lib = view.createFileObject(puHome, "lib");
        File[] jars = view.getFiles(lib, false);
        if (logger.isDebugEnabled()) {
            logger.debug("Using lib " + Arrays.asList(jars));
        }

        //get list of all shared
        File shared = view.createFileObject(puHome, "shared-lib");
        File[] sharedJars = view.getFiles(shared, false);
        if (logger.isDebugEnabled()) {
            logger.debug("Using shared-lib " + Arrays.asList(sharedJars));
        }

        //read pu xml
        String puString = readPUFile(root, puPath);
        if (logger.isDebugEnabled()) {
            logger.debug("Using PU xml [" + puString + "]");
        }

        // just call this to validate the xml before we deploy it
        Resource resource = new ByteArrayResource(puString.getBytes());
        new XmlBeanFactory(resource);

        // check to see if sla was passed as a parameter
        String slaString = puString;
        for (CommandLineParser.Parameter param : params) {
            if (param.getName().equalsIgnoreCase("sla")) {
                String slaLocation = param.getArguments()[0];
                if (logger.isInfoEnabled()) {
                    logger.info("Loading SLA from [" + slaLocation + "]");
                }
                resource = new DefaultResourceLoader() {
                    // override the default load from the classpath to load from the file system
                    protected Resource getResourceByPath(String path) {
                        return new FileSystemResource(path);
                    }
                }.getResource(slaLocation);
                InputStreamReader reader = new InputStreamReader(resource.getInputStream());
                slaString = FileCopyUtils.copyToString(reader);
                reader.close();
            }
        }

        //get sla from pu string
        resource = new ByteArrayResource(slaString.getBytes());
        XmlBeanFactory xmlBeanFactory = new XmlBeanFactory(resource);
        SLA sla;
        try {
            sla = (SLA) xmlBeanFactory.getBean("SLA");
        } catch (NoSuchBeanDefinitionException e) {
            logger.info("SLA Not Found in PU.  Using Default SLA.");
            sla = new SLA();
        }

        ClusterInfo clusterInfo = ClusterInfoParser.parse(params);
        if (clusterInfo != null) {
            // override specific cluster info parameters on the SLA
            if (clusterInfo.getSchema() != null) {
                if (logger.isInfoEnabled()) {
                    logger.info("Overrding SLA cluster schema with [" + clusterInfo.getSchema() + "]");
                }
                sla.setClusterSchema(clusterInfo.getSchema());
            }
            if (clusterInfo.getNumberOfInstances() != null) {
                if (logger.isInfoEnabled()) {
                    logger.info("Overrding SLA numberOfInstances with [" + clusterInfo.getNumberOfInstances() + "]");
                }
                sla.setNumberOfInstances(clusterInfo.getNumberOfInstances());
                if (logger.isInfoEnabled()) {
                    logger.info("Overrding SLA numberOfBackups with [" + clusterInfo.getNumberOfBackups() + "]");
                }
                if (clusterInfo.getNumberOfBackups() == null) {
                    sla.setNumberOfBackups(0);
                } else {
                    sla.setNumberOfBackups(clusterInfo.getNumberOfBackups());
                }
            }
        }

        for (CommandLineParser.Parameter param : params) {
            if (param.getName().equalsIgnoreCase("max-instances-per-vm")) {
                String maxInstancePerVm = param.getArguments()[0];
                sla.setMaxInstancesPerVM(Integer.valueOf(maxInstancePerVm));
                if (logger.isInfoEnabled()) {
                    logger.info("Overrding SLA maxInstancesPerVM with [" + maxInstancePerVm + "]");
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Using SLA " + sla);
        }

        //deploy to sg
        return loadDeployment(puString, codeserver, sla, jars, puPath, overridePuName, sharedJars, BeanLevelPropertiesParser.parse(params));
    }

    //copied from opstringloader
    private String[] getSLAConfigArgs(String type, String max, long lowerDampener, long upperDampener) {
        String[] args;
        String handler = "org.jini.rio.qos.";
        int handlerType;
        if (type.equals("scaling")) {
            handler = handler + "ScalingPolicyHandler";
            handlerType = 1;
        } else if (type.equals("relocation")) {
            handler = handler + "RelocationPolicyHandler";
            handlerType = 2;
        } else {
            handler = handler + "SLAPolicyHandler";
            handlerType = 3;
        }

        String slaPolicyHandler =
                "org.jini.rio.qos.SLAPolicyHandler.slaPolicyHandler";
        switch (handlerType) {
            case 1:
                args = new String[5];
                args[0] = "-";
                args[1] = slaPolicyHandler + "=" + "new " + handler + "((org.jini.rio.core.SLA)$data)";
                args[2] = handler + ".MaxServices=" + max;
                args[3] = handler + ".LowerThresholdDampeningFactor=" + lowerDampener;
                args[4] = handler + ".UpperThresholdDampeningFactor=" + upperDampener;
                break;
            case 2:
                args = new String[4];
                args[0] = "-";
                args[1] = slaPolicyHandler + "=" + "new " + handler + "((org.jini.rio.core.SLA)$data)";
                args[2] = handler + ".LowerThresholdDampeningFactor=" + lowerDampener;
                args[3] = handler + ".UpperThresholdDampeningFactor=" + upperDampener;
                break;
            default:
                args = new String[2];
                args[0] = "-";
                args[1] = slaPolicyHandler + "=" +
                        "new " + handler + "((org.jini.rio.core.SLA)$data)";
        }
        return (args);
    }

    private static String readPUFile(URL root, String puPath) throws IOException {
        URL puURL = new URL(root, puPath + "/META-INF/spring/pu.xml");
        BufferedReader reader = new BufferedReader(new InputStreamReader(puURL.openStream()));

        StringBuffer buffer = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
        return buffer.toString();
    }

    private String getCodebase(DeployAdmin deployAdmin) throws MalformedURLException, RemoteException {
        URL url = ((ServiceAdmin) deployAdmin).getServiceElement().getExportURLs()[0];
        return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort() + "/";
    }

    private OperationalString loadDeployment(String puString, String codeserver, SLA sla, File[] jars, String puPath,
                                             String puName, File[] sharedJars,
                                             BeanLevelProperties beanLevelProperties) throws Exception {
        URL opstringURL = Deploy.class.getResource("/org/openspaces/pu/container/servicegrid/puservicebean.xml");
        OperationalString opString;

        //load the servicebean opstring
        OpStringLoader opStringLoader = new OpStringLoader();
        opStringLoader.setDefaultGroups(getGroups());
        opStringLoader.setCodebaseOverride(codeserver);
        opString = opStringLoader.parseOperationalString(opstringURL)[0];
        ((OpString) opString).setName(puName);

        //this opstring should only have one servicebean
        ServiceElement[] serviceElements = opString.getServices();
        ServiceElement element = serviceElements[0];

        //put the entire pu spring xml as parameter to servicebean
        element.getServiceBeanConfig().addInitParameter("pu", puString);

        if (beanLevelProperties != null) {
            element.getServiceBeanConfig().addInitParameter("beanLevelProperties", new MarshalledObject(beanLevelProperties));
        }

        //sla
        Policy policy = sla.getPolicy();
        if (policy != null) {
            String type;
            if (policy instanceof ScaleUpPolicy) {
                type = "scaling";
            } else if (policy instanceof RelocationPolicy) {
                type = "relocation";
            } else {
                throw new IllegalArgumentException("Unknown SLA Policy:" + policy);
            }

            String max = String.valueOf(sla.getNumberOfInstances());
            //todo: make sure max is greater then num of instances
            if (policy instanceof ScaleUpPolicy) {
                max = String.valueOf(((ScaleUpPolicy) policy).getMaxInstances());
            }
            String[] configParms = getSLAConfigArgs(type, max, policy.getLowerDampener(), policy.getUpperDampener());
            org.jini.rio.core.SLA slaElement = new org.jini.rio.core.SLA(
                    policy.getMonitor(),
                    new double[]{policy.getLow(), policy.getHigh()},
                    configParms,
                    null);
            element.getServiceLevelAgreements().addServiceSLA(slaElement);
        }

        //requirements
        applyRequirements(element, sla.getRequirements());

        if (sla.getMaxInstancesPerVM() > 0) {
            element.setMaxPerMachine(sla.getMaxInstancesPerVM());
        }

        //put jars
        ClassBundle classBundle = element.getComponentBundle();
        classBundle.addJAR(puPath + "/");
        for (File jar : jars) {
            String path = jar.getPath().replace('\\', '/');
            classBundle.addJAR(path);
        }

        //shared-lib as sharedComponents
        String[] sharedJarPaths = new String[sharedJars.length];
        for (int i = 0; i < sharedJars.length; i++) {
            File sharedJar = sharedJars[i];
            String path = sharedJar.getPath().replace('\\', '/');
            sharedJarPaths[i] = path;
        }
        Map<String, String[]> jarsMap = new HashMap<String, String[]>();
        jarsMap.put("hack", sharedJarPaths);
        classBundle.addSharedComponents(jarsMap);

        // set the each servive to have the operation string name
        element.getServiceBeanConfig().setName(element.getOperationalStringName().replace(' ', '-') + "." + element.getName());

        // pass the SLA as an init parameter so the GSC won't need to parse the XML again
        element.getServiceBeanConfig().addInitParameter("sla", new MarshalledObject(sla));

        //this is the MOST IMPORTANT part
        if (sla.getInstanceSLAs() != null && sla.getInstanceSLAs().size() > 0) {
            element.setPlanned(1);
            String name = element.getName();
            opString.removeService(element);
            for (int instanceId = 1; instanceId <= sla.getNumberOfInstances(); instanceId++) {
                ServiceElement clone = deepCopy(element);
                clone.getServiceBeanConfig().setName(name + "." + instanceId);
                clone.getServiceBeanConfig().addInitParameter("clusterGroup", String.valueOf(instanceId));
                clone.getServiceBeanConfig().addInitParameter("instanceId", String.valueOf(instanceId));
                InstanceSLA instanceSLA = findInstanceSLA(instanceId, null, sla.getInstanceSLAs());
                if (instanceSLA != null) {
                    applyRequirements(clone, instanceSLA.getRequirements());
                }
                opString.addService(clone);
                if (logger.isTraceEnabled()) {
                    logger.trace("Using Service Element " + element.toString());
                }
                for (int backupId = 1; backupId <= sla.getNumberOfBackups(); backupId++) {
                    clone = deepCopy(element);
                    clone.getServiceBeanConfig().setName(name + "." + instanceId + "_" + backupId);
                    clone.getServiceBeanConfig().addInitParameter("clusterGroup", String.valueOf(instanceId));
                    clone.getServiceBeanConfig().addInitParameter("instanceId", String.valueOf(instanceId));
                    clone.getServiceBeanConfig().addInitParameter("backupId", String.valueOf(backupId));
                    instanceSLA = findInstanceSLA(instanceId, backupId, sla.getInstanceSLAs());
                    if (instanceSLA != null) {
                        applyRequirements(clone, instanceSLA.getRequirements());
                    }
                    opString.addService(clone);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Using Service Element " + element.toString());
                    }
                }
            }
        } else {
            boolean hasBackups = sla.getNumberOfBackups() > 0;
            if (hasBackups) {
                //the extra one is the primary
                element.setPlanned(sla.getNumberOfBackups() + 1);
                String name = element.getName();
                opString.removeService(element);
                for (int i = 1; i <= sla.getNumberOfInstances(); i++) {
                    ServiceElement clone = deepCopy(element);
                    clone.getServiceBeanConfig().setName(name + "." + i);
                    clone.getServiceBeanConfig().addInitParameter("clusterGroup", String.valueOf(i));
                    opString.addService(clone);
                    if (logger.isTraceEnabled()) {
                        logger.trace("Using Service Element " + element.toString());
                    }
                }
            } else {
                element.setPlanned(sla.getNumberOfInstances());
                element.getServiceBeanConfig().addInitParameter("clusterGroup", String.valueOf(1));
                if (logger.isTraceEnabled()) {
                    logger.trace("Using Service Element " + element.toString());
                }
            }
        }

        return (opString);
    }

    private InstanceSLA findInstanceSLA(Integer instanceId, Integer backupId, List<InstanceSLA> instanceSLAs) {
        for (InstanceSLA instanceSLA : instanceSLAs) {
            if (instanceId.equals(instanceSLA.getInstanceId())) {
                if (backupId != null) {
                    if (backupId.equals(instanceSLA.getBackupId())) {
                        return instanceSLA;
                    }
                } else {
                    return instanceSLA;
                }
            }
        }
        return null;
    }

    private void applyRequirements(ServiceElement element, List<Requirement> requirements) {
        if (requirements == null) {
            return;
        }
        List<String> hosts = new ArrayList<String>();
        for (Requirement requirement : requirements) {
            if (requirement instanceof RangeRequirement) {
                RangeRequirement range = (RangeRequirement) requirement;
                ThresholdValues thresholdValues = new ThresholdValues(range.getLow(), range.getHigh());
                element.getServiceLevelAgreements().addSystemThreshold(range.getWatch(), thresholdValues);
            } else if (requirement instanceof HostRequirement) {
                hosts.add(((HostRequirement) requirement).getIp());
            } else if (requirement instanceof SystemRequirement) {
                SystemRequirement systemAttributes = (SystemRequirement) requirement;
                ServiceLevelAgreements.SystemRequirement systemRequirement = new ServiceLevelAgreements.SystemRequirement(
                        systemAttributes.getName(),
                        null,
                        systemAttributes.getAttributes()
                );
                element.getServiceLevelAgreements().addSystemRequirement(systemRequirement);
            }
        }
        //put hosts as cluster
        if (hosts.size() > 0) {
            element.setCluster(hosts.toArray(new String[hosts.size()]));
        }
    }

    private ServiceElement deepCopy(ServiceElement element) throws IOException, ClassNotFoundException {
        //write
        byte[] writtenBytes;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(element);
        writtenBytes = byteArrayOutputStream.toByteArray();

        //read
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(writtenBytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        Object readObject = objectInputStream.readObject();
        return (ServiceElement) readObject;
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println(getUsage());
            java.lang.System.exit(-1);
        }

        if (System.getProperty("java.security.policy") == null) {
            SecurityPolicyLoader.loadPolicy(Constants.System.SYSTEM_GS_POLICY);
        }
        // init GigaSpace logger
        GSLogConfigLoader.getLoader();

        Deploy deployer = new Deploy();
        deployer.deploy(args);
    }

    public static String getUsage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Usage: Deploy [-sla ...] [-cluster ...] [-groups groups] [-timeout timeoutValue] [-properties ...] PU_Name");
        sb.append("\n    PU_Name: The name of the processing unit under the deploy directory");
        sb.append("\n    -sla [sla-location]                      : Location of an optional xml file holding the SLA element");
        sb.append("\n    -cluster [cluster properties]            : Allows to override the cluster parameters of the SLA elements");
        sb.append("\n             schema=partitioned              : The cluster schema to override");
        sb.append("\n             total_members=1,1               : The number of instances and number of backups to override");
        sb.append("\n    -groups [groupName] [groupName] ...      : The lookup groups used to look up the GSM");
        sb.append("\n    -timeout [timeout value]                 : The timeout value of GSM lookup (defaults to 5000) in milliseconds");
        sb.append("\n    -proeprties [properties-loc]             : Location of context level properties");
        sb.append("\n    -proeprties [bean-name] [properties-loc] : Location of properties used applied only for a specified bean");
        sb.append("\n    -override-name [override pu name]        : An override pu name, useful when using pu as a template");
        sb.append("\n    -max-instances-per-vm [number]           : Allows to set the SLA number of instances per VM");
        sb.append("\n");
        sb.append("\n");
        sb.append("\nSome Examples:");
        sb.append("\n1. Deploy data-processor");
        sb.append("\n    - Deploys a processing unit called data-processor");
        sb.append("\n2. Deploy -sla file://config/sla.xml data-processor");
        sb.append("\n    - Deploys a processing unit called data-processor using an SLA element read from sla.xml");
        sb.append("\n3. Deploy -properties file://config/context.properties -properties space1 file://config/space1.properties data-processor");
        sb.append("\n    - Deploys a processing unit called data-processor using context level properties called context.proeprties and bean level properties called space1.properties applied to bean named space1");
        sb.append("\n4. Deploy -properties embed://prop1=value1 -properties space1 embed://prop2=value2;prop3=value3");
        sb.append("\n    - Deploys a processing unit called data-processor using context level properties with a single property called prop1 with value1 and bean level properties with two properties");
        return sb.toString();
    }
}
