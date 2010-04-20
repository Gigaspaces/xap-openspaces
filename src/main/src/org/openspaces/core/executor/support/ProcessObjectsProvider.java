package org.openspaces.core.executor.support;

/**
 * An interface used with {@link org.openspaces.core.executor.Task} allowing to return
 * additional objects to be processed on the node the task is executed. Processing an
 * objects allows to inject resources defined within the processing unit.
 *
 * @author kimchy
 */
public interface ProcessObjectsProvider {

    /**
     * Returns an array of objects that needs processing on the space node side. Processing an
 * objects allows to inject resources defined within the processing unit.
     */
    Object[] getObjectsToProcess();
}
