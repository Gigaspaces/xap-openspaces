package org.openspaces.core;

import org.openspaces.core.exception.ExceptionTranslator;

/**
 * Thrown when writeMultiple operation fails.
 *
 * <p>Thrown on:
 * <ul>
 * <li>Partial and complete failure.
 * <li>Cluster/single space topologies. 
 * </ul>
 *
 * <p>The exception contains an array of write results where each result in the array is either a lease or an exception upon failure, 
 * the result index corresponds to the entry index in the array of entries which are being written/updated. 
 *
 * <p>
 * <b>Replaced {@link WriteMultiplePartialFailureException}.</b>
 *
 *  </pre>
 * 
 * @author  eitany
 * @since   7.1
 */
public class WriteMultipleException extends WriteMultiplePartialFailureException  {
    private static final long serialVersionUID = 1L;

    public WriteMultipleException(com.gigaspaces.client.WriteMultipleException cause, ExceptionTranslator exceptionTranslator) {
        super(cause, exceptionTranslator);
    }
}
