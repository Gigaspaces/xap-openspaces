package org.openspaces.core.space.filter;

import com.j_spaces.core.filters.FilterOperationCodes;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link com.j_spaces.core.filters.FilterProvider FilterProvider} factory that accepts
 * a Pojo filter with annotation markers as to which filter operarion to listen to. The
 * available annotations are the different annotations found within this package with
 * either the <code>Before</code> prefix or the <code>After</code> prefix (for example:
 * {@link BeforeWrite} and {@link AfterWrite}). Filter lifecycle methods can be marked
 * using {@link OnFilterInit} and {@link OnFilterClose} annotations.
 *
 * <p>The annotated operation callback methods can different arguments. Please see
 * {@link org.openspaces.core.space.filter.FilterOperationDelegateInvoker} for all
 * the different possibilites.
 *
 * <p>For a Pojo adapter that does not use annotations please see {@link MethodFilterFactoryBean}.
 *
 * @author kimchy
 * @see org.openspaces.core.space.filter.FilterOperationDelegate
 * @see com.j_spaces.core.filters.FilterProvider
 * @see com.j_spaces.core.filters.ISpaceFilter
 * @see com.j_spaces.core.filters.FilterOperationCodes
 */
public class AnnotationFilterFactoryBean extends AbstractFilterProviderAdapterFactoryBean {

    /**
     * Creates an operation code to filter invoker map based on the {@link #getFilter()}
     * delegate and its annotated methods.
     */
    protected Map<Integer, FilterOperationDelegateInvoker> doGetInvokerLookup() {
        final Map<Integer, FilterOperationDelegateInvoker> invokerLookup = new HashMap<Integer, FilterOperationDelegateInvoker>();
        ReflectionUtils.doWithMethods(getFilter().getClass(), new ReflectionUtils.MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                if (method.isAnnotationPresent(BeforeWrite.class)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_WRITE);
                }
                if (method.isAnnotationPresent(AfterWrite.class)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.AFTER_WRITE);
                }
                if (method.isAnnotationPresent(BeforeRead.class)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_READ);
                }
                if (method.isAnnotationPresent(BeforeTake.class)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_TAKE);
                }
                if (method.isAnnotationPresent(BeforeNotify.class)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_NOTIFY);
                }
                if (method.isAnnotationPresent(BeforeCleanSpace.class)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_CLEAN_SPACE);
                }
                if (method.isAnnotationPresent(BeforeUpdate.class)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_UPDATE);
                }
                if (method.isAnnotationPresent(AfterUpdate.class)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.AFTER_UPDATE);
                }
                if (method.isAnnotationPresent(BeforeReadMultiple.class)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_READ_MULTIPLE);
                }
                if (method.isAnnotationPresent(AfterReadMultiple.class)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.AFTER_READ_MULTIPLE);
                }
                if (method.isAnnotationPresent(BeforeTakeMultiple.class)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_TAKE_MULTIPLE);
                }
                if (method.isAnnotationPresent(AfterTakeMultiple.class)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.AFTER_TAKE_MULTIPLE);
                }
                if (method.isAnnotationPresent(BeforeNotifyTrigger.class)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_NOTIFY_TRIGGER);
                }
                if (method.isAnnotationPresent(AfterNotifyTrigger.class)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.AFTER_NOTIFY_TRIGGER);
                }
                if (method.isAnnotationPresent(BeforeAllNotifyTrigger.class)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_ALL_NOTIFY_TRIGGER);
                }
                if (method.isAnnotationPresent(AfterAllNotifyTrigger.class)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.AFTER_ALL_NOTIFY_TRIGGER);
                }
                if (method.isAnnotationPresent(BeforeRemoveByLease.class)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.BEFORE_REMOVE);
                }
                if (method.isAnnotationPresent(AfterRemoveByLease.class)) {
                    addInvoker(invokerLookup, method, FilterOperationCodes.AFTER_REMOVE);
                }
            }
        });
        return invokerLookup;
    }

    /**
     * Returns the filter lifcycle method anntated with {@link OnFilterInit}.
     */
    protected Method doGetInitMethod() {
        final AtomicReference<Method> ref = new AtomicReference<Method>();
        ReflectionUtils.doWithMethods(getFilter().getClass(), new ReflectionUtils.MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                if (method.isAnnotationPresent(OnFilterInit.class)) {
                    ref.set(method);
                }
            }
        });
        return ref.get();
    }

    /**
     * Returns the filter lifcycle method anntated with {@link OnFilterClose}.
     */
    protected Method doGetCloseMethod() {
        final AtomicReference<Method> ref = new AtomicReference<Method>();
        ReflectionUtils.doWithMethods(getFilter().getClass(), new ReflectionUtils.MethodCallback() {
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {
                if (method.isAnnotationPresent(OnFilterClose.class)) {
                    ref.set(method);
                }
            }
        });
        return ref.get();
    }
}
