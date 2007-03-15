package org.openspaces.core.space.filter;

import com.j_spaces.core.filters.FilterProvider;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author kimchy
 */
public abstract class AbstractFilterProviderAdapterFactoryBean extends AbstractFilterProviderFactoryBean {

    protected FilterProvider doGetFilterProvider() throws IllegalArgumentException {
        Map<Integer, FilterOperationDelegateInvoker> invokerLookup = doGetInvokerLookup();
        if (invokerLookup.size() == 0) {
            throw new IllegalArgumentException("No invoker found in filter [" + getFilter() + "]");
        }
        FilterOperationDelegate delegate = new FilterOperationDelegate(getFilter(), invokerLookup);
        delegate.setInitMethod(doGetInitMethod());
        delegate.setCloseMethod(doGetCloseMethod());

        FilterProvider filterProvider = new FilterProvider(getBeanName(), delegate);

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
        return filterProvider;
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
