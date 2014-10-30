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

package org.openspaces.itest.core.context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertSame;


/**
 * The test verifies that two different {@link org.openspaces.core.GigaSpace} instances defined
 * within the same application context can be differnicated by using {@link org.openspaces.core.context.GigaSpaceContext#name()}.
 *
 * @author kimchy
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/core/context/double-context.xml")
public class DoubleGigaSpaceContextTests   { 

     @Autowired protected GigaSpace gs1;
     @Autowired protected GigaSpace gs2;

     @Autowired protected DoubleContextTestBean testBean;

    public DoubleGigaSpaceContextTests() {
 
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/context/double-context.xml"};
    }

     @Test public void testFieldInjectedGs1() {
        assertSame(gs1, testBean.gs1);
    }

     @Test public void testSetterInjectionGs2() {
        assertSame(gs2, testBean.gs2);
    }
}

