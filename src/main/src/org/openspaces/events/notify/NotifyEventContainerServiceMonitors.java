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

package org.openspaces.events.notify;

import org.openspaces.events.EventContainerServiceMonitors;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Notify container service monitors.
 *
 * @author kimchy
 */
public class NotifyEventContainerServiceMonitors extends EventContainerServiceMonitors {

    public static class Attributes extends EventContainerServiceMonitors.Attributes {
    }

    public NotifyEventContainerServiceMonitors() {
        super();
    }

    public NotifyEventContainerServiceMonitors(String id, long processedEvents, long failedEvents, String status) {
        super(id, processedEvents, failedEvents, status);
    }

    public NotifyEventContainerServiceDetails getNotifyEventDetails() {
        return (NotifyEventContainerServiceDetails) getEventDetails();
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