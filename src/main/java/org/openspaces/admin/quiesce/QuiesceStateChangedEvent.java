/*
 * Copyright 2002-2006 the original author or authors.
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

package org.openspaces.admin.quiesce;

import com.gigaspaces.admin.quiesce.QuiesceState;
import com.gigaspaces.admin.quiesce.QuiesceToken;

/**
 * @author Boris
 * @since 10.1.0
 */
public class QuiesceStateChangedEvent {

    private QuiesceState quiesceState;
    private QuiesceToken token;
    private String description;

    public QuiesceStateChangedEvent(QuiesceState quiesceState, QuiesceToken token, String description) {
        this.quiesceState = quiesceState;
        this.token = token;
        this.description = description;
    }

    public QuiesceState getQuiesceState(){
        return quiesceState;
    }

    public QuiesceToken getToken() {
        return token;
    }

    public String getDescription() {
        return description;
    }
}
