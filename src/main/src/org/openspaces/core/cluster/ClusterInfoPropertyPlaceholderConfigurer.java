package org.openspaces.core.cluster;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionVisitor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.HashSet;
import java.util.Properties;

/**
 * Provides the {@link org.openspaces.core.cluster.ClusterInfo ClusterInfo} parameters as injected
 * proprties that can be used within Spring application context. The following is a list of the
 * properties:
 * <ul>
 * <li>${clusterInfo.numberOfInstances} : Maps to {@link ClusterInfo#getNumberOfInstances()}</li>
 * <li>${clusterInfo.numberOfBackups} : Maps to {@link ClusterInfo#getNumberOfBackups()}</li>
 * <li>${clusterInfo.instanceId} : Maps to {@link ClusterInfo#getInstanceId()}</li>
 * <li>${clusterInfo.backupId} : Maps to {@link ClusterInfo#getBackupId()}</li>
 * <li>${clusterInfo.schema} : Maps to {@link ClusterInfo#getSchema()}</li>
 * </ul>
 *
 * <p>If the cluster info parameter is not set (has <code>null</code> value) an empty string will be used.
 *
 * @author kimchy
 */
public class ClusterInfoPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements BeanNameAware,
        BeanFactoryAware {

    public static final String NUMBER_OF_INSTANCES_PROP = "clusterInfo.numberOfInstances";
    public static final String NUMBER_OF_BACKUPS_PROP = "clusterInfo.numberOfBackups";
    public static final String INSTANCE_ID_PROP = "clusterInfo.instanceId";
    public static final String BACKUP_ID_PROP = "clusterInfo.backupId";
    public static final String SCHEMA_PROP = "clusterInfo.schema";

    private Properties properties;

    public ClusterInfoPropertyPlaceholderConfigurer(ClusterInfo clusterInfo) {
        properties = new Properties();
        properties.setProperty(NUMBER_OF_INSTANCES_PROP, toPropertyValue(clusterInfo.getNumberOfInstances()));
        properties.setProperty(NUMBER_OF_BACKUPS_PROP, toPropertyValue(clusterInfo.getNumberOfBackups()));
        properties.setProperty(INSTANCE_ID_PROP, toPropertyValue(clusterInfo.getInstanceId().toString()));
        properties.setProperty(BACKUP_ID_PROP, toPropertyValue(clusterInfo.getBackupId().toString()));
        properties.setProperty(SCHEMA_PROP, toPropertyValue(clusterInfo.getSchema()));
        setIgnoreUnresolvablePlaceholders(true);
        setSystemPropertiesMode(SYSTEM_PROPERTIES_MODE_NEVER);
        setOrder(2);
    }

    private String beanName;

    private BeanFactory beanFactory;

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props)
            throws BeansException {

        String[] beanNames = beanFactoryToProcess.getBeanDefinitionNames();
        for (String beanName1 : beanNames) {
            // Check that we're not parsing our own bean definition,
            // to avoid failing on unresolvable placeholders in properties file locations.
            if (!(beanName1.equals(this.beanName) && beanFactoryToProcess.equals(this.beanFactory))) {
                BeanDefinitionVisitor visitor = new PlaceholderResolvingBeanDefinitionVisitor(properties);
                BeanDefinition bd = beanFactoryToProcess.getBeanDefinition(beanName1);
                try {
                    visitor.visitBeanDefinition(bd);
                } catch (BeanDefinitionStoreException ex) {
                    throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName1, ex.getMessage());
                }
            }
        }
    }

    private String toPropertyValue(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    /**
     * BeanDefinitionVisitor that resolves placeholders in String values, delegating to the
     * <code>parseStringValue</code> method of the containing class.
     */
    private class PlaceholderResolvingBeanDefinitionVisitor extends BeanDefinitionVisitor {

        private final Properties props;

        public PlaceholderResolvingBeanDefinitionVisitor(Properties props) {
            this.props = props;
        }

        protected String resolveStringValue(String strVal) throws BeansException {
            return parseStringValue(strVal, this.props, new HashSet<Object>());
        }
    }
}