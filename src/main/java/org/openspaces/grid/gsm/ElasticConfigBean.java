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
package org.openspaces.grid.gsm;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.internal.pu.elastic.GridServiceAgentFailureDetectionConfig;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.core.bean.Bean;

public class ElasticConfigBean implements Bean {

    Map<String,String> properties;
    
    GridServiceContainerConfig getGridServiceContainerConfig() {
        return new GridServiceContainerConfig(properties);
    }

	GridServiceAgentFailureDetectionConfig getAgentFailureDetectionConfig() {
		return new GridServiceAgentFailureDetectionConfig(properties);
	}
	
    public void afterPropertiesSet() throws Exception {
        // TODO Auto-generated method stub
        
    }

    public void destroy() throws Exception {
        // TODO Auto-generated method stub
        
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    public void setAdmin(Admin admin) {
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
}
