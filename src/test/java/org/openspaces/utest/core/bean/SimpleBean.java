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
package org.openspaces.utest.core.bean;

import org.openspaces.admin.Admin;
import org.openspaces.core.bean.Bean;

import java.util.Map;

public class SimpleBean implements Bean {

    private Map<String, String> properties;
    private Admin admin;

    public void afterPropertiesSet() throws Exception {

    }

    public void destroy() throws Exception {

    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
