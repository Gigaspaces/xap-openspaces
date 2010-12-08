package org.openspaces.utest.core.bean;

import java.util.HashMap;

import org.openspaces.admin.bean.BeanAlreadyExistsException;
import org.openspaces.admin.bean.BeanNotFoundException;
import org.openspaces.admin.internal.alerts.bean.AlertBean;
import org.openspaces.core.bean.DefaultBeanServer;
import org.openspaces.core.util.StringProperties;
import org.openspaces.utest.admin.internal.admin.NullMockAdmin;

import junit.framework.TestCase;

/**
 * @author Moran Avigdor
 */
public class DefaultBeanServerManagerTest extends TestCase {
    
    public void testAddBean() {
        DefaultBeanServer<AlertBean> manager = new DefaultBeanServer<AlertBean>(new NullMockAdmin());
        manager.addBean("myBean", new HashMap<String, String>());
        try {
            manager.addBean("myBean", new HashMap<String, String>());
            fail("Should have thrown BeanAlreadyExistsException");
        } catch (BeanAlreadyExistsException e) {
        }
    }
    
    public void testSetBean() {
        DefaultBeanServer<AlertBean> manager = new DefaultBeanServer<AlertBean>(new NullMockAdmin());
        try {
            manager.setBean("myBean", new HashMap<String, String>());
            fail("Should have thrown BeanNotFoundException");
        } catch (BeanNotFoundException e) {
            
        }
        
        manager.addBean("myBean", new StringProperties().put("key", "value").getProperties());
        manager.setBean("myBean", new StringProperties(manager.getBean("myBean")).put("secondKey", "secondValue").getProperties());

        assertEquals("Expected myBean to exist in manager", 1, manager.getBeans().length);
        assertEquals("Expected myBean properties to exist in manager", 2, manager.getBean("myBean").size());
    }
    
    public void testRemoveBean() {
        DefaultBeanServer<AlertBean> manager = new DefaultBeanServer<AlertBean>(new NullMockAdmin());
        try {
            manager.removeBean("myBean");
            fail("Should have thrown BeanNotFoundException");
        } catch (BeanNotFoundException e) {
            
        }
        
        manager.addBean("myBean", new StringProperties().put("key", "value").getProperties());
        manager.setBean("myBean", new StringProperties(manager.getBean("myBean")).put("secondKey", "secondValue").getProperties());

        assertEquals("Expected myBean to exist in manager", 1, manager.getBeans().length);
        assertEquals("Expected myBean properties to exist in manager", 2, manager.getBean("myBean").size());
        
        manager.removeBean("myBean");
        assertEquals("Expected myBean to not exist in manager", 0, manager.getBeans().length);
        try {
            manager.getBean("myBean");
            fail("Should have thrown BeanNotFoundException");
        }catch(BeanNotFoundException e) {
        }
    }
    
    public void testEnableBean() {
        DefaultBeanServer<AlertBean> manager = new DefaultBeanServer<AlertBean>(new NullMockAdmin());
        try {
            manager.enableBean("myBean");
            fail("Should have thrown BeanNotFoundException");
        } catch (BeanNotFoundException e) {
        }
        
        manager.addBean(SimpleBean.class.getName(), new StringProperties().put("key", "value").getProperties());
        assertEquals("Expected zero enabled beans", 0, manager.getEnabledBeans().length);
        
        manager.enableBean(SimpleBean.class.getName());
        assertEquals("Expected one enabled bean", 1, manager.getEnabledBeans().length);
        assertEquals("Expected one managed bean", 1, manager.getBeans().length);
        assertEquals("Expected bean properties to exist in manager", 1, manager.getBean(SimpleBean.class.getName()).size());

        manager.setBean(SimpleBean.class.getName(), new StringProperties().put("new-key", "new-value").getProperties());
        assertEquals("Expected one enabled bean", 1, manager.getEnabledBeans().length);
        assertEquals("Expected one managed bean", 1, manager.getBeans().length);
        assertEquals("Expected bean properties to exist in manager", 1, manager.getBean(SimpleBean.class.getName()).size());
        assertEquals("Expected bean properties to be updated", "new-value", manager.getBean(SimpleBean.class.getName()).get("new-key"));
        
        manager.addBean("myBean", new StringProperties().put("key", "value").getProperties());
        assertEquals("Expected one enabled bean", 1, manager.getEnabledBeans().length);
        assertEquals("Expected two managed beans", 2, manager.getBeans().length);
    }
    
    public void testDisableBean() {
        DefaultBeanServer<AlertBean> manager = new DefaultBeanServer<AlertBean>(new NullMockAdmin());
        try {
            manager.disableBean("myBean");
            fail("Should have thrown BeanNotFoundException");
        } catch (BeanNotFoundException e) {
        }
        
        manager.addBean(SimpleBean.class.getName(), new StringProperties().put("key", "value").getProperties());
        assertEquals("Expected zero enabled beans", 0, manager.getEnabledBeans().length);
        
        manager.enableBean(SimpleBean.class.getName());
        assertEquals("Expected one enabled bean", 1, manager.getEnabledBeans().length);
        assertEquals("Expected one managed bean", 1, manager.getBeans().length);
        
        manager.addBean("myBean", new StringProperties().put("key", "value").getProperties());
        assertEquals("Expected one enabled bean", 1, manager.getEnabledBeans().length);
        assertEquals("Expected two managed beans", 2, manager.getBeans().length);
        
        manager.disableBean(SimpleBean.class.getName());
        assertEquals("Expected zero enabled beans", 0, manager.getEnabledBeans().length);
        assertEquals("Expected two managed beans", 2, manager.getBeans().length);
        
        manager.removeBean(SimpleBean.class.getName());
        assertEquals("Expected one managed beans", 1, manager.getBeans().length);
        assertNotNull(manager.getBean("myBean"));
        assertEquals("Expected myBean properties to exist in manager", 1, manager.getBean("myBean").size());
    }
    
    public void testRemoveEnabledBean() {
        DefaultBeanServer<AlertBean> manager = new DefaultBeanServer<AlertBean>(new NullMockAdmin());
        
        manager.addBean(SimpleBean.class.getName(), new StringProperties().put("key", "value").getProperties());
        manager.enableBean(SimpleBean.class.getName());
        
        manager.removeBean(SimpleBean.class.getName());
        assertEquals("Expected zero enabled beans", 0, manager.getEnabledBeans().length);
        assertEquals("Expected zero managed beans", 0, manager.getBeans().length);
    }
    
    public void testDisableAllBeans() {
        DefaultBeanServer<AlertBean> manager = new DefaultBeanServer<AlertBean>(new NullMockAdmin());
        manager.addBean("myBean-1", new StringProperties().getProperties());
        manager.addBean(SimpleBean.class.getName(), new StringProperties().put("key", "value").getProperties());
        manager.addBean("myBean-2", new StringProperties().getProperties());
        manager.enableBean(SimpleBean.class.getName());
        
        assertEquals("Expected three managed beans", 3, manager.getBeans().length);
        assertEquals("Expected one enabled beans", 1, manager.getEnabledBeans().length);
        
        manager.disableAllBeans();
        
        assertEquals("Expected three managed beans", 3, manager.getBeans().length);
        assertEquals("Expected zero enabled beans", 0, manager.getEnabledBeans().length);
    }
}
