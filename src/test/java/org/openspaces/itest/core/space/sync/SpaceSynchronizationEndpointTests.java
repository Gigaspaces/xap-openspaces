/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.itest.core.space.sync;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.TimeoutException;

import junit.framework.Assert;

import org.openspaces.core.GigaSpace;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;

/**
 * @author Idan Moyal
 * @since 9.5
 *
 */
@SuppressWarnings("deprecation")
public class SpaceSynchronizationEndpointTests extends AbstractDependencyInjectionSpringContextTests {

    protected GigaSpace gigaSpace;
    
    public SpaceSynchronizationEndpointTests() {
        setPopulateProtectedVariables(true);
    }
    
    @Override
    protected String[] getConfigLocations() {
        return new String[] { "/org/openspaces/itest/core/space/sync/space-sync-endpoint.xml" };
    }
    
    public void test() throws InterruptedException, BrokenBarrierException, TimeoutException  {
        gigaSpace.getTypeManager().registerTypeDescriptor(
                new SpaceTypeDescriptorBuilder("MockDocument").idProperty("id").create());
        SpaceDocument document = new SpaceDocument("MockDocument");
        document.setProperty("id", "abcd");
        
        gigaSpace.write(document);
        
        Assert.assertTrue(MockSpaceSynchronizationEndpoint.invoked);
    }

    
}
