/**
 *
 */
package org.openspaces.core;

import org.openspaces.core.exception.ExceptionTranslator;


/**
 * Thrown when one of the following space operations fails: <b>readMultiple,takeMultiple,clear.</b>
 *
 * <p>Thrown on:
 * <ul>
 * <li>Partial and complete failure.
 * <li>Cluster/single space topologies.
 * <li>SQLQueries/Templates.
 * </ul>
 *
 * <p>The exception contains:
 * <ul>
 * <li>An array of exceptions that caused it. One exception per each space that failed.
 * <li>An array of entries that were successfully read or take.
 * </ul.
 *
 * <p>
 * <b>Replaced {@link QueryMultiplePartialFailureException}.</b>
 *
 * @author anna
 * @since 7.1
 */
public class BatchQueryException extends QueryMultiplePartialFailureException {


    /**
     * @param cause
     * @param exceptionTranslator
     */
    public BatchQueryException(com.j_spaces.core.multiple.query.BatchQueryException cause, ExceptionTranslator exceptionTranslator) {
        super(cause, exceptionTranslator);
    }

    private static final long serialVersionUID = 1L;

}
