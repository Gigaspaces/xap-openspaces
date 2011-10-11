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

import com.gigaspaces.annotation.pojo.SpaceRouting;
import com.gigaspaces.async.AsyncFuture;
import com.j_spaces.core.client.ReadModifiers;
import com.j_spaces.core.client.UpdateModifiers;
import com.j_spaces.core.filters.FilterOperationCodes;
import com.j_spaces.core.filters.entry.ExecutionFilterEntry;
import junit.framework.Assert;
import net.jini.core.lease.Lease;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.Task;
import org.openspaces.events.adapter.SpaceDataEvent;
import org.openspaces.events.notify.SimpleNotifyContainerConfigurer;
import org.openspaces.events.notify.SimpleNotifyEventListenerContainer;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public abstract class AbstractAdapterFilterTests extends AbstractDependencyInjectionSpringContextTests {

    protected SimpleFilter simpleFilter;

    protected GigaSpace gigaSpace;

    protected GigaSpace txnGigaSpace;

    protected PlatformTransactionManager mahaloTxManager;

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
        Message message = new Message(1);
        message.setMessage("test");
        gigaSpace.write(message);

        assertEquals(2, simpleFilter.getLastExecutions().size());

        Object[] params = simpleFilter.getLastExecutions().get(0);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.BEFORE_WRITE, params[1]);

        params = simpleFilter.getLastExecutions().get(1);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.AFTER_WRITE, params[1]);
        gigaSpace.clear(null);
        simpleFilter.getLastExecutions().clear();
        gigaSpace.clear(null);
    }

    public void testNotify() throws Exception {
        SimpleNotifyEventListenerContainer notifyEventListenerContainer = new SimpleNotifyContainerConfigurer(gigaSpace)
                .template(new Message())
                .eventListenerAnnotation(new Object() {
                    @SpaceDataEvent
                    public void gotAnEvent(Message message) {
                        System.out.println(message);
                    }
                }).notifyContainer();
        notifyEventListenerContainer.start();
        int size = simpleFilter.getLastExecutions().size();
        assertEquals(1, size);
        assertNull(((Message) simpleFilter.getLastExecutions().get(size - 1)[0]).getMessage());    //template
        assertEquals("BEFORE_NOTIFY", simpleFilter.getLastExecutions().get(size - 1)[1]);
        simpleFilter.getLastExecutions().clear();
        Message message = new Message(1);
        message.setMessage("test");
        gigaSpace.write(message);
        Thread.sleep(5000);
        size = simpleFilter.getLastExecutions().size();
        assertEquals(4, size);   //beforeWrite + beforeNotifyTrigger + afterNotifyTrigger
        Object[] params = simpleFilter.getLastExecutions().get(0);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.BEFORE_WRITE, params[1]);

        params = simpleFilter.getLastExecutions().get(1);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.AFTER_WRITE, params[1]);

        params = simpleFilter.getLastExecutions().get(2);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals(null, ((Message) params[1]).getMessage());
        assertEquals("BEFORE_NOTIFY_TRIGGER", params[2]);

        params = simpleFilter.getLastExecutions().get(3);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals(null, ((Message) params[1]).getMessage());
        assertEquals("AFTER_NOTIFY_TRIGGER", params[2]);

        notifyEventListenerContainer.destroy();
        gigaSpace.clear(null);
    }

    public void testUpdate() {
        Message message = new Message(1);
        message.setMessage("test");
        message.setData("1");
        gigaSpace.write(message);
        assertEquals(2, simpleFilter.getLastExecutions().size());
        Object[] params = simpleFilter.getLastExecutions().get(0);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        Message readMsg = gigaSpace.read(new Message("test"));
        readMsg.setData("2");
        simpleFilter.clearExecutions();
        gigaSpace.write(readMsg, Lease.FOREVER, 0, UpdateModifiers.UPDATE_ONLY);
        assertEquals(2, simpleFilter.getLastExecutions().size());
        params = simpleFilter.getLastExecutions().get(0);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals("2", ((Message) params[0]).getData());
        assertEquals(FilterOperationCodes.BEFORE_UPDATE, params[1]);
        params = simpleFilter.getLastExecutions().get(1);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals("1", ((Message) params[0]).getData());
        assertEquals("test", ((Message) params[1]).getMessage());
        assertEquals("2", ((Message) params[1]).getData());
        assertEquals(FilterOperationCodes.AFTER_UPDATE, params[2]);
        gigaSpace.clear(null);
    }

    public void testUpdateOrWriteUpdateWithTimeout() {
        Message message = new Message(1);
        message.setMessage("test");
        message.setData("1");
        gigaSpace.write(message);
        assertEquals(2, simpleFilter.getLastExecutions().size());
        Object[] params = simpleFilter.getLastExecutions().get(0);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        Message readMsg = gigaSpace.read(new Message("test"));
        readMsg.setData("2");
        simpleFilter.clearExecutions();
        gigaSpace.write(readMsg, Lease.FOREVER, 10000, UpdateModifiers.UPDATE_OR_WRITE);
        assertEquals(2, simpleFilter.getLastExecutions().size());
        params = simpleFilter.getLastExecutions().get(0);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals("2", ((Message) params[0]).getData());
        assertEquals(FilterOperationCodes.BEFORE_UPDATE, params[1]);
        params = simpleFilter.getLastExecutions().get(1);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals("1", ((Message) params[0]).getData());
        assertEquals("test", ((Message) params[1]).getMessage());
        assertEquals("2", ((Message) params[1]).getData());
        assertEquals(FilterOperationCodes.AFTER_UPDATE, params[2]);
        gigaSpace.clear(null);
    }

    public void testUpdateOrWriteWriteWithTimeout() {
        Message message = new Message(1);
        message.setMessage("test");
        message.setData("1");
        gigaSpace.write(message, Lease.FOREVER, 10000, UpdateModifiers.UPDATE_OR_WRITE);
        assertEquals(2, simpleFilter.getLastExecutions().size());
        Object[] params = simpleFilter.getLastExecutions().get(0);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals("1", ((Message) params[0]).getData());
        assertEquals(FilterOperationCodes.BEFORE_WRITE, params[1]);
        params = simpleFilter.getLastExecutions().get(1);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals("1", ((Message) params[0]).getData());
        assertEquals(FilterOperationCodes.AFTER_WRITE, params[1]);
        gigaSpace.clear(null);
    }

    public void testUpdateOrWriteWithTimeoutWhenObjectIsMissing() throws Exception {
        final Message message = new Message(1);
        message.setMessage("test");
        message.setData("1");
        gigaSpace.write(message);

        final Message readMsg = gigaSpace.read(new Message("test"));
        readMsg.setData("2");


        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus status = mahaloTxManager.getTransaction(definition);
        txnGigaSpace.read(message, 10000, ReadModifiers.EXCLUSIVE_READ_LOCK);
        simpleFilter.clearExecutions();

        new Thread(new Runnable() {
            @Override
            public void run() {
                gigaSpace.write(readMsg, Lease.FOREVER, 10000, UpdateModifiers.UPDATE_OR_WRITE);
            }
        }).start();
        Thread.sleep(2000);
        mahaloTxManager.commit(status);
        Thread.sleep(2000);

        assertEquals(2, simpleFilter.getLastExecutions().size());

        Object[] params = simpleFilter.getLastExecutions().get(0);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals("2", ((Message) params[0]).getData());
        assertEquals(FilterOperationCodes.BEFORE_UPDATE, params[1]);
        params = simpleFilter.getLastExecutions().get(1);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals("1", ((Message) params[0]).getData());
        assertEquals("test", ((Message) params[1]).getMessage());
        assertEquals("2", ((Message) params[1]).getData());
        assertEquals(FilterOperationCodes.AFTER_UPDATE, params[2]);
        gigaSpace.clear(null);
    }

    public void testUpdateWithTimeoutWhenObjectIsMissing() throws Exception {
        final Message message = new Message(1);
        message.setMessage("test");
        message.setData("1");
        gigaSpace.write(message);

        final Message readMsg = gigaSpace.read(new Message("test"));
        readMsg.setData("2");


        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        TransactionStatus status = mahaloTxManager.getTransaction(definition);
        txnGigaSpace.read(message, 10000, ReadModifiers.EXCLUSIVE_READ_LOCK);
        simpleFilter.clearExecutions();

        new Thread(new Runnable() {
            @Override
            public void run() {
                gigaSpace.write(readMsg, Lease.FOREVER, 10000, UpdateModifiers.UPDATE_ONLY);
            }
        }).start();
        Thread.sleep(2000);
        mahaloTxManager.commit(status);
        Thread.sleep(2000);
        assertEquals(2, simpleFilter.getLastExecutions().size());

        Object[] params = simpleFilter.getLastExecutions().get(0);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals("2", ((Message) params[0]).getData());
        assertEquals(FilterOperationCodes.BEFORE_UPDATE, params[1]);
        params = simpleFilter.getLastExecutions().get(1);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals("1", ((Message) params[0]).getData());
        assertEquals("test", ((Message) params[1]).getMessage());
        assertEquals("2", ((Message) params[1]).getData());
        assertEquals(FilterOperationCodes.AFTER_UPDATE, params[2]);
        gigaSpace.clear(null);
    }

    public void testRead() throws Exception {
        Message message = new Message(1);
        message.setMessage("test");
        gigaSpace.write(message);
        simpleFilter.getLastExecutions().clear();
        gigaSpace.read(message);

        assertEquals(2, simpleFilter.getLastExecutions().size());
        Object[] params = simpleFilter.getLastExecutions().get(0);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.BEFORE_READ, params[1]);

        params = simpleFilter.getLastExecutions().get(1);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.AFTER_READ, params[1]);

        gigaSpace.clear(null);
    }

    public void testReadWithTimeout() throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message(1);
                message.setMessage("test");
                System.out.println(gigaSpace.read(message, 100000));
            }
        }).start();
        Thread.sleep(2000);
        Message message = new Message(1);
        message.setMessage("test");
        gigaSpace.write(message);

        Thread.sleep(5000);
        assertEquals(4, simpleFilter.getLastExecutions().size());
        Object[] params = simpleFilter.getLastExecutions().get(0);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.BEFORE_READ, params[1]);

        params = simpleFilter.getLastExecutions().get(1);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.BEFORE_WRITE, params[1]);

        params = simpleFilter.getLastExecutions().get(2);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.AFTER_WRITE, params[1]);

        params = simpleFilter.getLastExecutions().get(3);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.AFTER_READ, params[1]);

        gigaSpace.clear(null);
    }

    public void testTake() {
        Message message = new Message(1);
        message.setMessage("test");
        gigaSpace.write(message);
        simpleFilter.clearExecutions();
        gigaSpace.take(message);
        assertEquals(2, simpleFilter.getLastExecutions().size());
        Object[] params = simpleFilter.getLastExecutions().get(0);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.BEFORE_TAKE, params[1]);

        params = simpleFilter.getLastExecutions().get(1);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.AFTER_TAKE, params[1]);
        gigaSpace.clear(null);
    }

    public void testTakeWithTimeout() throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message(1);
                message.setMessage("test");
                System.out.println(gigaSpace.take(message, 100000));
            }
        }).start();
        Thread.sleep(2000);
        Message message = new Message(1);
        message.setMessage("test");
        gigaSpace.write(message);

        Thread.sleep(2000);
        assertEquals(4, simpleFilter.getLastExecutions().size());
        Object[] params = simpleFilter.getLastExecutions().get(0);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.BEFORE_TAKE, params[1]);

        params = simpleFilter.getLastExecutions().get(1);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.BEFORE_WRITE, params[1]);

        params = simpleFilter.getLastExecutions().get(2);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.AFTER_WRITE, params[1]);

        params = simpleFilter.getLastExecutions().get(3);
        assertEquals(2, params.length);
        assertEquals("test", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.AFTER_TAKE, params[1]);

        gigaSpace.clear(null);
    }

    public void testExecute() throws Exception {
        AsyncFuture<String> result = gigaSpace.execute(new MyTask("test"));
        assertEquals("return", result.get(30000, TimeUnit.MILLISECONDS));
        Thread.sleep(2000);
        assertEquals(2, simpleFilter.getLastExecutions().size());
        Object[] params = simpleFilter.getLastExecutions().get(0);

        Assert.assertTrue(((ExecutionFilterEntry) params[0]).getObject(null) instanceof MyTask);

        params = simpleFilter.getLastExecutions().get(1);
        assertEquals("return", ((ExecutionFilterEntry) params[0]).getObject(null));
        assertEquals(1, params.length);
        gigaSpace.clear(null);
    }

    public void testReadMultiple() throws Exception {
        Message message = new Message(1);
        message.setMessage("test1");
        gigaSpace.write(message);

        message = new Message(2);
        message.setMessage("test2");
        gigaSpace.write(message);

        simpleFilter.getLastExecutions().clear();

        Assert.assertEquals(2, gigaSpace.readMultiple(new Message()).length);

        Thread.sleep(3000);
        assertEquals(3, simpleFilter.getLastExecutions().size());
        Object[] params = simpleFilter.getLastExecutions().get(0);
        assertEquals(2, params.length);
        assertEquals(null, ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.BEFORE_READ_MULTIPLE, params[1]);

        params = simpleFilter.getLastExecutions().get(1);
        assertEquals(2, params.length);
        assertEquals("test1", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.AFTER_READ_MULTIPLE, params[1]);

        params = simpleFilter.getLastExecutions().get(2);
        assertEquals(2, params.length);
        assertEquals("test2", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.AFTER_READ_MULTIPLE, params[1]);

        gigaSpace.clear(null);
    }

    public void testTakeMultiple() throws Exception {
        Message message = new Message(1);
        message.setMessage("test1");
        gigaSpace.write(message);

        message = new Message(2);
        message.setMessage("test2");
        gigaSpace.write(message);

        simpleFilter.getLastExecutions().clear();

        gigaSpace.takeMultiple(new Message());

        assertEquals(3, simpleFilter.getLastExecutions().size());
        Object[] params = simpleFilter.getLastExecutions().get(0);
        assertEquals(2, params.length);
        assertEquals(null, ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.BEFORE_TAKE_MULTIPLE, params[1]);

        params = simpleFilter.getLastExecutions().get(1);
        assertEquals(2, params.length);
        assertEquals("test1", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.AFTER_TAKE_MULTIPLE, params[1]);

        params = simpleFilter.getLastExecutions().get(2);
        assertEquals(2, params.length);
        assertEquals("test2", ((Message) params[0]).getMessage());
        assertEquals(FilterOperationCodes.AFTER_TAKE_MULTIPLE, params[1]);

        gigaSpace.clear(null);
    }

    private static class MyTask implements Task<String> {

        private static final long serialVersionUID = 7362616027645953535L;
        private String msg;

        private MyTask(String msg) {
            this.msg = msg;
        }

        @SpaceRouting
        public int routing() {
            return 1;
        }

        public String execute() throws Exception {
            return "return";
        }
    }
}
