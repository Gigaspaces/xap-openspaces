/**
 * 
 */
package org.openspaces.core;

import org.openspaces.core.exception.ExceptionTranslator;


/**
 * <pre>
 * Thrown when one of the following space operations fails:
 *  <b>readMultiple,takeMultiple,clear.</b>
 * 
 *  
 *  Thrown on:
 *  1. Partial and complete failure. 
 *  2. Cluster/single space topologies.
 *  3. SQLQueries/Templates.
 *    
 *  The exception contains:
 *  1. An array of exceptions that caused it. One exception per each space that failed.
 *  2. An array of entries that were successfully read or take.
 * 
 *  Replaced {@link QueryMultiplePartialFailureException}.
 *  </pre>
 * @author anna
 * @since 7.1
 */
public class BatchOperationException extends QueryMultiplePartialFailureException {

   
    /**
     * @param cause
     * @param exceptionTranslator
     */
    public BatchOperationException(com.j_spaces.core.multiple.query.QueryMultiplePartialFailureException cause, ExceptionTranslator exceptionTranslator) {
        super(cause, exceptionTranslator);
        
    }

    private static final long serialVersionUID = 1L;

}
