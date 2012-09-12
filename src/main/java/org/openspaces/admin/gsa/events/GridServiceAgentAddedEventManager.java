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

package org.openspaces.admin.gsa.events;

/**
 * An event manager allowing to remove and add {@link GridServiceAgentAddedEventListener}s.
 *
 * @author kimchy
 * @see org.openspaces.admin.gsa.GridServiceAgents#getGridServiceAgentAdded() 
 */
public interface GridServiceAgentAddedEventManager {

    /**
     * Add the event listener. Note, the add callback will be called for currently discovered agents as
     * well.
     */
    void add(GridServiceAgentAddedEventListener eventListener);

    /**
     * Add the event listener. Allows to control  if the event will be called for existing grid service
     * agents as well.
     */
    void add(GridServiceAgentAddedEventListener eventListener, boolean includeExisting);

    /**
     * Removes the event listener.
     */
    void remove(GridServiceAgentAddedEventListener eventListener);
}