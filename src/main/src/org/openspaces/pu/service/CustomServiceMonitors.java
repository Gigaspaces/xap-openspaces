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
package org.openspaces.pu.service;

import java.util.Map;


/**
 * This class should be used by customers in order to implement their ServiceMonitors
 *
 * @since 8.0.1
 */
public class CustomServiceMonitors extends PlainServiceMonitors {

    public CustomServiceMonitors() {
    }

    /**
     * 
     * @param id should identify that service
     */
    public CustomServiceMonitors(String id) {
        super(id);
    }

    /**
     * 
     * @param id should identify that service
     * @param exposed monitors
     */
    public CustomServiceMonitors(String id, Map<String,Object> monitors) {
        super(id, monitors);
    }
}