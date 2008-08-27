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

package org.openspaces.example.helloworld;

import org.apache.commons.math.random.RandomData;
import org.apache.commons.math.random.RandomDataImpl;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>A simple bean that is run within a Spring context that acts
 * as the processing unit context as well (when executed within a
 * processing unit container).
 *
 * <p>Gets injected with a {@link org.openspaces.core.GigaSpace} interface
 * which provides a simpler programming model on top of JavaSpaces core interface.
 *
 * <p>The bean writes and takes a {@link org.openspaces.example.helloworld.Message}
 * object from the space using the injected {@link org.openspaces.core.GigaSpace}
 * when the bean starts up (using Spring {@link org.springframework.beans.factory.InitializingBean}
 * callback). This means that spring context is started or the processing unit is
 * deployed, this code will be executed.
 *
 * @author kimchy
 */
public class HelloWorldBean implements InitializingBean {

    private GigaSpace gigaSpace;

    /**
     * A GigaSpace interface used to interact with the space. Injected
     * by Spring.
     */
    public void setGigaSpace(GigaSpace gigaSpace) {
        this.gigaSpace = gigaSpace;
    }

    /**
     * A spring callback invoked when the container starts. Uses
     * the injected GigaSpace to write and then take a single SimpleMessage
     * object.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(gigaSpace, "gigaSpace property is required");

        RandomData randomData = new RandomDataImpl();

        Message message = new Message("Hello World " + randomData.nextLong(0l, 100l));
        System.out.println("Writing SimpleMessage [" + message.getMessage() + "]");
        gigaSpace.write(message);

        message = (Message) gigaSpace.take(new Message());
        System.out.println("Took SimpleMessage [" + message.getMessage() + "]");
    }
}
