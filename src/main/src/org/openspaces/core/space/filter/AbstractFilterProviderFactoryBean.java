package org.openspaces.core.space.filter;

import com.j_spaces.core.filters.FilterProvider;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * @author kimchy
 */
public abstract class AbstractFilterProviderFactoryBean implements FactoryBean, InitializingBean, BeanNameAware {

    private Object filter;

    private boolean activeWhenBackup = true;

    private boolean enabled = true;

    private boolean securityFilter = false;

    private boolean shutdownSpaceOnInitFailure = false;

    private int priority = 0;


    private String beanName;

    private FilterProvider filterProvider;

    public void setFilter(Object filter) {
        this.filter = filter;
    }

    protected Object getFilter() {
        return filter;
    }

    public void setActiveWhenBackup(boolean activeWhenBackup) {
        this.activeWhenBackup = activeWhenBackup;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setSecurityFilter(boolean securityFilter) {
        this.securityFilter = securityFilter;
    }

    public void setShutdownSpaceOnInitFailure(boolean shutdownSpaceOnInitFailure) {
        this.shutdownSpaceOnInitFailure = shutdownSpaceOnInitFailure;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setBeanName(String name) {
        this.beanName = name;
    }

    protected String getBeanName() {
        return this.beanName;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(filter, "filter property is required");
        this.filterProvider = doGetFilterProvider();
        
        filterProvider.setPriority(priority);
        filterProvider.setActiveWhenBackup(activeWhenBackup);
        filterProvider.setEnabled(enabled);
        filterProvider.setSecurityFilter(securityFilter);
        filterProvider.setShutdownSpaceOnInitFailure(shutdownSpaceOnInitFailure);
    }

    protected abstract FilterProvider doGetFilterProvider() throws IllegalArgumentException;

    public Object getObject() throws Exception {
        return this.filterProvider;
    }

    public Class getObjectType() {
        return FilterProvider.class;
    }

    public boolean isSingleton() {
        return true;
    }

}
