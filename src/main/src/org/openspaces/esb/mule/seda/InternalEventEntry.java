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

package org.openspaces.esb.mule.seda;

import com.j_spaces.core.client.MetaDataEntry;
import org.mule.api.MuleEvent;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.MarshalledObject;

/**
 * An intenral entry holding the name of the serice (that has the SEDA queue) and the
 * actual mule event to be used.
 *
 * @author kimchy
 */
public class InternalEventEntry extends MetaDataEntry implements Externalizable {

    public String name;

    public MuleEvent event;

    private MarshalledObject marshalledObject;

    public InternalEventEntry() {
    }

    public InternalEventEntry(MuleEvent event, String name) {
        this.event = event;
        this.name = name;
    }

    public static String[] __getSpaceIndexedFields() {
        return new String[]{"name"};
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super._writeExternal(out);
        MarshalledObject marshalledObject = new MarshalledObject(event);
        out.writeUTF(name);
        out.writeObject(marshalledObject);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super._readExternal(in);
        name = in.readUTF();
        marshalledObject = (MarshalledObject) in.readObject();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MuleEvent getEvent() throws IOException, ClassNotFoundException {
        if (event != null) {
            return event;
        }
        event = (MuleEvent) marshalledObject.get();
        marshalledObject = null;
        return event;
    }

    public void setEvent(MuleEvent event) {
        this.event = event;
    }
}