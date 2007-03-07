package org.openspaces.core.config;

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
 * <p>An extension on top of Spring {@link org.springframework.beans.factory.config.PropertyPlaceholderConfigurer} that
 * works with {@link org.openspaces.core.config.BeanLevelProperties} in order to inject bean level propeties.
 *
 * <p>${..} notations are used to lookup bean level properties with the properites obtained based on the bean name
 * using {@link org.openspaces.core.config.BeanLevelProperties#getMergedBeanProperties(String)}.
 *
 * @author kimchy
 */
public class BeanLevelPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements BeanNameAware, BeanFactoryAware {

    private BeanLevelProperties beanLevelProperties;

    public BeanLevelPropertyPlaceholderConfigurer(BeanLevelProperties beanLevelProperties) {
        this.beanLevelProperties = beanLevelProperties;
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
                BeanDefinitionVisitor visitor = new PlaceholderResolvingBeanDefinitionVisitor(beanLevelProperties.getMergedBeanProperties(beanName1));
                BeanDefinition bd = beanFactoryToProcess.getBeanDefinition(beanName1);
                try {
                    visitor.visitBeanDefinition(bd);
                }
                catch (BeanDefinitionStoreException ex) {
                    throw new BeanDefinitionStoreException(bd.getResourceDescription(), beanName1, ex.getMessage());
                }
            }
        }
    }

    /**
     * BeanDefinitionVisitor that resolves placeholders in String values,
     * delegating to the <code>parseStringValue</code> method of the
     * containing class.
     */
    private class PlaceholderResolvingBeanDefinitionVisitor extends BeanDefinitionVisitor {

        private final Properties props;

        public PlaceholderResolvingBeanDefinitionVisitor(Properties props) {
            this.props = props;
        }

        protected String resolveStringValue(String strVal) throws BeansException {
            return parseStringValue(strVal, this.props, new HashSet());
        }
    }
}
