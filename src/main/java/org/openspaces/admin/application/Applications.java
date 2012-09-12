/*******************************************************************************
 * 
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
 *  
 ******************************************************************************/
package org.openspaces.admin.application;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.application.events.ApplicationAddedEventManager;
import org.openspaces.admin.application.events.ApplicationLifecycleEventListener;
import org.openspaces.admin.application.events.ApplicationRemovedEventManager;


/**
 * Holds one or more {@link org.openspaces.admin.application.Application}s
 * 
 * @author itaif
 * @since 8.0.3
 */
public interface Applications extends Iterable<Application>, AdminAware {

    /**
     * Returns the number of currently deployed {@link org.openspaces.admin.application.Application}s.
     */
    int getSize();

    /**
     * Returns <code>true</code> if there are no currently deployed applications.
     */
    boolean isEmpty();

    /**
     * Returns the {@link org.openspaces.admin.application.Application}s currently deployed.
     */
    Application[] getApplications();

    /**
     * Returns the {@link org.openspaces.admin.application.Application} for the given processing unit name.
     */
    Application getApplication(String name);

    /**
     * Returns a map of {@link org.openspaces.admin.application.Application} keyed by their respective names.
     */
    Map<String, Application> getNames();

    /**
     * Waits indefinitely till the application is identified as deployed. Returns the
     * {@link org.openspaces.admin.application.Application}.
     */
    Application waitFor(String applicationName);

    /**
     * Waits for the specified timeout (in time interval) till the application is identified as deployed. Returns the
     * {@link org.openspaces.admin.application.Application}. Return <code>null</code> if the application is not deployed
     * within the specified timeout.
     */
    Application waitFor(String applicationName, long timeout, TimeUnit timeUnit);

    /**
     * Returns an event manager allowing to register {@link org.openspaces.admin.application.events.ApplicationAddedEventListener}s.
     */
    ApplicationAddedEventManager getApplicationAdded();

    /**
     * Returns an event manager allowing to register {@link org.openspaces.admin.pu.events.ProcessingUnitRemovedEventListener}s.
     */
    ApplicationRemovedEventManager getApplicationRemoved();

    /**
     * Allows to add a {@link ApplicationLifecycleEventListener}.
     */
    void addLifecycleListener(ApplicationLifecycleEventListener eventListener);

    /**
     * Allows to remove a {@link ApplicationLifecycleEventListener}.
     */
    void removeLifecycleListener(ApplicationLifecycleEventListener eventListener);

}
