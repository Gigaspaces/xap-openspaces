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

package org.openspaces.interop;

import org.openspaces.pu.service.PlainServiceDetails;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * A service that holds a dot net.
 *
 * @author kimchy
 */
public class DotnetContainerServiceDetails extends PlainServiceDetails {

    private static final long serialVersionUID = 2644937669655497764L;
    
    public static final String SERVICE_TYPE = "dotnet";
    
    public DotnetContainerServiceDetails() {
    }

    public DotnetContainerServiceDetails(String id, String serviceSubType, String description, String longDescription) {
        super(id, SERVICE_TYPE, serviceSubType, description, longDescription);
    }

    public void setSubType(String type) {
        this.serviceSubType = type;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
    }
}