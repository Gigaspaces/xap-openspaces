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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * @author kimchy
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/core/space/support/context.xml")
public class WorkerAdapterTests   { 

     @Autowired protected GigaSpace gigaSpace1;

     @Autowired protected GigaSpace gigaSpace2;

     @Autowired protected MyWorker worker1;

     @Autowired protected MyWorker worker2;

    public WorkerAdapterTests() {
 
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/space/support/context.xml"};
    }

     @Test public void testCorrectCalls() throws Exception {
        assertTrue(worker1.isInitCalled());
        
        assertFalse(worker2.isInitCalled());

        // sleep to wait for the thread to start
        Thread.sleep(500);
        
        assertTrue(worker1.isRunCalled());
        assertFalse(worker2.isRunCalled());
    }
}


