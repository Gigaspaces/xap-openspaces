/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.itest.core.simple;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.executor.support.WaitForAllListener;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kimchy
 */
public class SimpleOperationsTests extends AbstractDependencyInjectionSpringContextTests {

    protected GigaSpace gigaSpace;

    public SimpleOperationsTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/simple/context.xml"};
    }

    protected void onSetUp() throws Exception {
        gigaSpace.clear(null);
    }

    protected void onTearDown() throws Exception {
        gigaSpace.clear(null);
    }

    public void testSimpleOperations() throws Exception {
        assertEquals(0, gigaSpace.count(null));
        gigaSpace.write(new Message("test"));
        assertEquals(1, gigaSpace.count(null));

        Message val = gigaSpace.read(null);
        assertNotNull(val);

        val = gigaSpace.take(null);
        assertNotNull(val);
    }

    public void testAsyncOperations() throws Exception {
        assertEquals(0, gigaSpace.count(null));
        assertNull(gigaSpace.asyncRead(new Message()).get());
        assertNull(gigaSpace.asyncRead(new Message(), 0).get());
        assertNull(gigaSpace.asyncRead(new Message(), 0, 0).get());

        WaitForAllListener listener = new WaitForAllListener(1);
        gigaSpace.asyncRead(new Message(), listener);
        assertNull(listener.waitForResult()[0].get());

        listener = new WaitForAllListener(1);
        gigaSpace.asyncRead(new Message(), 0, listener);
        assertNull(listener.waitForResult()[0].get());

        listener = new WaitForAllListener(1);
        gigaSpace.asyncRead(new Message(), 0, 0, listener);
        assertNull(listener.waitForResult()[0].get());

        assertNull(gigaSpace.asyncTake(new Message()).get());
        assertNull(gigaSpace.asyncTake(new Message(), 0).get());
        assertNull(gigaSpace.asyncTake(new Message(), 0, 0).get());

        listener = new WaitForAllListener(1);
        gigaSpace.asyncTake(new Message(), listener);
        assertNull(listener.waitForResult()[0].get());

        listener = new WaitForAllListener(1);
        gigaSpace.asyncTake(new Message(), 0, listener);
        assertNull(listener.waitForResult()[0].get());

        listener = new WaitForAllListener(1);
        gigaSpace.asyncTake(new Message(), 0, 0, listener);
        assertNull(listener.waitForResult()[0].get());

        gigaSpace.write(new Message("test"));
        assertEquals(1, gigaSpace.count(null));
        assertEquals(gigaSpace.asyncRead(new Message()).get().getValue(), "test");
        assertEquals(1, gigaSpace.count(null));
        assertEquals(gigaSpace.asyncTake(new Message()).get().getValue(), "test");
        assertEquals(0, gigaSpace.count(null));
    }
}
