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

package org.openspaces.admin.gateway.events;

/**
 * An event manager allowing to add and remove {@link GatewayAddedEventListener}s.
 * 
 * @since 9.5
 * @author evgeny
 * @see org.openspaces.admin.gateway.Gateways#getGatewayAdded()
 */
public interface GatewayAddedEventManager {

    /**
     * Adds an event listener.
     */
    void add(GatewayAddedEventListener eventListener);
    
    /**
     * Adds an event listener. <code>includeExisting</code> controls if events will be raised for existing
     * gateways as well.
     */
    void add(GatewayAddedEventListener eventListener, boolean includeExisting);

    /**
     * Removes an event listener.
     */
    void remove(GatewayAddedEventListener eventListener);
}