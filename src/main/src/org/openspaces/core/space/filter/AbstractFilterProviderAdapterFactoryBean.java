package org.openspaces.core.space.filter;

import com.j_spaces.core.filters.FilterProvider;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author kimchy
 */
public abstract class AbstractFilterProviderAdapterFactoryBean implements FactoryBean, InitializingBean, BeanNameAware {

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

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(filter, "filter property is required");
        Map<Integer, FilterOperationDelegateInvoker> invokerLookup = doGetInvokerLookup();
        if (invokerLookup.size() == 0) {
            throw new IllegalArgumentException("No invoker found in filter [" + filter + "]");
        }
        FilterOperationDelegate delegate = new FilterOperationDelegate(filter, invokerLookup);
        delegate.setInitMethod(doGetInitMethod());
        delegate.setCloseMethod(doGetCloseMethod());

        filterProvider = new FilterProvider(beanName, delegate);
        filterProvider.setPriority(priority);
        filterProvider.setActiveWhenBackup(activeWhenBackup);
        filterProvider.setEnabled(enabled);
        filterProvider.setSecurityFilter(securityFilter);
        filterProvider.setShutdownSpaceOnInitFailure(shutdownSpaceOnInitFailure);

        // set up operation codes
        List<Integer> operationCodes = new ArrayList<Integer>();
        for (FilterOperationDelegateInvoker invoker : invokerLookup.values()) {
            operationCodes.add(invoker.getOperationCode());
        }
        int[] opCodes = new int[operationCodes.size()];
        for (int i = 0; i < opCodes.length; i++) {
            opCodes[i] = operationCodes.get(i);
        }
        filterProvider.setOpCodes(opCodes);
    }

    public Object getObject() throws Exception {
        return this.filterProvider;
    }

    public Class getObjectType() {
        return FilterProvider.class;
    }

    public boolean isSingleton() {
        return true;
    }

    protected void addInvoker(Map<Integer, FilterOperationDelegateInvoker> invokerLookup, Method method, int operationCode) throws IllegalArgumentException {
        FilterOperationDelegateInvoker invoker = invokerLookup.get(operationCode);
        if (invoker != null) {
            throw new IllegalArgumentException("Filter adapter only allows for a single method for each operation. " +
                    "operation [" + operationCode + "] has method [" + invoker.getProcessMethod().getName() + "] and method [" +
                    method.getName() + "]");
        }
        // TODO add paramter validation
        invokerLookup.put(operationCode, new FilterOperationDelegateInvoker(operationCode, method));
    }

    protected abstract Map<Integer, FilterOperationDelegateInvoker> doGetInvokerLookup();

    protected abstract Method doGetInitMethod();

    protected abstract Method doGetCloseMethod();
}
