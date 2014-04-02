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
import com.j_spaces.core.cache.offHeap.OffHeapStorageHandler;

import java.util.Properties;

/**
 * A cache policy that stores data offheap and indexes onheap.
 *
 * @author yechielf
 */
public class OffHeapDataCachePolicy implements CachePolicy {

    private Integer offHeapCacheSize;
    private OffHeapStorageHandler offHeapStorageHandler;


    public OffHeapDataCachePolicy() {
    }

    public OffHeapDataCachePolicy(OffHeapStorageHandler offHeapStorageHandler) {
        this.offHeapStorageHandler = offHeapStorageHandler;
    }

    public OffHeapDataCachePolicy(Integer offheapCacheSize, OffHeapStorageHandler offHeapStorageHandler) {
        this.offHeapCacheSize = offheapCacheSize;
        this.offHeapStorageHandler = offHeapStorageHandler;
    }

    public void setOffHeapStorageHandler(OffHeapStorageHandler offHeapStorageHandler) {
        this.offHeapStorageHandler = offHeapStorageHandler;
    }

    public void setOffHeapCacheSize(Integer offHeapCacheSize) {
        this.offHeapCacheSize = offHeapCacheSize;
    }

    public Properties toProps() {
        Properties props = new Properties();
        props.setProperty(Constants.CacheManager.FULL_CACHE_POLICY_PROP, "" + Constants.CacheManager.CACHE_POLICY_CACHED_INDICES_OFFHEAP_DATA);

        if(offHeapCacheSize != null) {
            props.setProperty(Constants.CacheManager.CACHE_MANAGER_OFFHEAP_CACHE_SIZE_PROP, offHeapCacheSize.toString());
        }

        if(offHeapStorageHandler != null){
            props.put(Constants.CacheManager.CACHE_MANAGER_OFFHEAP_STORAGE_HANDLER_PROP, offHeapStorageHandler);
        }

        return props;
    }
}
