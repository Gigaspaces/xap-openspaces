/*******************************************************************************
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.openspaces.events;

/**
 * A template provider allowing for custom implementations to provide a template used for matching
 * on events to receive.
 * 
 * Unlike the {@link EventTemplateProvider} which is called only once,
 * this implementation is called before each space read or take operation,
 * providing the ability to modify the take template for each specific operation.
 *
 * <p>
 * Can be used when a {@link org.openspaces.events.SpaceDataEventListener SpaceDataEventListener} is
 * provided that also controls the template associated with it. 
 * Use with {@link org.openspaces.events.polling.SimplePollingEventListenerContainer} or {@link org.openspaces.archive.ArchivePollingContainer})
 *
 * @author Itai Frenkel
 * @since 9.1.1
 */
public interface DynamicEventTemplateProvider {

    /**
     * Returns the template that will be used for matching on events,
     * and is called before each take or read operation.
     */
    Object getDynamicTemplate();
}
