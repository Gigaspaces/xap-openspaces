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

package org.openspaces.esb.mule.queue;

import org.mule.api.MuleException;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.transport.MessageRequester;
import org.mule.transport.AbstractMessageRequesterFactory;

/**
 * @author kimchy
 */
public class OpenSpacesQueueMessageRequestorFactory extends AbstractMessageRequesterFactory {
    /*
     * (non-Javadoc)
     *
     * @see org.mule.umo.provider.UMOMessageDispatcherFactory#create(org.mule.umo.provider.UMOConnector)
     */
    public MessageRequester create(InboundEndpoint endpoint) throws MuleException {
        return new OpenSpacesQueueMessageRequestor(endpoint);
    }
}