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

package org.openspaces.itest.remoting.methodannotations;

import com.gigaspaces.annotation.pojo.SpaceClass;
import com.gigaspaces.annotation.pojo.SpaceId;
import com.gigaspaces.annotation.pojo.SpaceRouting;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author uri
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/remoting/methodannotations/method-annotations-remoting.xml")
public class ExecutorRemotingMethodAnnotationsTests   { 

     @Autowired protected SimpleExecutorRemotingService executorProxy;

     @Autowired protected GigaSpace gigaSpace;

    public ExecutorRemotingMethodAnnotationsTests() {
 
    }


    //@Override
    protected String[] getConfigLocations () {
        return new String[] {"/org/openspaces/itest/remoting/methodannotations/method-annotations-remoting.xml"};
    }



     @Test public void testTakeMultipleWorking() {
        gigaSpace.write(new Message(1));
        gigaSpace.write(new Message(2));
        Message[] result = gigaSpace.takeMultiple(new Message(), Integer.MAX_VALUE);
        assertEquals(2, result.length);
        gigaSpace.clear(new Message());
    }

     @Test public void testExecutorBroadcastWithInjectedReducer() {
        int value = executorProxy.sumWithInjectedReducer(2);
        assertEquals(4, value);
    }

     @Test public void testExecutorAsyncBroadcastWithInjectedReducer() throws ExecutionException, InterruptedException {
        Future<Integer> result = executorProxy.asyncSumWithInjectedReducer(2);
        assertEquals(new Integer(4), result.get());
    }

     @Test public void testExecutorBroadcastWithReducerType() {
        int value = executorProxy.sumWithReducerType(2);
        assertEquals(4, value);
    }

     @Test public void testAsyncExecutorBroadcastWithReducerType() throws ExecutionException, InterruptedException {
        Future<Integer> result = executorProxy.asyncSumWithReducerType(2);
        assertEquals(new Integer(4), result.get());
    }

     @Test public void testInjectedRoutingHandler() {
        int value = executorProxy.testInjectedRoutingHandler(1);
        assertEquals(value, 1);
    }

     @Test public void testAsyncInjectedRoutingHandler() throws ExecutionException, InterruptedException {
        Future<Integer> result = executorProxy.asyncTestInjectedRoutingHandler(1);
        assertEquals(result.get(), new Integer(1));
    }

     @Test public void testRoutingHandlerType() {
        int value = executorProxy.testRoutingHandlerType(1);
        assertEquals(value, 1);
    }

     @Test public void testAsyncRoutingHandlerType() throws ExecutionException, InterruptedException {
        Future<Integer> result = executorProxy.asyncTestRoutingHandlerType(1);
        assertEquals(result.get(), new Integer(1));
    }

     @Test public void testInjectedInvocationAspect() {
        boolean value = executorProxy.testInjectedInvocationAspect();
        assertTrue(value);
    }

     @Test public void testInvocationAspectType() {
        boolean value = executorProxy.testInvocationAspectType();
        assertTrue(value);
    }

     @Test public void testInjectedMetaArgumentsHandler() {
        boolean value = executorProxy.testInjectedMetaArgumentsHandler();
        assertTrue(value);
    }

     @Test public void testAsyncInjectedMetaArgumentsHandler() throws ExecutionException, InterruptedException {
        Future<Boolean> result = executorProxy.asyncTestInjectedMetaArgumentsHandler();
        assertEquals(result.get(), Boolean.TRUE);
    }


     @Test public void testMetaArgumentsHandlerType() {
        boolean value = executorProxy.testMetaArgumentsHandlerType();
        assertTrue(value);
    }

     @Test public void testAsyncMetaArgumentsHandlerType() throws ExecutionException, InterruptedException {
        Future<Boolean> result = executorProxy.asyncTestMetaArgumentsHandlerType();
        assertEquals(result.get(), Boolean.TRUE);
    }



    @SpaceClass
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/remoting/methodannotations/method-annotations-remoting.xml")
    public static class Message  { 

        private Integer routing;

        public Message() {
        }

        public Message(int routing) {
            this.routing = routing;
        }

        @SpaceRouting
        public Integer getRouting() {
            return routing;
        }

        public void setRouting(Integer routing) {
            this.routing = routing;
        }
        
        private String uid;

        @SpaceId(autoGenerate=true)
        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }
    }
}

