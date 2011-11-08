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

package org.openspaces.itest.core.space.filter.security.adapter;

import java.util.concurrent.ExecutionException;

import net.jini.core.entry.UnusableEntryException;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.Task;
import org.openspaces.itest.core.space.filter.AllOperationsFilterUtil.MyTask;
import org.openspaces.itest.core.space.filter.adapter.CustomFilter;
import org.openspaces.itest.core.space.filter.adapter.Message;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import com.gigaspaces.async.AsyncFuture;
import com.j_spaces.core.LeaseContext;
import com.j_spaces.core.SpaceContext;
import com.j_spaces.core.client.UpdateModifiers;
import com.j_spaces.core.filters.FilterOperationCodes;
import com.j_spaces.core.filters.entry.ISpaceFilterEntry;

/**
 * @author gal
 */
public class AbstractSecurityAdapterCustomFilterTests extends AbstractDependencyInjectionSpringContextTests {

    protected CustomFilter customFilter;

    protected GigaSpace gigaSpace;

    

    public AbstractSecurityAdapterCustomFilterTests(){
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

        assertEquals(3, customFilter.getLastExecutions().size());

        Object[] params = customFilter.getLastExecutions().get(0); // the params of the beforeAuthentication method call
        assertEquals(3, params.length); 
        assertNotNull((SpaceContext)params[0]);
        assertEquals(FilterOperationCodes.BEFORE_AUTHENTICATION, params[2]);        
        
        params = customFilter.getLastExecutions().get(1); // the params of the beforeWrite method call
        assertEquals(2, params.length);
        assertEquals("changed at beforeWrite", ((Message)((ISpaceFilterEntry)params[0]).getObject(gigaSpace.getSpace())).getMessage());
        assertEquals(FilterOperationCodes.BEFORE_WRITE, params[1]);

        params = customFilter.getLastExecutions().get(2); // the params of the AfterWrite method call
        assertEquals(2, params.length);
        assertEquals("changed at beforeWrite", ((Message)((ISpaceFilterEntry)params[0]).getObject(gigaSpace.getSpace())).getMessage());
        assertEquals(FilterOperationCodes.AFTER_WRITE, params[1]);
        assertEquals("changed at beforeWrite", gigaSpace.readMultiple(new Message())[0].getMessage());
        gigaSpace.clear(null);
    }

    public void testRead() throws UnusableEntryException {
        Message message = new Message(1);
        message.setMessage("test1");
        gigaSpace.write(message);

        Message message2 = new Message(1);
        message2.setMessage("test2");
        gigaSpace.write(message2);
        customFilter.clearExecutions();
        
        assertNotNull(gigaSpace.read(new Message("test1")));

        assertEquals(3, customFilter.getLastExecutions().size());
        
        Object[] params = customFilter.getLastExecutions().get(0);
        assertEquals(3, params.length);
        assertNotNull((SpaceContext)params[0]);
        assertEquals(FilterOperationCodes.BEFORE_AUTHENTICATION, params[2]);

        params = customFilter.getLastExecutions().get(1);
        assertEquals(2, params.length);
        assertEquals("test1", ((Message)((ISpaceFilterEntry)params[0]).getObject(gigaSpace.getSpace())).getMessage());
        assertEquals(FilterOperationCodes.BEFORE_READ, params[1]);

        params = customFilter.getLastExecutions().get(2);
        assertEquals(2, params.length);
        assertEquals(FilterOperationCodes.AFTER_READ, params[2]);
        
        gigaSpace.clear(null);
    }
    
    public void testTake() throws UnusableEntryException {
        Message message = new Message(1);
        message.setMessage("test1");
        gigaSpace.write(message);
        customFilter.clearExecutions();
        
        assertNotNull(gigaSpace.take(new Message()));
        
        assertEquals(3, customFilter.getLastExecutions().size());
        
        Object[] params = customFilter.getLastExecutions().get(0);
        assertEquals(3, params.length);
        assertNotNull((SpaceContext)params[0]);
        assertEquals(FilterOperationCodes.BEFORE_AUTHENTICATION, params[2]);

        params = customFilter.getLastExecutions().get(1);
        assertEquals(2, params.length);
        assertEquals("test1", ((Message)((ISpaceFilterEntry)params[0]).getObject(gigaSpace.getSpace())).getMessage());
        assertEquals(FilterOperationCodes.BEFORE_TAKE, params[1]);

        params = customFilter.getLastExecutions().get(2);
        assertEquals(2, params.length);
        assertEquals("changed at afterRead", ((Message)((ISpaceFilterEntry)params[0]).getObject(gigaSpace.getSpace())).getMessage());
        assertEquals(FilterOperationCodes.AFTER_TAKE, params[2]);
        
        gigaSpace.clear(null);
    }
    
    public void testTakeMultiple() throws UnusableEntryException{
        Message[] messages = {new Message(1),new Message(2)};
        LeaseContext<Message>[] leases = gigaSpace.writeMultiple(messages ,Integer.MAX_VALUE);
        assertNotNull(leases);
        assertEquals(2, leases.length);
        customFilter.clearExecutions();
        
        messages = gigaSpace.takeMultiple(new Message() ,Integer.MAX_VALUE);
        assertNotSame(new Message[0], messages);
        assertEquals(2, messages.length);
        
        assertEquals(4, customFilter.getLastExecutions().size());
        
        Object[] params = customFilter.getLastExecutions().get(0);
        assertEquals(3, params.length);
        assertNotNull((SpaceContext)params[0]);
        assertEquals(FilterOperationCodes.BEFORE_AUTHENTICATION, params[2]);
        
        params = customFilter.getLastExecutions().get(1);
        assertEquals(2, params.length);
        assertNotNull((Message)((ISpaceFilterEntry)params[0]).getObject(gigaSpace.getSpace()));
        assertEquals(FilterOperationCodes.BEFORE_TAKE_MULTIPLE, params[1]);
        
        params = customFilter.getLastExecutions().get(2);
        assertEquals(2, params.length);
        assertNotNull((Message)((ISpaceFilterEntry)params[0]).getObject(gigaSpace.getSpace()));
        assertEquals(FilterOperationCodes.AFTER_TAKE_MULTIPLE, params[1]);
        
        params = customFilter.getLastExecutions().get(3);
        assertEquals(2, params.length);
        assertNotNull((Message)((ISpaceFilterEntry)params[0]).getObject(gigaSpace.getSpace()));
        assertEquals(FilterOperationCodes.AFTER_TAKE_MULTIPLE, params[1]);
        
        gigaSpace.clear(null);
    }
    
    public void testExecute() throws InterruptedException, ExecutionException, UnusableEntryException{
        AsyncFuture<Integer> future = gigaSpace.execute(new MyTask());
        assertEquals(2, future.get().intValue());
        
        assertEquals(3, customFilter.getLastExecutions().size());
        
        Object[] params = customFilter.getLastExecutions().get(0); 
        assertEquals(3, params.length); 
        assertNotNull((SpaceContext)params[0]);
        assertEquals(FilterOperationCodes.BEFORE_AUTHENTICATION, params[2]);        
        
        params = customFilter.getLastExecutions().get(1);
        assertEquals(2, params.length);
        assertNotNull((Task<Integer>)((ISpaceFilterEntry)params[0]).getObject(gigaSpace.getSpace()));
        assertEquals(FilterOperationCodes.BEFORE_EXECUTE, params[1]);

        params = customFilter.getLastExecutions().get(2); 
        assertEquals(2, params.length);
        assertNotNull((Task<Integer>)((ISpaceFilterEntry)params[0]).getObject(gigaSpace.getSpace()));
        assertEquals(FilterOperationCodes.AFTER_EXECUTE, params[1]);
    }
    
    public void testUpdate() throws UnusableEntryException{      
        Message message = new Message(1);        
        LeaseContext<Message> lease = gigaSpace.write(message);
        assertNotNull(lease);
        customFilter.clearExecutions();
        
        message.setMessage("message");
        lease = gigaSpace.write(message, 1000 * 20, 0, UpdateModifiers.UPDATE_ONLY);
        assertNotNull(lease);
        
        assertEquals(3, customFilter.getLastExecutions().size());
        
        Object[] params = customFilter.getLastExecutions().get(0); 
        assertEquals(3, params.length); 
        assertNotNull((SpaceContext)params[0]);
        assertEquals(FilterOperationCodes.BEFORE_AUTHENTICATION, params[2]);        
        
        params = customFilter.getLastExecutions().get(1);
        assertEquals(2, params.length);
        assertNotNull((Message)((ISpaceFilterEntry)params[0]).getObject(gigaSpace.getSpace()));
        assertEquals(FilterOperationCodes.BEFORE_UPDATE, params[1]);

        params = customFilter.getLastExecutions().get(2); 
        assertEquals(2, params.length);
        assertNotNull((Message)((ISpaceFilterEntry)params[0]).getObject(gigaSpace.getSpace()));
        assertEquals(FilterOperationCodes.AFTER_UPDATE, params[1]);
        
        gigaSpace.clear(null);
    }
}