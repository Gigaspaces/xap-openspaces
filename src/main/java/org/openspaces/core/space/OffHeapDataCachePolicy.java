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
package org.openspaces.core.space;

import com.j_spaces.core.Constants;

import java.util.Properties;

/**
 * A cache policy that stores data offheap and ndexes onheap.
 *
 * @author yechielf
 */
public class OffHeapDataCachePolicy implements CachePolicy {

    public Properties toProps() {
        Properties props = new Properties();
        props.setProperty(Constants.CacheManager.FULL_CACHE_POLICY_PROP, "" + Constants.CacheManager.CACHE_POLICY_CACHED_INDICES_OFFHEAP_DATA);
        return props;
    }
}
