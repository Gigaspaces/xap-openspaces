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

import java.util.concurrent.ExecutionException;

import org.openspaces.core.GigaSpace;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.notify.SimpleNotifyContainerConfigurer;
import org.openspaces.events.notify.SimpleNotifyEventListenerContainer;
import org.openspaces.itest.core.space.filter.AllOperationsFilterUtil.MyTask;
import org.openspaces.itest.core.space.filter.adapter.Message;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import com.gigaspaces.async.AsyncFuture;
import com.j_spaces.core.LeaseContext;
import com.j_spaces.core.client.UpdateModifiers;

/**
 * @author gal
 */
public class AllOperationsFilterTest extends AbstractDependencyInjectionSpringContextTests {
    
 
    protected SimpleFilter simpleFilterCode;
    protected SimpleFilter simpleFilterCodeName;
    protected SimpleFilter[] filters = new SimpleFilter[2];
    protected GigaSpace gigaSpace;

    public AllOperationsFilterTest() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/space/filter/all-filter.xml"};
    }
    
    public void beforeTest(){       
        filters[0] = simpleFilterCodeName;
        filters[1] = simpleFilterCode;
        
        gigaSpace.takeMultiple(new Message());
        AllOperationsFilterUtil.restartStats(filters);
        AllOperationsFilterUtil.initialAssert(simpleFilterCodeName.getStats() , "simpleFilterCodeName"); 
        AllOperationsFilterUtil.initialAssert(simpleFilterCode.getStats() , "simpleFilterCode"); 
    }
    
    public void testWrite(){
        beforeTest();
        Message message = new Message(1);        
        
        LeaseContext<Message> lease = gigaSpace.write(message);
        assertNotNull(lease);
        AllOperationsFilterUtil.assertAfterWrite(simpleFilterCodeName.getStats() , "simpleFilterCodeName");
        AllOperationsFilterUtil.assertAfterWrite(simpleFilterCode.getStats() , "simpleFilterCode");
    }
    
    public void testRead(){
        beforeTest();
        Message message = new Message(1);              
        LeaseContext<Message> lease = gigaSpace.write(message);
        assertNotNull(lease);
        AllOperationsFilterUtil.restartStats(filters);
        
        message = gigaSpace.read(message);
        assertNotNull(message);
        AllOperationsFilterUtil.assertAfterRead(simpleFilterCodeName.getStats() , "simpleFilterCodeName");
        AllOperationsFilterUtil.assertAfterRead(simpleFilterCode.getStats() , "simpleFilterCode");
     }
    
    public void testTake(){ 
        beforeTest();
        Message message = new Message(1);        
        LeaseContext<Message> lease = gigaSpace.write(message);
        assertNotNull(lease);
        AllOperationsFilterUtil.restartStats(filters);
        
        Message takenMessage = gigaSpace.take(message);
        assertNotNull(takenMessage);
        AllOperationsFilterUtil.assertAfterTake(simpleFilterCodeName.getStats() , "simpleFilterCodeName");
        AllOperationsFilterUtil.assertAfterTake(simpleFilterCode.getStats() , "simpleFilterCode");
    }
    

    public void testUpdate(){
        beforeTest();
        Message message = new Message(1);        
        LeaseContext<Message> lease = gigaSpace.write(message);
        assertNotNull(lease);
        AllOperationsFilterUtil.restartStats(filters);
        
        message.setMessage("message");
        lease = gigaSpace.write(message, 1000 * 20, 0, UpdateModifiers.UPDATE_ONLY);
        assertNotNull(lease);
        AllOperationsFilterUtil.assertAfterUpdate(simpleFilterCodeName.getStats() , "simpleFilterCodeName");
        AllOperationsFilterUtil.assertAfterUpdate(simpleFilterCode.getStats() , "simpleFilterCode");
    }
    
    public void testReadMultiple(){
        beforeTest(); 
        Message[] messages = {new Message(1),new Message(2)};        
        LeaseContext<Message>[] leases = gigaSpace.writeMultiple(messages);
        assertNotNull(leases);
        assertEquals(2, leases.length);
        AllOperationsFilterUtil.restartStats(filters);
        
        messages = gigaSpace.readMultiple(new Message() ,Integer.MAX_VALUE);
        assertNotSame(new Message[0], messages);
        assertEquals(2, messages.length);
        AllOperationsFilterUtil.assertAfterReadMultiple(simpleFilterCodeName.getStats() , "simpleFilterCodeName");
        AllOperationsFilterUtil.assertAfterReadMultiple(simpleFilterCode.getStats() , "simpleFilterCode");
    }
    
    public void testTakeMultiple(){
        beforeTest();
        Message[] messages = {new Message(1),new Message(2)};
        LeaseContext<Message>[] leases = gigaSpace.writeMultiple(messages ,Integer.MAX_VALUE);
        assertNotNull(leases);
        assertEquals(2, leases.length);
        AllOperationsFilterUtil.restartStats(filters);
        
        messages = gigaSpace.takeMultiple(new Message() ,Integer.MAX_VALUE);
        assertNotSame(new Message[0], messages);
        assertEquals(2, messages.length);
        AllOperationsFilterUtil.assertAfterTakeMultiple(simpleFilterCodeName.getStats() , "simpleFilterCodeName");
        AllOperationsFilterUtil.assertAfterTakeMultiple(simpleFilterCode.getStats() , "simpleFilterCode");    
    }
    
    public void testExecute() throws InterruptedException, ExecutionException{
        beforeTest(); 
        
        AsyncFuture<Integer> future = gigaSpace.execute(new MyTask());
        assertEquals(2, future.get().intValue());
        AllOperationsFilterUtil.assertAfterExecute(simpleFilterCodeName.getStats() , "simpleFilterCodeName");
        AllOperationsFilterUtil.assertAfterExecute(simpleFilterCode.getStats() , "simpleFilterCode");
    }
    
    public void testNotify() throws InterruptedException, ExecutionException{
        beforeTest();
        Message message = new Message(1);
        message.setMessage("hello");
        
        SimpleNotifyEventListenerContainer notifyEventListenerContainer = new SimpleNotifyContainerConfigurer(gigaSpace)
        .template(message)
        .eventListenerAnnotation(new Object() {
            @SpaceDataEvent
            public void gotAnEvent(Message message) {
                System.out.println(message);
            }
        }).notifyContainer();
        notifyEventListenerContainer.start();
        
        LeaseContext<Message> lease = gigaSpace.write(message);
        assertNotNull(lease);        
        Thread.sleep(5000);
        AllOperationsFilterUtil.assertAfterNotify(simpleFilterCodeName.getStats() , "simpleFilterCodeName");
        AllOperationsFilterUtil.assertAfterNotify(simpleFilterCode.getStats() , "simpleFilterCode");
    }
   

   
}
