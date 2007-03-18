package org.openspaces.core.space.filter;

import com.j_spaces.core.filters.FilterProvider;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>A base class for filter adapters that delegate the invocation of filter operation and lifecycle
 * methods to another class. The delegate invocation is done using {@link FilterOperationDelegate}.
 *
 * <p>Subclasses should implement three methods. The first, {@link #doGetInvokerLookup()} provides a
 * map of operaion per {@link FilterOperationDelegateInvoker}. The other two provide filter lifecycle
 * methods {@link #doGetInitMethod()} and {@link #doGetCloseMethod()}.
 *
 * @author kimchy
 * @see org.openspaces.core.space.filter.FilterOperationDelegate
 */
public abstract class AbstractFilterProviderAdapterFactoryBean extends AbstractFilterProviderFactoryBean {

    /**
     * <p>Constructs a new {@link com.j_spaces.core.filters.FilterProvider} using
     * {@link org.openspaces.core.space.filter.FilterOperationDelegate} as the <code>ISpaceFilter</code>
     * implemenation.
     *
     * <p>Subclasses should provide the main Map of operation per {@link FilterOperationDelegateInvoker}
     * which is used to initialize the {@link FilterOperationDelegate}.
     */
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

    /**
     * Helper method for basclasses that add an invoker to the lookup map. Performs validation that no
     * other invoker is bounded to the operation code.
     */
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

    /**
     * Responsible for returning a lookup map of operation code to invoker.
     */
    protected abstract Map<Integer, FilterOperationDelegateInvoker> doGetInvokerLookup();

    /**
     * Retruns the filter lifecycle init method delegate. Can be <code>null</code>.
     */
    protected abstract Method doGetInitMethod();

    /**
     * Retruns the filter lifecycle close method delegate. Can be <code>null</code>.
     */
    protected abstract Method doGetCloseMethod();
}
