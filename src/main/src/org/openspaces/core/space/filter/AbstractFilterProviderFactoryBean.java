package org.openspaces.core.space.filter;

import com.j_spaces.core.filters.FilterProvider;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>A base factory for {@link com.j_spaces.core.filters.FilterProvider} allowing to construct a
 * filter provider that can be used to provide pre intialized {@link com.j_spaces.core.filters.ISpaceFilter}
 * implementation including its different aspects to an embedded space.
 *
 * <p>Subclasses should implement {@link #doGetFilterProvider()} initalizing the filter provider. All its
 * different aspects will be initalized by this factory.
 *
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

    /**
     * Sets the filter that will be used. Note, it is not an {@link com.j_spaces.core.filters.ISpaceFilter ISpaceFilter}
     * implemenation since this object filter can be a delegate that does not require the filter to implement
     * the space filter interface..
     */
    public void setFilter(Object filter) {
        this.filter = filter;
    }

    protected Object getFilter() {
        return filter;
    }

    /**
     * Sets if the filter will be active when the space is in <code>BACKUP</code> mode. Defaults to <code>true<code>.
     */
    public void setActiveWhenBackup(boolean activeWhenBackup) {
        this.activeWhenBackup = activeWhenBackup;
    }

    /**
     * Is the filter enabled. Defaults to <code>true</code>.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Is this filter a security filter. Defaults to <code>false</code>.
     */
    public void setSecurityFilter(boolean securityFilter) {
        this.securityFilter = securityFilter;
    }

    /**
     * Should the space shutdown in case the init method fails (throws an exception).
     * Defaults to <code>false</code>.
     */
    public void setShutdownSpaceOnInitFailure(boolean shutdownSpaceOnInitFailure) {
        this.shutdownSpaceOnInitFailure = shutdownSpaceOnInitFailure;
    }

    /**
     * The filter priority. Defaults to <code>0</code>.
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Sets the bean name thanks to Spring {@link org.springframework.beans.factory.BeanNameAware}.
     * Used as the filter name.
     */
    public void setBeanName(String name) {
        this.beanName = name;
    }

    protected String getBeanName() {
        return this.beanName;
    }

    /**
     * <p>Constructs the filter provider and applies its different aspects. Delegates to
     * {@link #doGetFilterProvider()} for the actual filter provider creation.
     *
     * <p>Note, subclasses will need to intialize the filter provider with the relevant
     * operation codes it will listen on.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(filter, "filter property is required");
        this.filterProvider = doGetFilterProvider();
        Assert.notNull(filterProvider.getOpCodes(), "At least one operation code is required for filter provider");
        Assert.isTrue(filterProvider.getOpCodes().length > 0, "At least one operation code is required for filter provider");

        filterProvider.setPriority(priority);
        filterProvider.setActiveWhenBackup(activeWhenBackup);
        filterProvider.setEnabled(enabled);
        filterProvider.setSecurityFilter(securityFilter);
        filterProvider.setShutdownSpaceOnInitFailure(shutdownSpaceOnInitFailure);
    }

    /**
     * Sub classes should implement this method in order to create the actual filter provider. Note,
     * the created filter provider will have to be intialized with at least one operation code is
     * will listen on.
     */
    protected abstract FilterProvider doGetFilterProvider() throws IllegalArgumentException;

    public Object getObject() throws Exception {
        return this.filterProvider;
    }

    /**
     * Returns the {@link com.j_spaces.core.filters.FilterProvider} class as a type.
     */
    public Class getObjectType() {
        return FilterProvider.class;
    }

    /**
     * Returns <code>true</code>.
     */
    public boolean isSingleton() {
        return true;
    }

}
