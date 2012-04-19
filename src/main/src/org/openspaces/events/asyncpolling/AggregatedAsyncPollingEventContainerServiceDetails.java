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

package org.openspaces.events.asyncpolling;

import org.openspaces.events.EventContainerServiceDetails;
import org.openspaces.pu.service.PlainAggregatedServiceDetails;
import org.openspaces.pu.service.ServiceDetails;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Async Polling container service details.
 *
 * @author kimchy
 */
public class AggregatedAsyncPollingEventContainerServiceDetails extends PlainAggregatedServiceDetails {

    private static final long serialVersionUID = 6886365711202765516L;
    
    public static class Attributes extends EventContainerServiceDetails.Attributes {
        public static final String RECEIVE_TIMEOUT = "receive-timeout";
        public static final String CONCURRENT_CONSUMERS = "concurrent-consumers";
    }

    public AggregatedAsyncPollingEventContainerServiceDetails() {
        super();
    }

    public AggregatedAsyncPollingEventContainerServiceDetails(String serviceType, ServiceDetails[] details) {
        super(serviceType, details);
        int concurrentConsumers = 0;
        for (ServiceDetails detail : details) {
            if (!(detail instanceof AsyncPollingEventContainerServiceDetails)) {
                throw new IllegalArgumentException("Details [" + detail.getClass().getName() + "] is of wrong type");
            }
            AsyncPollingEventContainerServiceDetails asyncPOllingServiceDetails = (AsyncPollingEventContainerServiceDetails) detail;
            concurrentConsumers += asyncPOllingServiceDetails.getConcurrentConsumers();
        }
        getAttributes().put(Attributes.CONCURRENT_CONSUMERS, concurrentConsumers);
    }

    public long getReceiveTimeout() {
        return (Long) getAttributes().get(Attributes.RECEIVE_TIMEOUT);
    }

    public int getConcurrentConsumers() {
        return (Integer) getAttributes().get(Attributes.CONCURRENT_CONSUMERS);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }
}