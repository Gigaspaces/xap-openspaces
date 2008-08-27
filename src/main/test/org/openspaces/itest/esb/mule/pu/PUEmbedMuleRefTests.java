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

package org.openspaces.itest.esb.mule.pu;

import org.mule.api.config.ConfigurationException;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;
import org.openspaces.itest.esb.mule.SimpleMessage;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Test the ability to run PU with mule imbedded in it.
 *
 * @author yitzhaki
 */
public class PUEmbedMuleRefTests extends AbstractDependencyInjectionSpringContextTests {

    protected String[] getConfigLocations() {
        return new String[]{"org/openspaces/itest/esb/mule/pu/puembedmuleref.xml"};
    }

    public void testTakeSingleFromSpace() throws ConfigurationException {
        GigaSpace gigaSpace = new GigaSpaceConfigurer(new UrlSpaceConfigurer("jini://*/*/space").lookupGroups(System.getProperty("user.name")).space()).gigaSpace();

        gigaSpace.clear(null);
        
        int numberOfMsgs = 10;
        for (int i = 0; i < numberOfMsgs; i++) {
            SimpleMessage message = new SimpleMessage("Hello World " + i, false);
            gigaSpace.write(message);
        }

        //blocking wait untill the mule writes back the messages to the space after reading them.
        for (int i = 0; i < numberOfMsgs; i++) {
            SimpleMessage template = new SimpleMessage("Hello World " + i, true);
            SimpleMessage message = gigaSpace.take(template, 5000);
            assertEquals(template, message);
        }
        assertEquals(0, gigaSpace.count(new SimpleMessage()));
    }
}