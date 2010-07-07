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

package org.openspaces.itest.remoting.simple.plain;

import com.gigaspaces.async.AsyncFuture;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.support.WaitForAnyListener;
import org.openspaces.remoting.EventDrivenRemotingProxyConfigurer;
import org.openspaces.remoting.EventDrivenSpaceRemotingEntry;
import org.openspaces.remoting.ExecutorRemotingProxyConfigurer;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author kimchy
 */
public class SimpleRemotingTests extends AbstractDependencyInjectionSpringContextTests {

    protected SimpleService simpleService;

    protected SimpleServiceAsync simpleServiceAsync;

    protected SimpleService simpleServiceExecutor;

    protected SimpleAnnotationBean simpleAnnotationBean;

    protected GigaSpace gigaSpace;

    public SimpleRemotingTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/remoting/simple/plain/simple-remoting.xml"};
    }


    public void testAsyncSyncExecution() {
        String reply = simpleService.say("test");
        assertEquals("SAY test", reply);
    }

    public void testAsyncSyncExecutionWithException() {
        try {
            simpleService.testException();
            fail();
        } catch (SimpleService.MyException e) {
            // all is well
        }
    }

    public void testAsyncAsyncExecutionWithException() {
        try {
            simpleService.asyncTestException().get();
            fail();
        } catch (InterruptedException e) {
            fail();
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof SimpleService.MyException)) {
                fail();
            }
        }
    }

    public void testAsyncExecutionIsDone() throws InterruptedException, ExecutionException {
        Future result = simpleService.asyncSay("test");
        while (!result.isDone()) {
            Thread.sleep(1000);
        }
        assertEquals("SAY test", result.get());
    }

    public void testAsyncExecutionGet() throws ExecutionException, InterruptedException {
        Future result = simpleService.asyncSay("test");
        assertEquals("SAY test", result.get());
    }

    public void testAsyncExecutionGetWithAsyncInterface() throws ExecutionException, InterruptedException {
        Future result = simpleServiceAsync.say("test");
        assertEquals("SAY test", result.get());
    }

    public void testExecutorSyncExecution() {
        String reply = simpleServiceExecutor.say("test");
        assertEquals("SAY test", reply);
    }

    public void testExecutorAsyncExecution() throws Exception {
        Future<String> reply = simpleServiceExecutor.asyncSay("test");
        assertEquals("SAY test", reply.get());
    }

    public void testExecutorAsyncExecutionWithListener() throws Exception {
        AsyncFuture<String> reply = (AsyncFuture<String>) simpleServiceExecutor.asyncSay("test");
        WaitForAnyListener<String> listener = new WaitForAnyListener<String>(1);
        reply.setListener(listener);
        assertEquals("SAY test", listener.waitForResult());
    }

    public void testExecutorSyncExecutionWithException() {
        try {
            simpleServiceExecutor.testException();
            fail();
        } catch (SimpleService.MyException e) {
            // all is well
        }
    }

    public void testExecutorAsyncExecutionWithException() {
        try {
            simpleServiceExecutor.asyncTestException().get();
            fail();
        } catch (InterruptedException e) {
            fail();
        } catch (ExecutionException e) {
            if (!(e.getCause() instanceof SimpleService.MyException)) {
                fail();
            }
        }
    }

    public void testSimpleAnnotationExecution() {
        String reply = simpleAnnotationBean.eventSimpleService.say("test");
        assertEquals("SAY test", reply);
        reply = simpleAnnotationBean.executorSimpleService.say("test");
        assertEquals("SAY test", reply);
    }

    public void testWiredParameters() {
        assertTrue(simpleAnnotationBean.executorSimpleService.wire(new WiredParameter()));
        assertTrue(simpleAnnotationBean.eventSimpleService.wire(new WiredParameter()));
    }

    public void testSimpleConfigurerExecution() {
        SimpleService localSyncSimpleService = new ExecutorRemotingProxyConfigurer<SimpleService>(gigaSpace, SimpleService.class)
                .proxy();
        String reply = localSyncSimpleService.say("test");
        assertEquals("SAY test", reply);
        SimpleService localAsyncSimpleService = new EventDrivenRemotingProxyConfigurer<SimpleService>(gigaSpace, SimpleService.class)
                .timeout(15000)
                .proxy();
        reply = localAsyncSimpleService.say("test");
        assertEquals("SAY test", reply);
    }

    public void testSerializationOfAsyncRemotingEntry() throws IOException, ClassNotFoundException {
        EventDrivenSpaceRemotingEntry entry = new EventDrivenSpaceRemotingEntry();
        entry = entry.buildInvocation("test", "test", null);
        entry.oneWay = true;
        entry.metaArguments = new Object[]{new Integer(1)};
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream);
        oos.writeObject(entry);
        oos.flush();
        byte[] bytes = byteArrayOutputStream.toByteArray();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(byteArrayInputStream);
        EventDrivenSpaceRemotingEntry invocation = (EventDrivenSpaceRemotingEntry) ois.readObject();
        compareAsyncInvocationNullableFields(entry, invocation);

        entry = entry.buildResult("result");
        entry.instanceId = 1;
        byteArrayOutputStream = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(byteArrayOutputStream);
        oos.writeObject(entry);
        oos.flush();
        bytes = byteArrayOutputStream.toByteArray();
        byteArrayInputStream = new ByteArrayInputStream(bytes);
        ois = new ObjectInputStream(byteArrayInputStream);
        EventDrivenSpaceRemotingEntry invocationResult = (EventDrivenSpaceRemotingEntry) ois.readObject();
        compareAsyncResultNullableFields(entry, invocationResult);
    }

    private void compareAsyncResultNullableFields(EventDrivenSpaceRemotingEntry entry, EventDrivenSpaceRemotingEntry invocationResult) {
        assertEquals(entry.getRouting(), invocationResult.getRouting());
        assertEquals(entry.getResult(), invocationResult.getResult());
        assertEquals(entry.getInstanceId(), invocationResult.getInstanceId());
        assertEquals(entry.getException(), invocationResult.getException());
    }

    private void compareAsyncInvocationNullableFields(EventDrivenSpaceRemotingEntry entry, EventDrivenSpaceRemotingEntry invocation) {
        assertEquals(entry.getRouting(), invocation.getRouting());
        assertEquals(entry.oneWay, invocation.oneWay);
        Object[] entryMetaArgs = entry.getMetaArguments();
        if (entryMetaArgs != null) {
            Object[] invocationMetaArgs = invocation.getMetaArguments();
            for (int i = 0; i < entryMetaArgs.length; i++) {
                assertEquals(entryMetaArgs[i], invocationMetaArgs[i]);
            }
        } else {
            assertNull(invocation.getMetaArguments());
        }
        Object[] entryArgs = entry.getArguments();
        if (entryArgs != null) {
            Object[] invocationArgs = invocation.getArguments();
            for (int i = 0; i < entryArgs.length; i++) {
                assertEquals(entryArgs[i], invocationArgs[i]);
            }
        } else {
            assertNull(invocation.getArguments());
        }
    }
}