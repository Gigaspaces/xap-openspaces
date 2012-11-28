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
package org.openspaces.itest.core.space.datasource;

import junit.framework.Assert;

import org.openspaces.core.GigaSpace;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * 
 * 
 * @author Idan Moyal
 * @since 9.1.1
 *
 */
@SuppressWarnings("deprecation")
public class SpaceDataSourceTests extends AbstractDependencyInjectionSpringContextTests {

    protected GigaSpace gigaSpace;
    
    public SpaceDataSourceTests() {
        setPopulateProtectedVariables(true);
    }
    
    @Override
    protected String[] getConfigLocations() {
        return new String[] { "/org/openspaces/itest/core/space/datasource/space-data-source.xml" };
    }
    
    public void test() {
        int count = gigaSpace.count(null);
        Assert.assertEquals(1, count);
    }
    
}
