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

package org.openspaces.itest.core.space.view;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
import org.openspaces.itest.utils.TestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;


/**
 * @author kobi
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/core/space/view/simple-view.xml")
public class LocalViewTests   { 

     @Autowired protected GigaSpace remoteGigaSpace;
     @Autowired protected GigaSpace gigaSpace;
     @Autowired protected GigaSpace localViewGigaSpace;

    private static int OBJECTS = 100;

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/core/space/view/simple-view.xml"};
    }

    public LocalViewTests() {
 
    }

     @Before public  void onSetUp() throws Exception {
        gigaSpace.clear(new Object());
    }

     @After public  void onTearDown() {
        gigaSpace.clear(new Object());
    }

    Message message;

     @Test public void testBasicLocalView() {
        for (int i = 0; i < OBJECTS; i++) {

            if (i % 2 == 0) {
                message = new Message(i, true);
            } else {
                message = new Message(i, false);
            }
            gigaSpace.write(message);
        }




        TestUtils.repetitive(new Runnable() {
            public void run() {
                 assertEquals(OBJECTS/2 , localViewGigaSpace.count(new Message(false)));
            }
        }, 1000);

    }


}

