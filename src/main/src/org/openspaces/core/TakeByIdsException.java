package org.openspaces.core;

import org.openspaces.core.exception.ExceptionTranslator;
import org.springframework.dao.InvalidDataAccessResourceUsageException;

import com.gigaspaces.client.TakeByIdsException.ITakeByIdResult;

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
    private ITakeByIdResult[] _results;
    
    public TakeByIdsException(com.gigaspaces.client.TakeByIdsException cause, ExceptionTranslator exceptionTranslator) {
        super(cause.getMessage(), cause);
        _exceptionTranslator = exceptionTranslator;
        _results = new ITakeByIdResult[cause.getResults().length];
        for (int i = 0; i < _results.length; i++) {
            _results[i] = new TranslatedTakeByIdResult(cause.getResults()[i]);
        }
    }

    /**
     * Returns the results contained in the exception.
     * @return An array of IReadByIdResult objects.
     */
    public ITakeByIdResult[] getResults() {
        return _results;
    }
        
    private class TranslatedTakeByIdResult implements ITakeByIdResult {

        private ITakeByIdResult _result;
        
        public TranslatedTakeByIdResult(ITakeByIdResult result) {
            _result = result;
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
    }
    
    
}
