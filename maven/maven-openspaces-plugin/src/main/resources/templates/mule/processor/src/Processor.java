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

package ${puGroupId}.processor;

import ${puGroupId}.common.Data;

import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;


/**
 * An implementation of IDataProcessor. Can set the simulated work done when
 * processData is called by setting the work duration (defaults to 100 ms).
 *
 * <p>This implementation is used to demonstrate two features of OpenSpaces. The
 * first is OpenSpaces Events, using simple Spring configuration to cause
 * processData to be invoked when a matching event occurs. The processor uses
 * OpenSpaces support for annotation markup allowing to use @SpaceDataEvent to
 * mark a method as an event listener. Note, processData does not use any space
 * API (though it can), receiving the Data object to be processed and returning
 * the result that will be automatically written back to the space.
 *
 * <p>Note, changing the event container is just a matter of configuration (for example,
 * switching from polling container to notify container) and does not affect this class.
 *
 * <p>Also note, the deployment model or the Space topology does not affect this data processor
 * as well. The data processor can run on a remote space, embedded within a space, and using
 * any Space cluster topology (partitioned, replicated, primary/backup). It is all just a
 * matter of configuration.
 *
 * <p>The second feature is OpenSpaces remoting, allowing to expose this implementation
 * of IDataProcessor to other processing units (or any other client) to be invoked.
 * The invocation is done using the Space as the transport layer benefiting from all
 * the Space built in features (HA, Load Balancing).
 *
 * @author kimchy
 */
public class Processor implements Callable {

    private long workDuration = 100;

    @GigaSpaceContext
    private GigaSpace gigaSpace;

    /**
     * Sets the simulated work duration (in milliseconds). Defaut to 100.
     */
    public void setWorkDuration(long workDuration) {
        this.workDuration = workDuration;
    }
    
    /**
     * Respond to mule event
     */
    public Object onCall(MuleEventContext eventContext) throws Exception {
        Object payload = eventContext.getMessage().getPayload();
        return processData((Data) payload);
    }

    /**
     * Process the given Data object and returning the processed Data.
     *
     * Can be invoked using OpenSpaces Events when a matching event
     * occurs or using OpenSpaces Remoting.
     */
    private Data processData(Data data) {
        // sleep to simulate some work
        try {
            Thread.sleep(workDuration);
        } catch (InterruptedException e) {
            // do nothing
        }
        data.setProcessed(true);
        data.setData("PROCESSED : " + data.getRawData());
        System.out.println(" ------ PROCESSED : " + data);
        return data;
    }

}
