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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * @author kimchy
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/core/space/filter/replication/simple-filter.xml")
public class SimpleReplicationFilterTests   { 

     @Autowired protected SimpleReplicationFilter simpleFilter;

     @Autowired protected GigaSpace gigaSpace1;

     @Autowired protected GigaSpace gigaSpace2;

    public SimpleReplicationFilterTests() {
 
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/space/filter/replication/simple-filter.xml"};
    }


     @Test public void testFilter() {
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

