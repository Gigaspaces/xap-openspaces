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
package org.openspaces.itest.core.iterator;

import com.j_spaces.core.client.GSIterator;
import junit.framework.TestCase;
import net.jini.core.entry.Entry;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;

/**
 * @author kimchy
 */
public class SimpleIteratorTests extends TestCase {

    public void testSimpleIterator() {
        UrlSpaceConfigurer urlSpaceConfigurer = new UrlSpaceConfigurer("/./space").lookupGroups(System.getProperty("user.name"));
        GigaSpace gigaSpace = new GigaSpaceConfigurer(urlSpaceConfigurer.space()).gigaSpace();

        for (int i = 0; i < 50; i++) {
            TestMessage testMessage = new TestMessage();
            testMessage.id = i;
            gigaSpace.write(testMessage);
        }

        GSIterator it = gigaSpace.iterator().withHistory().addTemplate(new TestMessage()).iterate();
        int counter = 0;
        for (Object test : it) {
            counter++;
        }
        assertEquals(50, counter);
    }

    public static class TestMessage implements Entry {
        private static final long serialVersionUID = 4214004586339042525L;

        public Integer id;
    }
}
