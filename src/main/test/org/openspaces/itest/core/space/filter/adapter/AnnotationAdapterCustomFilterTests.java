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
import net.jini.core.entry.UnusableEntryException;
import org.openspaces.core.GigaSpace;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kobi
 */
public class AnnotationAdapterCustomFilterTests extends AbstractDependencyInjectionSpringContextTests {

    protected CustomFilter customFilter;

    protected GigaSpace gigaSpace;

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/space/filter/adapter/adapter-annotation-custom-filter.xml"};
    }

    public AnnotationAdapterCustomFilterTests(){
        setPopulateProtectedVariables(true);
    }


    protected void onSetUp() throws Exception {
        gigaSpace.clear(new Object());
        customFilter.clearExecutions();
    }

    public void testOnInit() {
        assertTrue(customFilter.isOnInitCalled());
    }

    public void testWrite() throws UnusableEntryException {
        Message message = new Message(1);
        message.setMessage("test");
        gigaSpace.write(message);

        assertEquals(2, customFilter.getLastExecutions().size());

        Object[] params = customFilter.getLastExecutions().get(0);
        assertEquals(2, params.length);
        assertEquals("test", ((Message)((ISpaceFilterEntry)params[0]).getObject(gigaSpace.getSpace())).getMessage());
        assertEquals(FilterOperationCodes.BEFORE_WRITE, params[1]);

        params = customFilter.getLastExecutions().get(1);
        assertEquals(2, params.length);
        assertEquals("test", ((Message)((ISpaceFilterEntry)params[0]).getObject(gigaSpace.getSpace())).getMessage());
        assertEquals(FilterOperationCodes.AFTER_WRITE, params[1]);
        assertEquals("test", gigaSpace.readMultiple(new Message())[0].getMessage());
        gigaSpace.clear(null);
    }

    public void testRead() throws UnusableEntryException {
        Message message = new Message(1);
        message.setMessage("test");
        gigaSpace.write(message);

        Message message2 = new Message(1);
        message2.setMessage("test2");
        gigaSpace.write(message2);
        customFilter.clearExecutions();

        assertNotNull(gigaSpace.read(new Message("test2")));

        assertEquals(2, customFilter.getLastExecutions().size());

        Object[] params = customFilter.getLastExecutions().get(0);
        assertEquals(2, params.length);
        assertEquals("test2", ((Message)((ISpaceFilterEntry)params[0]).getObject(gigaSpace.getSpace())).getMessage());
        assertEquals(FilterOperationCodes.BEFORE_READ, params[1]);

        params = customFilter.getLastExecutions().get(1);
        assertEquals(2, params.length);
        assertEquals("test2", ((Message)((ISpaceFilterEntry)params[0]).getObject(gigaSpace.getSpace())).getMessage());
        assertEquals(FilterOperationCodes.AFTER_READ, params[1]);
        Message[] msgs = gigaSpace.readMultiple(new Message());
        for(Message m : msgs){
            assertEquals("test2", m.getMessage());
        }

        gigaSpace.clear(null);
    }

}