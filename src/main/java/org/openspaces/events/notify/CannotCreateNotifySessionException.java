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

import com.gigaspaces.events.EventSessionConfig;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * A failure to create a notify session.
 * 
 * @author kimchy
 */
public class CannotCreateNotifySessionException extends DataAccessResourceFailureException {

    private static final long serialVersionUID = 8957193715747405306L;

    private EventSessionConfig config;

    public CannotCreateNotifySessionException(String message, EventSessionConfig config) {
        super(message);
        this.config = config;
    }

    public CannotCreateNotifySessionException(String message, EventSessionConfig config, Throwable cause) {
        super(message, cause);
        this.config = config;
    }

    public EventSessionConfig getConfig() {
        return config;
    }
}
