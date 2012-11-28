/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.itest.core.space.datasource;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.SpaceDataSource;
import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;

/**
 * 
 * @author Idan Moyal
 * @since 9.1.1
 *
 */
public class MockSpaceDataSource extends SpaceDataSource {

    @Override
    public DataIterator<SpaceTypeDescriptor> initialMetadataLoad() {
        final SpaceTypeDescriptor typeDescriptor = new SpaceTypeDescriptorBuilder("MockDocument").idProperty("id").create();
        return new DataIterator<SpaceTypeDescriptor>() {
            boolean next = true;
            @Override
            public boolean hasNext() {
                if (next) {
                    next = false;
                    return true;
                }
                return false;
            }
            @Override
            public SpaceTypeDescriptor next() {
                return typeDescriptor;
            }
            @Override
            public void remove() {
            }
            @Override
            public void close() {
            }
        };
    }    
    
    @Override
    public DataIterator<Object> initialDataLoad() {
        final SpaceDocument document = new SpaceDocument("MockDocument");
        document.setProperty("id", "aaa");
        return new DataIterator<Object>() {
            boolean next = true;
            @Override
            public boolean hasNext() {
                if (next) {
                    next = false;
                    return true;
                }
                return false;
            }
            @Override
            public Object next() {
                return document;
            }
            @Override
            public void remove() {
            }
            @Override
            public void close() {
            }
        };
    }
    
    
    
    
}
