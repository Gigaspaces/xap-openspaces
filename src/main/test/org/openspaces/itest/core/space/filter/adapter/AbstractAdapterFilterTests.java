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

package org.openspaces.itest.core.space.filter.adapter;

import com.j_spaces.core.filters.FilterOperationCodes;
import com.j_spaces.core.filters.entry.ISpaceFilterEntry;
import org.openspaces.core.GigaSpace;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kimchy
 */
public abstract class AbstractAdapterFilterTests extends AbstractDependencyInjectionSpringContextTests {

    protected SimpleFilter simpleFilter;

    protected GigaSpace gigaSpace;

    public AbstractAdapterFilterTests() {
        setPopulateProtectedVariables(true);
    }

    protected void onSetUp() throws Exception {
        gigaSpace.clear(new Object());
        simpleFilter.clearExecutions();
    }

    public void testOnInit() {
        assertTrue(simpleFilter.isOnInitCalled());
    }

    public void testWrite() {
        Message message = new Message("test");
        gigaSpace.write(message);
        assertEquals(1, simpleFilter.getLastExecutions().size());
        Object[] params = simpleFilter.getLastExecutions().get(0);
        assertEquals(1, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());

        simpleFilter.clearExecutions();

        Echo echo = new Echo("test");
        gigaSpace.write(echo);
        assertEquals(1, simpleFilter.getLastExecutions().size());
        params = simpleFilter.getLastExecutions().get(0);
        assertEquals(1, params.length);
        assertEquals("test", ((Echo) params[0]).getMessage());
    }

    public void testRead() throws Exception {
        Message message = new Message("test");
        gigaSpace.read(message);
        assertEquals(1, simpleFilter.getLastExecutions().size());
        Object[] params = simpleFilter.getLastExecutions().get(0);
        assertEquals(1, params.length);
        assertEquals("test", ((Message)((ISpaceFilterEntry) params[0]).getObject(gigaSpace.getSpace())).getMessage());
    }

    public void testTake() {
        Message message = new Message("test");
        gigaSpace.take(message);
        assertEquals(1, simpleFilter.getLastExecutions().size());
        Object[] params = simpleFilter.getLastExecutions().get(0);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.BEFORE_TAKE, params[1]);
    }
}
