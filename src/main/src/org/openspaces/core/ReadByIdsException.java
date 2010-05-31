// JAVA-DOC-STAMP
package org.openspaces.core;

import org.openspaces.core.exception.ExceptionTranslator;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import com.gigaspaces.client.ReadTakeByIdResult;

/**
 * Thrown when readByIds operation fails.
 *
 * <p>Thrown on:
 * <ul>
 * <li>Partial and complete failure.
 * <li>Cluster/single space topologies. 
 * </ul>
 *
 * <p>The exception contains an array of ReadByIdResult objects where each result in the array contains
 * either a read object or an exception upon failure. The result array index corresponds to the ID index in
 * the operation's supplied IDs array.
 * 
 * @author idan
 * @since 7.1.1
 *
 */
public class ReadByIdsException extends InvalidDataAccessResourceUsageException {

    private static final long serialVersionUID = 1L;
    private ReadByIdResult[] _results;
    
    public ReadByIdsException(com.gigaspaces.client.ReadByIdsException cause, ExceptionTranslator exceptionTranslator) {
        super(cause.getMessage(), cause);
        _results = new ReadByIdResult[cause.getResults().length];
        for (int i = 0; i < _results.length; i++) {
            _results[i] = new ReadByIdResult(cause.getResults()[i], exceptionTranslator);
        }        
    }
    
    /**
     * Returns the results contained in the exception.
     * @return An array of ReadByIdResult objects.
     */
    public ReadByIdResult[] getResults() {
        return _results;
    }
    
    /**
     * Holds a ReadByIdsException result.
     * The result contains the object's Id, the result type, the read object and the thrown exception. 
     * 
     * @author idan
     * @since 7.1.1
     *
     */
    public static class ReadByIdResult {
        
        /**
         * Determines the result type of a read by id operation result.
         * @author idan
         * @since 7.1.1
         *
         */
        public enum ReadByIdResultType {            
            /**
             * Operation failed - result contains the exception that caused the failure.
             */
            ERROR,          
            /**
             * Operation succeeded - result contains the object matching the corresponded Id.
             */
            OBJECT,         
            /**
             * Operation succeeded - there's no object matching the corresponded Id.
             */
            NOT_FOUND
        }
        
        private ReadTakeByIdResult _result;
        private ReadByIdResultType _resultType;
        private ExceptionTranslator _exceptionTranslator;
        
        protected ReadByIdResult(ReadTakeByIdResult result, ExceptionTranslator exceptionTranslator) {
            _result = result;
            if (_result.isError()) {
                _resultType = ReadByIdResultType.ERROR;                
            } else {
                _resultType = (_result.getObject() == null)? ReadByIdResultType.NOT_FOUND : ReadByIdResultType.OBJECT;
            }
            _exceptionTranslator = exceptionTranslator;
        }
                
        /**
         * @return On error returns the exception that occurred, otherwise null.
         */
        public Throwable getError() {
            if (_result.getError() == null) {
                return null;
            }
            return _exceptionTranslator.translate(_result.getError());
        }

        /**
         * @return The object Id this result is relevant for.
         */
        public Object getId() {
            return _result.getId();
        }

        /**
         * @return The read object for this result or null if no object was found or an exception occurred.
         */
        public Object getObject() {
            return _result.getObject();
        }

        /**
         * @return True if an exception is associated with this result.
         */
        public boolean isError() {
            return _result.isError();
        }
        
        /**
         * @return This result's object type.
         */
        public ReadByIdResultType getResultType() {
            return _resultType;
        }
    }    
        
    
}
