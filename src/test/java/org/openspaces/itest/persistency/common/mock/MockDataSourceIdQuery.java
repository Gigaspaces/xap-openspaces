/*******************************************************************************
 *
 * Copyright (c) 2013 GigaSpaces Technologies Ltd. All rights reserved
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
package org.openspaces.itest.persistency.common.mock;

import com.gigaspaces.datasource.DataSourceIdQuery;
import com.gigaspaces.metadata.SpaceTypeDescriptor;

public class MockDataSourceIdQuery implements DataSourceIdQuery {

    private final SpaceTypeDescriptor typeDescriptor;
    private final Object id;

    public MockDataSourceIdQuery(SpaceTypeDescriptor typeDescriptor, Object id) {
        this.typeDescriptor = typeDescriptor;
        this.id = id;
    }
    
    @Override
    public SpaceTypeDescriptor getTypeDescriptor() {
        return typeDescriptor;
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public int getVersion() {
        return 0;
    }
}
