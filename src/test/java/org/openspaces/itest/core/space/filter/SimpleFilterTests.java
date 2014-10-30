/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.itest.core.space.filter;

import com.j_spaces.core.filters.FilterOperationCodes;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
import org.openspaces.itest.utils.EmptySpaceDataObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;


/**
 * @author kimchy
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/core/space/filter/simple-filter.xml")
public class SimpleFilterTests   { 

     @Autowired protected SimpleFilter simpleFilter;

     @Autowired protected GigaSpace gigaSpace;

    public SimpleFilterTests() {
 
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/space/filter/simple-filter.xml"};
    }


     @Test public void testFilter() {
        assertNotNull(simpleFilter.gigaSpace);
        assertNull(simpleFilter.getStats().get(FilterOperationCodes.BEFORE_WRITE));
        assertNull(simpleFilter.getStats().get(FilterOperationCodes.AFTER_WRITE));
        assertNull(simpleFilter.getStats().get(FilterOperationCodes.BEFORE_READ));
        assertNull(simpleFilter.getStats().get(FilterOperationCodes.BEFORE_TAKE));

        gigaSpace.write(new EmptySpaceDataObject());
        assertEquals(1, simpleFilter.getStats().get(FilterOperationCodes.BEFORE_WRITE).intValue());
        assertEquals(1, simpleFilter.getStats().get(FilterOperationCodes.AFTER_WRITE).intValue());
        assertNull(simpleFilter.getStats().get(FilterOperationCodes.BEFORE_READ));
        assertNull(simpleFilter.getStats().get(FilterOperationCodes.BEFORE_TAKE));
    }
}

