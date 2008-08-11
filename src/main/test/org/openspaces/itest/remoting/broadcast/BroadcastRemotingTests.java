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

package org.openspaces.itest.remoting.broadcast;

import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author kimchy
 */
public class BroadcastRemotingTests extends AbstractDependencyInjectionSpringContextTests {

    protected SimpleService syncService;

    protected SimpleService executorService;

    public BroadcastRemotingTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/remoting/broadcast/broadcast-remoting.xml"};
    }

    public void testExecutorSyncBroadcast() {
        innerTestSyncBroadcast(executorService);
    }

    public void testSyncSyncBroadcast() {
        innerTestSyncBroadcast(syncService);
    }

    public void testExecutorAsyncBroadcast() throws ExecutionException, InterruptedException {
        innerTestAsyncBroadcast(executorService);
    }

    public void testSyncAsyncBroadcast() throws ExecutionException, InterruptedException {
        innerTestAsyncBroadcast(syncService);
    }

    private void innerTestSyncBroadcast(SimpleService service) {
        int value = service.sum(2);
        assertEquals(4, value);
    }

    private void innerTestAsyncBroadcast(SimpleService service) throws ExecutionException, InterruptedException {
        Future<Integer> future = service.asyncSum(2);
        assertEquals(4, (int) future.get());
    }
}