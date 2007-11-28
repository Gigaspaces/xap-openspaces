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

package org.openspaces.itest.core.space.filter.replication;

import com.j_spaces.core.cluster.IReplicationFilter;
import org.openspaces.core.GigaSpace;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kimchy
 */
public class SimpleReplicationFilterTests extends AbstractDependencyInjectionSpringContextTests {

    protected SimpleReplicationFilter simpleFilter;

    protected GigaSpace gigaSpace1;

    protected GigaSpace gigaSpace2;

    public SimpleReplicationFilterTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/space/filter/replication/simple-filter.xml"};
    }


    public void testFilter() {
        assertNotNull(simpleFilter.gigaSpace1);
        assertNotNull(simpleFilter.gigaSpace2);
        assertEquals(2, simpleFilter.initCalled.intValue());
        assertEquals(0, simpleFilter.processEntries.size());

        Message message = new Message();
        message.setMessage("test");
        gigaSpace2.write(message);
        assertEquals(2, simpleFilter.processEntries.size());
        assertEquals(IReplicationFilter.FILTER_DIRECTION_OUTPUT, simpleFilter.processEntries.get(0).direction);
        assertEquals(IReplicationFilter.FILTER_DIRECTION_INPUT, simpleFilter.processEntries.get(1).direction);
    }
}