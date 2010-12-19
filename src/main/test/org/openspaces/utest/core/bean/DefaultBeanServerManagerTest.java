package org.openspaces.utest.core.bean;

import java.util.HashMap;

import junit.framework.TestCase;

import org.openspaces.admin.bean.BeanConfigAlreadyExistsException;
import org.openspaces.admin.bean.BeanConfigNotFoundException;
import org.openspaces.admin.bean.EnabledBeanConfigCannotBeChangedException;
import org.openspaces.admin.internal.alerts.bean.AlertBean;
import org.openspaces.core.bean.DefaultBeanServer;
import org.openspaces.core.util.StringProperties;
import org.openspaces.utest.admin.internal.admin.NullMockAdmin;

/**
 * @author Moran Avigdor
 */
public class DefaultBeanServerManagerTest extends TestCase {
    
    public void testAddBean() {
        DefaultBeanServer<AlertBean> manager = new DefaultBeanServer<AlertBean>(new NullMockAdmin());
        manager.addConfig("myBean", new HashMap<String, String>());
        try {
            manager.addConfig("myBean", new HashMap<String, String>());
            fail("Should have thrown BeanAlreadyExistsException");
        } catch (BeanConfigAlreadyExistsException e) {
        }
    }
    
    public void testSetBean() {
        DefaultBeanServer<AlertBean> manager = new DefaultBeanServer<AlertBean>(new NullMockAdmin());
        try {
            manager.setConfig("myBean", new HashMap<String, String>());
            fail("Should have thrown BeanNotFoundException");
        } catch (BeanConfigNotFoundException e) {
            
        }
        
        manager.addConfig("myBean", new StringProperties().put("key", "value").getProperties());
        manager.setConfig("myBean", new StringProperties(manager.getConfig("myBean")).put("secondKey", "secondValue").getProperties());

        assertEquals("Expected myBean to exist in manager", 1, manager.getBeansClassNames().length);
        assertEquals("Expected myBean properties to exist in manager", 2, manager.getConfig("myBean").size());
    }
    
    public void testRemoveBean() {
        DefaultBeanServer<AlertBean> manager = new DefaultBeanServer<AlertBean>(new NullMockAdmin());
        try {
            manager.removeConfig("myBean");
            fail("Should have thrown BeanNotFoundException");
        } catch (BeanConfigNotFoundException e) {
            
        }
        
        manager.addConfig("myBean", new StringProperties().put("key", "value").getProperties());
        manager.setConfig("myBean", new StringProperties(manager.getConfig("myBean")).put("secondKey", "secondValue").getProperties());

        assertEquals("Expected myBean to exist in manager", 1, manager.getBeansClassNames().length);
        assertEquals("Expected myBean properties to exist in manager", 2, manager.getConfig("myBean").size());
        
        manager.removeConfig("myBean");
        assertEquals("Expected myBean to not exist in manager", 0, manager.getBeansClassNames().length);
        try {
            manager.getConfig("myBean");
            fail("Should have thrown BeanNotFoundException");
        }catch(BeanConfigNotFoundException e) {
        }
    }
    
    public void testEnableBean() {
        DefaultBeanServer<AlertBean> manager = new DefaultBeanServer<AlertBean>(new NullMockAdmin());
        try {
            manager.enableBean("myBean");
            fail("Should have thrown BeanNotFoundException");
        } catch (BeanConfigNotFoundException e) {
        }
        
        String beanClassName = SimpleBean.class.getName();
        manager.addConfig(beanClassName, new StringProperties().put("key", "value").getProperties());
        assertEquals("Expected zero enabled beans", 0, manager.getEnabledBeansClassNames().length);
        
        manager.enableBean(beanClassName);
        assertEquals("Expected one enabled bean", 1, manager.getEnabledBeansClassNames().length);
        assertEquals("Expected one managed bean", 1, manager.getBeansClassNames().length);
        assertEquals("Expected bean properties to exist in manager", 1, manager.getConfig(beanClassName).size());

        manager.disableBean(beanClassName);
        manager.setConfig(beanClassName, new StringProperties().put("new-key", "new-value").getProperties());
        manager.enableBean(beanClassName);
        assertEquals("Expected one enabled bean", 1, manager.getEnabledBeansClassNames().length);
        assertEquals("Expected one managed bean", 1, manager.getBeansClassNames().length);
        assertEquals("Expected bean properties to exist in manager", 1, manager.getConfig(beanClassName).size());
        assertEquals("Expected bean properties to be updated", "new-value", manager.getConfig(beanClassName).get("new-key"));
        
        manager.addConfig("myBean", new StringProperties().put("key", "value").getProperties());
        assertEquals("Expected one enabled bean", 1, manager.getEnabledBeansClassNames().length);
        assertEquals("Expected two managed beans", 2, manager.getBeansClassNames().length);
    }
    
    public void testDisableBean() {
        DefaultBeanServer<AlertBean> manager = new DefaultBeanServer<AlertBean>(new NullMockAdmin());
        try {
            manager.disableBean("myBean");
            fail("Should have thrown BeanNotFoundException");
        } catch (BeanConfigNotFoundException e) {
        }
        
        manager.addConfig(SimpleBean.class.getName(), new StringProperties().put("key", "value").getProperties());
        assertEquals("Expected zero enabled beans", 0, manager.getEnabledBeansClassNames().length);
        
        manager.enableBean(SimpleBean.class.getName());
        assertEquals("Expected one enabled bean", 1, manager.getEnabledBeansClassNames().length);
        assertEquals("Expected one managed bean", 1, manager.getBeansClassNames().length);
        
        manager.addConfig("myBean", new StringProperties().put("key", "value").getProperties());
        assertEquals("Expected one enabled bean", 1, manager.getEnabledBeansClassNames().length);
        assertEquals("Expected two managed beans", 2, manager.getBeansClassNames().length);
        
        manager.disableBean(SimpleBean.class.getName());
        assertEquals("Expected zero enabled beans", 0, manager.getEnabledBeansClassNames().length);
        assertEquals("Expected two managed beans", 2, manager.getBeansClassNames().length);
        
        manager.removeConfig(SimpleBean.class.getName());
        assertEquals("Expected one managed beans", 1, manager.getBeansClassNames().length);
        assertNotNull(manager.getConfig("myBean"));
        assertEquals("Expected myBean properties to exist in manager", 1, manager.getConfig("myBean").size());
    }
    
    public void testRemoveEnabledThenDisabledBean() {
        DefaultBeanServer<AlertBean> manager = new DefaultBeanServer<AlertBean>(new NullMockAdmin());
        
        String beanClassName = SimpleBean.class.getName();
        manager.addConfig(beanClassName, new StringProperties().put("key", "value").getProperties());
        manager.enableBean(beanClassName);
        manager.disableBean(beanClassName);
        manager.removeConfig(beanClassName);
        assertEquals("Expected zero enabled beans", 0, manager.getEnabledBeansClassNames().length);
        assertEquals("Expected zero managed beans", 0, manager.getBeansClassNames().length);
    }
    
    public void testUnableToRemoveEnabledBean() {
        DefaultBeanServer<AlertBean> manager = new DefaultBeanServer<AlertBean>(new NullMockAdmin());
        
        String beanClassName = SimpleBean.class.getName();
        manager.addConfig(beanClassName, new StringProperties().put("key", "value").getProperties());
        manager.enableBean(beanClassName);
        try { 
            manager.removeConfig(beanClassName);
            fail("must not allow removal of enabled beans");
        }
        catch (EnabledBeanConfigCannotBeChangedException e) {
            //expected exception
        }    
    }
    
    public void testDisableAllBeans() {
        DefaultBeanServer<AlertBean> manager = new DefaultBeanServer<AlertBean>(new NullMockAdmin());
        manager.addConfig("myBean-1", new StringProperties().getProperties());
        manager.addConfig(SimpleBean.class.getName(), new StringProperties().put("key", "value").getProperties());
        manager.addConfig("myBean-2", new StringProperties().getProperties());
        manager.enableBean(SimpleBean.class.getName());
        
        assertEquals("Expected three managed beans", 3, manager.getBeansClassNames().length);
        assertEquals("Expected one enabled beans", 1, manager.getEnabledBeansClassNames().length);
        
        manager.disableAllBeans();
        
        assertEquals("Expected three managed beans", 3, manager.getBeansClassNames().length);
        assertEquals("Expected zero enabled beans", 0, manager.getEnabledBeansClassNames().length);
    }
}
