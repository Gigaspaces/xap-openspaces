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

package org.openspaces.itest.core.space.filter.security;

import com.gigaspaces.async.AsyncFuture;
import com.gigaspaces.security.SecurityException;
import com.j_spaces.core.LeaseContext;
import com.j_spaces.core.client.UpdateModifiers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
import org.openspaces.itest.core.space.filter.AllOperationsFilterUtil;
import org.openspaces.itest.core.space.filter.AllOperationsFilterUtil.MyTask;
import org.openspaces.itest.core.space.filter.adapter.Message;
import org.openspaces.security.spring.SpringSecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;


/**
 * @author kimchy
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/core/space/filter/security/securityFilter.xml")
public class SecurityOperationCodeFilterTest   { 

     @Autowired protected SecurityFilter securityFilterCodeName;
     @Autowired protected SecurityFilter securityFilterCode;
     protected ProviderManager authenticationManager;
     @Autowired protected GigaSpace gigaSpace;
    private SpringSecurityManager securityManager;
    protected SecurityFilter[] filters = new SecurityFilter[2];
    
    public SecurityOperationCodeFilterTest() {
 
    }

    @BeforeClass
    public static void setUp()
    {
        System.setProperty("com.gs.security.properties-file", "org/openspaces/itest/core/space/filter/security/spring-security.properties");

    }
    //@Override
    protected String[] getConfigLocations () {
        return new String[]{"/org/openspaces/itest/core/space/filter/security/securityFilter.xml"};
    }

    @Before
    public void beforeTest(){

        filters[0] = securityFilterCodeName;
        filters[1] = securityFilterCode;
        
        gigaSpace.takeMultiple(new Message());
        AllOperationsFilterUtil.restartStats(filters);
        AllOperationsFilterUtil.initialAssert(securityFilterCodeName.getStats() , "simpleFilterCodeName"); 
        AllOperationsFilterUtil.initialAssert(securityFilterCode.getStats() , "simpleFilterCode"); 
    }
    
     @Test public void testWrite() throws SecurityException, RemoteException {
        beforeTest();
        Message message = new Message(1);
        LeaseContext<Message> lease = gigaSpace.write(message);
        assertNotNull(lease);
        
        AllOperationsFilterUtil.assertAfterWriteWithAuthentication(securityFilterCode.getStats(), "securityFilterCode");
    }
    
     @Test public void testRead() {
        beforeTest();
        Message message = new Message(1);        
        LeaseContext<Message> lease = gigaSpace.write(message);
        assertNotNull(lease);
        AllOperationsFilterUtil.restartStats(filters);
        message = gigaSpace.read(message);
        assertNotNull(message);
        
        AllOperationsFilterUtil.assertAfterReadWithAuthentication(securityFilterCode.getStats(), "securityFilterCode");
    }
    
     @Test public void testTake() {
        beforeTest();
        Message message = new Message(1);        
        LeaseContext<Message> lease = gigaSpace.write(message);
        assertNotNull(lease);
        AllOperationsFilterUtil.restartStats(filters);
        message = gigaSpace.take(message);
        assertNotNull(message);
        
        AllOperationsFilterUtil.assertAfterTakeWithAuthentication(securityFilterCode.getStats(), "securityFilterCode");
    }
    
     @Test public void testTakeMultiple(){
        beforeTest();
        Message[] messages = {new Message(1),new Message(2)};
        LeaseContext<Message>[] leases = gigaSpace.writeMultiple(messages ,Integer.MAX_VALUE);
        assertNotNull(leases);
        assertEquals(2, leases.length);
        AllOperationsFilterUtil.restartStats(filters);
        
        messages = gigaSpace.takeMultiple(new Message() ,Integer.MAX_VALUE);
        assertNotSame(new Message[0], messages);
        assertEquals(2, messages.length);
        AllOperationsFilterUtil.assertAfterTakeMultipleWithAuthentication(securityFilterCode.getStats() , "simpleFilterCodeName");
    }
    
     @Test public void testExecute() throws InterruptedException, ExecutionException{
        beforeTest(); 
        
        AsyncFuture<Integer> future = gigaSpace.execute(new MyTask());
        assertEquals(2, future.get().intValue());
        AllOperationsFilterUtil.assertAfterExecuteWithAuthentication(securityFilterCodeName.getStats() , "simpleFilterCodeName");
    }
    
     @Test public void testUpdate(){
        beforeTest();
        Message message = new Message(1);        
        LeaseContext<Message> lease = gigaSpace.write(message);
        assertNotNull(lease);
        AllOperationsFilterUtil.restartStats(filters);
        
        message.setMessage("message");
        lease = gigaSpace.write(message, 1000 * 20, 0, UpdateModifiers.UPDATE_ONLY);
        assertNotNull(lease);
        AllOperationsFilterUtil.assertAfterUpdateWithAuthentication(securityFilterCodeName.getStats() , "simpleFilterCodeName");
    }
}

