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
package org.openspaces.itest.core.space.datasource;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.datasource.ManagedDataSource;

import java.util.Properties;

/**
 * @author kimchy
 */
public class MockManagedDataSource implements ManagedDataSource {

    private boolean initCalled;

    public void init(Properties properties) throws DataSourceException {
        initCalled = true;
    }

    public void shutdown() throws DataSourceException {
    }

    public boolean isInitCalled() {
        return initCalled;
    }

    public DataIterator initialLoad() throws DataSourceException {
        return null;
    }
}
