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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


/**
 * 
 * 
 * @author Idan Moyal
 * @since 9.1.1
 *
 */
@SuppressWarnings("deprecation")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/core/space/datasource/space-data-source.xml")

public class SpaceDataSourceTests   { 

     @Autowired protected GigaSpace gigaSpace;
    
    public SpaceDataSourceTests() {
 
    }
    
    //@Override
    protected String[] getConfigLocations () {
        return new String[] { "/org/openspaces/itest/core/space/datasource/space-data-source.xml" };
    }
    
     @Test public void test() {
        int count = gigaSpace.count(null);
        Assert.assertEquals(1, count);
    }
    
}

