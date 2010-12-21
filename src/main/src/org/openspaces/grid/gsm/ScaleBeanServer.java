package org.openspaces.grid.gsm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.bean.BeanConfigException;
import org.openspaces.admin.bean.BeanConfigPropertiesManager;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.internal.pu.elastic.MachineProvisioningBeanPropertiesManager;
import org.openspaces.admin.internal.pu.elastic.ScaleStrategyBeanPropertiesManager;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.topology.ElasticDeploymentTopology;
import org.openspaces.core.bean.Bean;
import org.openspaces.core.bean.BeanServer;
import org.openspaces.core.bean.DefaultBeanServer;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcement;
import org.openspaces.grid.gsm.machines.ElasticMachineProvisioning;
import org.openspaces.grid.gsm.machines.MachinesSlaEnforcement;
import org.openspaces.grid.gsm.machines.NonBlockingElasticMachineProvisioning;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcement;

public class ScaleBeanServer {

    private final BeanServer<Bean> beanServer;
    private final ProcessingUnit pu ;
    private final RebalancingSlaEnforcement rebalancingSlaEnforcement;
    private final MachinesSlaEnforcement machinesSlaEnforcement;
    private final ContainersSlaEnforcement containersSlaEnforcement;

    public ScaleBeanServer(
            ProcessingUnit pu,
            RebalancingSlaEnforcement rebalancingSlaEnforcement, 
            ContainersSlaEnforcement containersSlaEnforcement,
            MachinesSlaEnforcement machinesSlaEnforcement,
            Map<String,String> elasticProperties) {

        this.pu = pu;
        this.rebalancingSlaEnforcement = rebalancingSlaEnforcement;
        this.containersSlaEnforcement = containersSlaEnforcement;
        this.machinesSlaEnforcement = machinesSlaEnforcement;
        this.beanServer = new DefaultBeanServer<Bean>(
                
                new ScaleBeanFactory(
                        pu, 
                        rebalancingSlaEnforcement.createEndpoint(pu), 
                        containersSlaEnforcement.createEndpoint(pu),
                        machinesSlaEnforcement.createEndpoint(pu)));
        
        // order of beans is important due to implicit dependency inject order
        setGridServiceContainerConfig(elasticProperties);
        
        MachineProvisioningBeanPropertiesManager machineProvisioningPropertiesManager = 
                new MachineProvisioningBeanPropertiesManager(elasticProperties);
        String enabledMachineProvisioningClassName = getEnabledBeanClassName(machineProvisioningPropertiesManager);
        if (enabledMachineProvisioningClassName != null) {
            beanServer.putConfig(
                    enabledMachineProvisioningClassName, 
                    machineProvisioningPropertiesManager.getConfig(enabledMachineProvisioningClassName));
            beanServer.enableBean(enabledMachineProvisioningClassName);
        }
        
        ScaleStrategyBeanPropertiesManager scaleStrategyPropertiesManager = 
            new ScaleStrategyBeanPropertiesManager(elasticProperties);
        String enabledScaleStrategyClassName = getEnabledBeanClassName(scaleStrategyPropertiesManager);
        if (enabledScaleStrategyClassName != null) {
            beanServer.putConfig(
                    enabledScaleStrategyClassName, 
                    scaleStrategyPropertiesManager.getConfig(enabledScaleStrategyClassName));
            beanServer.enableBean(enabledScaleStrategyClassName);
        }
    }

    public void destroy() {
        this.beanServer.destroy();
        this.rebalancingSlaEnforcement.destroyEndpoint(pu);
        this.containersSlaEnforcement.destroyEndpoint(pu);
        this.machinesSlaEnforcement.destroyEndpoint(pu);
    }

    public void setElasticProperties(Map<String,String> elasticProperties) throws BeanConfigException {
        // order of beans is important due to implicit dependency inject order
        // see ScaleBeanFactory
        setGridServiceContainerConfig(elasticProperties);
        setElasticMachineProvisioning(elasticProperties);
        setElasticScaleStrategy(elasticProperties);
    }

    private void setGridServiceContainerConfig(Map<String, String> elasticProperties) {
        String beanClassName = GridServiceContainerConfigBean.class.getName();
        beanServer.putConfig(beanClassName, elasticProperties);
        beanServer.enableBean(beanClassName);
    }

    private void setElasticScaleStrategy(Map<String, String> elasticProperties) {
        
        ScaleStrategyBeanPropertiesManager scaleStrategyBeanPropertiesManager = new ScaleStrategyBeanPropertiesManager(elasticProperties);
        String enabledBeanClassName = getEnabledBeanClassName(scaleStrategyBeanPropertiesManager);
        
        //replace scale strategy bean if necessary
        if (enabledBeanClassName == null) {
            beanServer.disableAllBeansAssignableTo(ElasticDeploymentTopology.class);
        }
        else {

            Map<String, String> beanProperties = new HashMap<String,String>(scaleStrategyBeanPropertiesManager.getConfig(enabledBeanClassName));
            beanServer.replaceBeanAssignableTo(
                    new Class[]{ElasticDeploymentTopology.class}, 
                    enabledBeanClassName,
                    beanProperties); 
        }
    }
    
    private void setElasticMachineProvisioning(Map<String, String> elasticProperties) {
        
        final MachineProvisioningBeanPropertiesManager propertiesManager = 
            new MachineProvisioningBeanPropertiesManager(elasticProperties);
        
        String enabledBeanClassName = getEnabledBeanClassName(propertiesManager);

        Bean[] existingEnabledBeans = beanServer.getEnabledBeanAssignableTo(
                new Class[]{
                        ElasticMachineProvisioning.class,
                        NonBlockingElasticMachineProvisioning.class});

        if (enabledBeanClassName == null && existingEnabledBeans.length != 0) {
            throw new BeanConfigurationException("Cannot disable " + existingEnabledBeans[0].getClass() + " at runtime.");
        }
        
        if (enabledBeanClassName != null && existingEnabledBeans.length == 0) {
            throw new BeanConfigurationException("Cannot enable " + enabledBeanClassName + " at runtime.");
        }
        
        // replace machine provisioning bean if possible    
        if (enabledBeanClassName != null && existingEnabledBeans.length != 0) {
                       
            Map<String, String> beanProperties = propertiesManager.getConfig(enabledBeanClassName);
            beanServer.replaceBeanAssignableTo(
                    new Class[]{ElasticMachineProvisioning.class,
                                NonBlockingElasticMachineProvisioning.class}, 
                    enabledBeanClassName,
                    beanProperties);
        }
    }
    
    private String getEnabledBeanClassName(BeanConfigPropertiesManager propertiesManager) {
        final String[] enabledBeansClassNames = propertiesManager.getEnabledBeansClassNames();
        
        if (enabledBeansClassNames.length > 1) {
            throw new BeanConfigurationException("At most one of the following beans can be enabled: " + Arrays.toString(enabledBeansClassNames));
        }
        String enabledBeanClassName = null; 
        if (enabledBeansClassNames.length == 1) {
            enabledBeanClassName = enabledBeansClassNames[0];
        }
        return enabledBeanClassName;
    }

}
