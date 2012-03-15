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
package org.openspaces.utest.enhancer.entry.index;

import junit.framework.TestCase;

import java.lang.reflect.Method;

/**
 * @author kimchy
 */
public class IndexEntryEnhancerTests extends TestCase {

    public void testUIDGeneration() throws Exception {
        Data data = new Data();

        Method getIndexed = Data.class.getMethod("__getSpaceIndexedFields");
        String[] indexedFields = (String[]) getIndexed.invoke(data);
        assertEquals(2, indexedFields.length);
        assertEquals("value3", indexedFields[0]);
        assertEquals("value2", indexedFields[1]);
    }
}
