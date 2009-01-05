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

package org.openspaces.admin;

import org.openspaces.admin.internal.admin.DefaultAdmin;

/**
 * A factory allowing to create {@link org.openspaces.admin.Admin} instance.
 *
 * <p>Allows to set the Lookup Service Groups and Locators. The Admin listens for events
 * from the lookup serivce of components added and removed. It will monitor all components
 * that work within the specified group and locator.
 *
 * <p>Allows to set the username and password that will be used to authenticate when secured
 * services are discovered.
 *
 * @author kimchy
 */
public class AdminFactory {

    private DefaultAdmin admin = new DefaultAdmin();

    /**
     * Adds a lookup group that will be used to find Lookup Services (using multicast)
     * that will be used to listen for events.
     */
    public AdminFactory addGroup(String group) {
        admin.addGroup(group);
        return this;
    }

    /**
     * Adds a lookup locator (ip and port) of the lookup service that will be used to listen
     * for events.
     */
    public AdminFactory addLocator(String locator) {
        admin.addLocator(locator);
        return this;
    }

    /**
     * Sets the username that will be used when discovering secured services.
     */
    public AdminFactory setUsername(String username) {
        admin.setUsername(username);
        return this;
    }

    /**
     * Sets the password that will be used when discovering secured services.
     */
    public AdminFactory setPassword(String password) {
        admin.setPassword(password);
        return this;
    }

    /**
     * Creats the admin and begins its listening for events from the lookup service.
     */
    public Admin createAdmin() {
        admin.begin();
        return admin;
    }
}
