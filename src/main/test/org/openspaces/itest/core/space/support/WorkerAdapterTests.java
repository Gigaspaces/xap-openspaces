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
package org.openspaces.itest.core.space.support;

import org.openspaces.core.GigaSpace;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author kimchy
 */
public class WorkerAdapterTests extends AbstractDependencyInjectionSpringContextTests {

    protected GigaSpace gigaSpace1;

    protected GigaSpace gigaSpace2;

    protected MyWorker worker1;

    protected MyWorker worker2;

    public WorkerAdapterTests() {
        setPopulateProtectedVariables(true);
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/space/support/context.xml"};
    }

    public void testCorrectCalls() throws Exception {
        assertTrue(worker1.isInitCalled());
        
        assertFalse(worker2.isInitCalled());

        // sleep to wait for the thread to start
        Thread.sleep(500);
        
        assertTrue(worker1.isRunCalled());
        assertFalse(worker2.isRunCalled());
    }
}

