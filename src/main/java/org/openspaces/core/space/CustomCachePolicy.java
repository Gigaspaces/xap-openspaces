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
package org.openspaces.core.space;

import java.util.Properties;

import com.gigaspaces.server.eviction.SpaceEvictionStrategy;
import com.j_spaces.core.Constants;

/**
 * Configures the Space to run in Custom mode. Defaults value for all configuration will be based on
 * the schema chosen.
 * 
 * @author Sagi Bernstein
 * @since 9.1.0
 *
 */
public class CustomCachePolicy extends LruCachePolicy {

    //TODO remove this once we support injection propertly
    private String customCachePolicyClass = "";
    private SpaceEvictionStrategy evictionStrategy;
    
    
    /**
     * Sets the custom eviction policy to be used by the space,
     * the String argument must be the fully qualified name (i.e. com.mycompany.myapp.MySpaceCachePolicy)
     * When using a Custom Cache Policy this property must be set
     */
    //TODO remove this once we support injection propertly
    public CustomCachePolicy customCachePolicyClass(String customCachePolicyClass) {
        setCustomCachePolicyClass(customCachePolicyClass);
        return this;
    }
    
    /**
     * Sets the custom eviction strategy to be used by the space,
     * When using a Custom Cache Policy this property must be set.
     */
    public CustomCachePolicy evictionStrategy(SpaceEvictionStrategy evictionStrategy) {
        setEvictionStrategy(evictionStrategy);
        return this;
    }
    
    /**
     * Sets the custom eviction policy to be used by the space,
     * the String argument must be the fully qualified name (i.e. com.mycompany.myapp.MySpaceCachePolicy)
     * When using a Custom Cache Policy this property must be set
     */
    //TODO remove this once we support injection propertly
    public void setCustomCachePolicyClass(String customCachePolicyClass) {
        this.customCachePolicyClass = customCachePolicyClass;
    }
    
    /**
     * Sets the custom eviction strategy to be used by the space,
     * When using a Custom Cache Policy this property must be set.
     */
    public void setEvictionStrategy(SpaceEvictionStrategy evictionStrategy) {
        this.evictionStrategy = evictionStrategy;
    }
    
    @Override
    public Properties toProps() {
        Properties props = super.toProps();
        props.setProperty(Constants.CacheManager.FULL_CACHE_POLICY_PROP, "" + Constants.CacheManager.CACHE_POLICY_PLUGGED_EVICTION);
        if (evictionStrategy != null)
            props.put(Constants.CacheManager.CACHE_MANAGER_EVICTION_STRATEGY_PROP, evictionStrategy);
        //TODO remove this once we support injection propertly
        props.setProperty(Constants.CacheManager.FULL_CACHE_MANAGER_EVICTION_STRATEGY_CLASS_PROP, customCachePolicyClass);
        
        return props;
    }
    
}
