package org.openspaces.core;

import org.openspaces.core.exception.ExceptionTranslator;

/**
 * Thrown when a readMultiple space operations fails.</b>
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
 * </ul>
 *
 * <p>
 * <b>Replaced {@link QueryMultiplePartialFailureException}.</b>
 *
 * @author anna
 * @since 7.1
 */
public class ReadMultipleException extends QueryMultiplePartialFailureException {
    private static final long serialVersionUID = 1L;

    /**
     * @param cause
     * @param exceptionTranslator
     */
    public ReadMultipleException(com.gigaspaces.client.ReadMultipleException cause, ExceptionTranslator exceptionTranslator) {
        super(cause, exceptionTranslator);
    }
}
