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
package org.openspaces.persistency.patterns;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.datasource.ManagedDataSource;
import com.gigaspaces.datasource.SQLDataProvider;
import com.j_spaces.core.client.SQLQuery;

/**
 * A sql data provider that redirects the sql basded operatinos to the given data source that
 * can handle the given type.
 *
 * @author kimchy
 * @deprecated since 9.5 - use {@link SpaceDataSourceSplitter} instead.
 */
@Deprecated
public class SQLDataProviderSplitter extends BulkDataPersisterSplitter implements SQLDataProvider {

    public SQLDataProviderSplitter(ManagedDataSourceEntriesProvider[] dataSources) {
        super(dataSources);
        for (ManagedDataSource dataSource : dataSources) {
            if (!(dataSource instanceof SQLDataProvider)) {
                throw new IllegalArgumentException("data source [" + dataSource + "] must implement SQLDataProvider");
            }
        }
    }

    public DataIterator iterator(SQLQuery sqlQuery) throws DataSourceException {
        return ((SQLDataProvider) getDataSource(sqlQuery.getTypeName())).iterator(sqlQuery);
    }
}
