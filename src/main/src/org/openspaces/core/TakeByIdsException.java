// JAVA-DOC-STAMP
package org.openspaces.core;

import org.openspaces.core.exception.ExceptionTranslator;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import com.gigaspaces.client.ReadTakeByIdResult;
import com.gigaspaces.client.TakeByIdsException.TakeByIdResult;

/**
 * Thrown when takeByIds operation fails.
 *
 * <p>Thrown on:
 * <ul>
 * <li>Partial and complete failure.
 * <li>Cluster/single space topologies. 
 * </ul>
 *
 * <p>The exception contains an array of ITakeByIdResult objects where each result in the array contains
 * either a read object or an exception upon failure. The result array index corresponds to the ID index in
 * the operation's supplied IDs array.
 * 
 * @author idan
 * @since 7.1.1
 *
 */
public class TakeByIdsException extends InvalidDataAccessResourceUsageException {

    private static final long serialVersionUID = 1L;
    private ExceptionTranslator _exceptionTranslator;
    private TakeByIdResult[] _results;
    
    public TakeByIdsException(com.gigaspaces.client.TakeByIdsException cause, ExceptionTranslator exceptionTranslator) {
        super(cause.getMessage(), cause);
        _exceptionTranslator = exceptionTranslator;
        _results = new TakeByIdResult[cause.getResults().length];
        for (int i = 0; i < _results.length; i++) {
            _results[i] = new TranslatedTakeByIdResult(cause.getResults()[i]);
        }
    }

    /**
     * Returns the results contained in the exception.
     * @return An array of TakeByIdResult objects.
     */
    public TakeByIdResult[] getResults() {
        return _results;
    }
        
    private class TranslatedTakeByIdResult implements TakeByIdResult {

        private ReadTakeByIdResult _result;
        private TakeByIdResultType _resultType;                
        
        public TranslatedTakeByIdResult(ReadTakeByIdResult result) {
            _result = result;
            if (_result.isError()) {
                _resultType = TakeByIdResultType.ERROR;                
            } else {
                _resultType = (_result.getObject() == null)? TakeByIdResultType.NOT_FOUND : TakeByIdResultType.OBJECT;
            }            
        }
        
        public Throwable getError() {
            if (_result.getError() == null) {
                return null;
            }
            return _exceptionTranslator.translate(_result.getError());
        }

        public Object getId() {
            return _result.getId();
        }

        public Object getObject() {
            return _result.getObject();
        }

        public boolean isError() {
            return _result.isError();
        }

        public TakeByIdResultType getResultType() {
            return _resultType;
        }        
    }
    
    
}
