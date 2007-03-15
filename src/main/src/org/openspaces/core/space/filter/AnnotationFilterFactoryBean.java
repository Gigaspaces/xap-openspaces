package org.openspaces.core.space.filter;

import com.j_spaces.core.filters.FilterOperationCodes;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kimchy
 */
public class AnnotationFilterFactoryBean extends AbstractFilterProviderAdapterFactoryBean {

    protected Map<Integer, FilterOperationDelegateInvoker> doGetInvokerLookup(Object filter) {
        final Map<Integer, FilterOperationDelegateInvoker> invokerLookup = new HashMap<Integer, FilterOperationDelegateInvoker>();
        ReflectionUtils.doWithMethods(getFilter().getClass(), new ReflectionUtils.MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                if (method.getAnnotation(BeforeWrite.class) != null) {
                    invokerLookup.put(FilterOperationCodes.BEFORE_WRITE, new FilterOperationDelegateInvoker(FilterOperationCodes.BEFORE_WRITE, method));
                }
            }
        });
        return invokerLookup;
    }

    protected Method doGetInitMethod(Object filter) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected Method doGetCloseMethod(Object filter) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
