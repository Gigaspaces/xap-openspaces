package org.openspaces.core.space.filter;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.SpaceContext;
import com.j_spaces.core.filters.entry.ISpaceFilterEntry;
import net.jini.core.entry.UnusableEntryException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <p>A filter operation delegate invoker, invoking a method associated with the given operation code.
 *
 * <p>For sinlge {@link com.j_spaces.core.filters.entry.ISpaceFilterEntry ISpaceFilterEntry} invocation
 * (see {@link com.j_spaces.core.filters.ISpaceFilter#process(com.j_spaces.core.SpaceContext,com.j_spaces.core.filters.entry.ISpaceFilterEntry,int) process})
 * support the following different structures:
 * <ul>
 * <li>A no op method callback. For example <code>test()</code></li>
 * <li>A single parameter. The parameter can either be an {@link com.j_spaces.core.filters.entry.ISpaceFilterEntry}
 * or the actual template object wrapped by the entry. Note, if using actual types, this delegate will filter out
 * all the types that are not assignable to it. For example: <code>test(ISpaceFilterEntry entry)</li> or
 * <code>test(Message message)</code>.
 * <li>Two parameters. The first one maps to the prevoius option, the second one is the operation code.</li>
 * <li>Three parameters. The first two maps to the previous option, the third one is a {@link com.j_spaces.core.SpaceContext}.
 * </ul>
 *
 * <p>For multiple {@link com.j_spaces.core.filters.entry.ISpaceFilterEntry} invocation
 * (see {@link com.j_spaces.core.filters.ISpaceFilter#process(com.j_spaces.core.SpaceContext,com.j_spaces.core.filters.entry.ISpaceFilterEntry[],int) process})
 * supprt the following different structures:
 * <ul>
 * <li>A no op method callback. For example <code>test()</code></li>
 * <li>A single parameter. The parameter can either be an {@link com.j_spaces.core.filters.entry.ISpaceFilterEntry}
 * or the actual template object wrapped by the entry. Note, if using actual types, this delegate will filter out
 * all the types that are not assignable to it. For example: <code>test(ISpaceFilterEntry entry)</li> or
 * <code>test(Message message)</code>.
 * <li>Two parameters. The first one maps to the prevoius option, the second is the same as the first one since
 * multiple entries always have two entries (mainly for update operations).</li>
 * <li>Three parameters. The first two maps to the previous option, the third one is the operaiton code.</li>
 * <li>Four parameters. The first three maps to the previous option, the fourth one is a {@link com.j_spaces.core.SpaceContext}.
 * </ul>
 *
 * @author kimchy
 */
class FilterOperationDelegateInvoker {

    private boolean filterOnTypes = true;

    private int operationCode;

    private Method processMethod;

    /**
     * Constructs a new delegate for the given operation code and a method to invoke.
     */
    public FilterOperationDelegateInvoker(int operationCode, Method processMethod) {
        this.operationCode = operationCode;
        this.processMethod = processMethod;
        this.processMethod.setAccessible(true);
    }

    /**
     * Returns the operation code this delegate represents.
     */
    public int getOperationCode() {
        return operationCode;
    }

    /**
     * Returns the method that will be delegated to.
     */
    public Method getProcessMethod() {
        return processMethod;
    }

    /**
     * Invokes the method based on a single entry. See {@link FilterOperationDelegateInvoker}.
     */
    public void invokeProcess(IJSpace space, Object delegate, SpaceContext context, ISpaceFilterEntry entry)
            throws FilterExecutionException {
        Object[] params;
        int numberOfParams = processMethod.getParameterTypes().length;
        if (numberOfParams == 0) {
            params = null;
        } else {
            Object entryParam = entry;
            if (entryParam != null) {
                entryParam = detectSpaceFilterEntryParam(processMethod.getParameterTypes()[0], space, entry);
                // perform filtering based on type
                if (entryParam == null) {
                    return;
                }
            }
            if (numberOfParams == 1) {
                params = new Object[]{entryParam};
            } else if (numberOfParams == 2) {
                params = new Object[]{entryParam, operationCode};
            } else if (numberOfParams == 3) {
                params = new Object[]{entryParam, operationCode, context};
            } else {
                throw new FilterExecutionException("Method [" + processMethod.getName() + "] should not have more than 3 parameters");
            }
        }
        try {
            processMethod.invoke(delegate, params);
        } catch (IllegalAccessException e) {
            throw new FilterExecutionException("Failed to access method [" + processMethod.getName() +
                    "] with operation code [" + operationCode + "]", e);
        } catch (InvocationTargetException e) {
            throw new FilterExecutionException("Failed to execute method [" + processMethod.getName() +
                    "] with operation code [" + operationCode + "]", e);
        }
    }

    /**
     * Invokes the method based on a multiple entries. See {@link FilterOperationDelegateInvoker}.
     */
    public void invokeProcess(IJSpace space, Object delegate, SpaceContext context, ISpaceFilterEntry[] entries)
            throws FilterExecutionException {
        Object[] params = null;
        int numberOfParams = processMethod.getParameterTypes().length;
        if (numberOfParams == 0) {
            params = null;
        } else {
            Object entryParam1 = entries[0];
            if (entryParam1 != null) {
                entryParam1 = detectSpaceFilterEntryParam(processMethod.getParameterTypes()[0], space, entries[0]);
                if (entryParam1 == null) {
                    return;
                }
            }
            if (numberOfParams == 1) {
                params = new Object[]{entryParam1};
            } else {
                Object entryParam2 = entries[1];
                if (entryParam2 != null) {
                    entryParam2 = detectSpaceFilterEntryParam(processMethod.getParameterTypes()[1], space, entries[1]);
                    if (entryParam2 == null) {
                        return;
                    }
                }
                if (numberOfParams == 2) {
                    params = new Object[]{entryParam1, entryParam2};
                } else if (numberOfParams == 3) {
                    params = new Object[]{entryParam1, entryParam2, operationCode};
                } else if (numberOfParams == 4) {
                    params = new Object[]{entryParam1, entryParam2, operationCode, context};
                } else {
                    throw new FilterExecutionException("Method [" + processMethod.getName() + "] should not have more than 4 parameters");
                }
            }
        }
        try {
            processMethod.invoke(delegate, params);
        } catch (IllegalAccessException e) {
            throw new FilterExecutionException("Failed to access method [" + processMethod.getName() +
                    "] with operation code [" + operationCode + "]", e);
        } catch (InvocationTargetException e) {
            throw new FilterExecutionException("Failed to execute method [" + processMethod.getName() +
                    "] with operation code [" + operationCode + "]", e);
        }
    }

    private Object detectSpaceFilterEntryParam(Class paramType, IJSpace space, ISpaceFilterEntry entry)
            throws FilterExecutionException {
        if (ISpaceFilterEntry.class.isAssignableFrom(paramType)) {
            return entry;
        }
        Object retVal;
        try {
            retVal = entry.getObject(space);
        } catch (UnusableEntryException e) {
            throw new FilterExecutionException("Failed to get object from entry [" + entry + "]", e);
        }
        if (filterOnTypes) {
            if (paramType.isAssignableFrom(retVal.getClass())) {
                return retVal;
            } else {
                return null;
            }
        }
        return retVal;
    }
}
