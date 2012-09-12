/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.jpa.openjpa.query;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.openjpa.lib.rop.ResultObjectProvider;
import org.apache.openjpa.meta.ClassMetaData;
import org.openspaces.jpa.StoreManager;

import com.gigaspaces.internal.transport.IEntryPacket;

/**
 * A wrapper for holding JPQL queries result set.
 * The getResultObject() method should initiate a state manager for the loaded object.
 * 
 * @author idan
 * @since 8.0
 * 
 */
public class SpaceResultObjectProvider implements ResultObjectProvider {
    //
    private Object[] _result;
    private int _currentIndex;
    private StoreManager _store;
    private ClassMetaData _classMetaData;
    
    public SpaceResultObjectProvider(ClassMetaData classMetaData, Object[] result, StoreManager store) {
        _result = result;
        _currentIndex = -1;
        _store = store;
        _classMetaData = classMetaData;
    }
    
    public boolean absolute(int pos) throws Exception {
        if (pos >= 0 && pos < _result.length) {
            _currentIndex = pos;
            return true;
        }
        return false;
    }

    public void close() throws Exception {
        reset();
    }

    /**
     * Gets the current result as a Pojo initiated with a state manager.
     */
    public Object getResultObject() throws Exception {
        return _store.loadObject(_classMetaData, (IEntryPacket) _result[_currentIndex]);
    }

    public void handleCheckedException(Exception e) {
        // openjpa: shouldn't ever happen
        throw new NestableRuntimeException(e);
    }

    public boolean next() throws Exception {
        return absolute(_currentIndex + 1);
    }

    public void open() throws Exception {                
    }

    public void reset() throws Exception {
        _currentIndex = -1;
    }

    public int size() throws Exception {
        return _result.length;
    }

    public boolean supportsRandomAccess() {
        return true;
    }

}
