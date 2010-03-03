package org.openspaces.core;

import org.openspaces.core.exception.ExceptionTranslator;

/**
 * Thrown when a clear space operations fails.</b>
 *
 * <p>Thrown on:
 * <ul>
 * <li>Partial and complete failure.
 * <li>Cluster/single space topologies.
 * <li>SQLQueries/Templates.
 * </ul>
 * 
 * At space level - the failure is fail-fast - once an operation on an object failed - exception is returned immediately and no other objects are processed. <br><br>
 * 
 * At cluster level - if one of operation target spaces fails, first completion of the operation on other spaces is done. 
 * Then the aggregated exception is thrown. <br>  
 * <p>The exception contains:
 * <ul>
 * <li>An array of exceptions that caused it. One exception per each space that failed.
 * </ul>
 *
 * <p>
 * <b>Replaced {@link QueryMultiplePartialFailureException}.</b>
 *
 * @author anna
 * @since 7.1
 */
public class ClearException extends QueryMultiplePartialFailureException {
    private static final long serialVersionUID = 1L;

    /**
     * @param cause
     * @param exceptionTranslator
     */
    public ClearException(com.gigaspaces.client.ClearException cause, ExceptionTranslator exceptionTranslator) {
        super(cause, exceptionTranslator);
    }
}
