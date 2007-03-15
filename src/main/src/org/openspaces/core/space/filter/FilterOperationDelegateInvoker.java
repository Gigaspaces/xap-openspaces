package org.openspaces.core.space.filter;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.SpaceContext;
import com.j_spaces.core.filters.entry.ISpaceFilterEntry;
import net.jini.core.entry.UnusableEntryException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author kimchy
 */
class FilterOperationDelegateInvoker {

    private boolean filterOnTypes = true;

    private int operationCode;

    private Method processMethod;

    public FilterOperationDelegateInvoker(int operationCode, Method processMethod) {
        this.operationCode = operationCode;
        this.processMethod = processMethod;
        this.processMethod.setAccessible(true);
    }

    public int getOperationCode() {
        return operationCode;
    }

    public void invokeProcess(IJSpace space, Object delegate, SpaceContext context, ISpaceFilterEntry entry)
            throws FilterExecutionException {
        Object[] params = null;
        int numberOfParams = processMethod.getParameterTypes().length;
        if (numberOfParams == 0) {
            params = null;
        } else {
            Object entryParam = detectSpaceFilterEntryParam(processMethod.getParameterTypes()[0], space, entry);
            if (entryParam == null) {
                return;
            }
            if (numberOfParams == 1) {
                params = new Object[]{entryParam};
            } else if (numberOfParams == 2) {
                params = new Object[]{entryParam, context};
            } else if (numberOfParams == 3) {
                params = new Object[]{entryParam, context, operationCode};
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

    public void invokeProcess(IJSpace space, Object delegate, SpaceContext context, ISpaceFilterEntry[] entries)
            throws FilterExecutionException {
        Object[] params = null;
        int numberOfParams = processMethod.getParameterTypes().length;
        if (numberOfParams == 0) {
            params = null;
        } else {
            Object entryParam1 = detectSpaceFilterEntryParam(processMethod.getParameterTypes()[0], space, entries[0]);
            if (entryParam1 == null) {
                return;
            }
            if (numberOfParams == 1) {
                params = new Object[]{entryParam1};
            } else {
                Object entryParam2 = detectSpaceFilterEntryParam(processMethod.getParameterTypes()[1], space, entries[1]);
                if (entryParam2 == null) {
                    return;
                }
                if (numberOfParams == 2) {
                    params = new Object[]{entryParam1, entryParam2};
                } else if (numberOfParams == 3) {
                    params = new Object[]{entryParam1, entryParam2, context};
                } else if (numberOfParams == 4) {
                    params = new Object[]{entryParam1, entryParam2, context, operationCode};
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
