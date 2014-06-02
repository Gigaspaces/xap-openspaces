/*
 * Copyright 2014 the original author or authors.
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

package org.openspaces.core.space;

import com.j_spaces.core.IJSpace;

import java.util.Properties;

/**
 * @author yuvalm
 */
public class SpaceProxyConfigurer implements SpaceConfigurer {

    private UrlSpaceConfigurer urlSpaceConfigurer;

    public SpaceProxyConfigurer(String name) {
        urlSpaceConfigurer = new UrlSpaceConfigurer(name);
        urlSpaceConfigurer.setUrlSpaceFactoryBean(new SpaceProxyFactoryBean(name));
    }

    @Override
    public IJSpace space() {
        return urlSpaceConfigurer.space();
    }

    public SpaceProxyConfigurer addProperty(String name, String value) {
        urlSpaceConfigurer.addProperty(name, value);
        return this;
    }

    public SpaceProxyConfigurer addProperties(Properties props) {
        urlSpaceConfigurer.addProperties(props);
        return this;
    }

    public SpaceProxyConfigurer fifo(boolean fifo) {
        urlSpaceConfigurer.fifo(fifo);
        return this;
    }

    public SpaceProxyConfigurer lookupGroups(String... lookupGroups) {
        urlSpaceConfigurer.lookupGroups(lookupGroups);
        return this;
    }

    public SpaceProxyConfigurer lookupGroups(String lookupGroups) {
        urlSpaceConfigurer.lookupGroups(lookupGroups);
        return this;
    }

    public SpaceProxyConfigurer lookupLocators(String lookupLocators) {
        urlSpaceConfigurer.lookupLocators(lookupLocators);
        return this;
    }

    public SpaceProxyConfigurer lookupLocators(String... lookupLocators) {
        urlSpaceConfigurer.lookupLocators(lookupLocators);
        return this;
    }

    public SpaceProxyConfigurer lookupTimeout(int lookupTimeout) {
        urlSpaceConfigurer.lookupTimeout(lookupTimeout);
        return this;
    }

    public SpaceProxyConfigurer versioned(boolean versioned) {
        urlSpaceConfigurer.versioned(versioned);
        return this;
    }

    public SpaceProxyConfigurer mirror(boolean mirror) {
        urlSpaceConfigurer.mirror(mirror);
        return this;
    }

    public SpaceProxyConfigurer registerForSpaceModeNotifications(boolean registerForSpaceMode) {
        urlSpaceConfigurer.registerForSpaceModeNotifications(registerForSpaceMode);
        return this;
    }
}
