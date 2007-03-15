package org.openspaces.core.space.filter;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.SpaceContext;
import com.j_spaces.core.filters.ISpaceFilter;
import com.j_spaces.core.filters.entry.ISpaceFilterEntry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author kimchy
 */
public class FilterOperationDelegate implements ISpaceFilter {

    private Object delegate;

    private Map<Integer, FilterOperationDelegateInvoker> invokerLookup;

    private Method initMethod;

    private Method closeMethod;


    private IJSpace space;

    public FilterOperationDelegate(Object delegate, Map<Integer, FilterOperationDelegateInvoker> invokerLookup) {
        this.delegate = delegate;
        this.invokerLookup = invokerLookup;
    }

    public void setInitMethod(Method initMethod) {
        this.initMethod = initMethod;
        if (initMethod != null) {
            initMethod.setAccessible(true);
        }
    }

    public void setCloseMethod(Method closeMethod) {
        this.closeMethod = closeMethod;
        if (closeMethod != null) {
            closeMethod.setAccessible(true);
        }
    }

    public void init(IJSpace space, String filterId, String url, int priority) throws RuntimeException {
        this.space = space;
        if (initMethod == null) {
            return;
        }
        Object[] params = null;
        if (initMethod.getParameterTypes().length == 1) {
            params = new Object[]{space};
        }
        try {
            initMethod.invoke(delegate, params);
        } catch (IllegalAccessException e) {
            throw new FilterExecutionException("Failed to access init method [" + initMethod.getName() + "]", e);
        } catch (InvocationTargetException e) {
            throw new FilterExecutionException("Failed to execute init method [" + initMethod.getName() + "]", e);
        }
    }

    public void process(SpaceContext context, ISpaceFilterEntry entry, int operationCode) throws RuntimeException {
        FilterOperationDelegateInvoker invoker = invokerLookup.get(operationCode);
        if (invoker != null) {
            invoker.invokeProcess(space, delegate, context, entry);
        }
    }

    public void process(SpaceContext context, ISpaceFilterEntry[] entries, int operationCode) throws RuntimeException {
        FilterOperationDelegateInvoker invoker = invokerLookup.get(operationCode);
        if (invoker != null) {
            invoker.invokeProcess(space, delegate, context, entries);
        }
    }

    public void close() throws RuntimeException {
        if (closeMethod == null) {
            return;
        }
        try {
            closeMethod.invoke(delegate);
        } catch (IllegalAccessException e) {
            throw new FilterExecutionException("Failed to access close method [" + closeMethod.getName() + "]", e);
        } catch (InvocationTargetException e) {
            throw new FilterExecutionException("Failed to execute close method [" + closeMethod.getName() + "]", e);
        }
    }
}
