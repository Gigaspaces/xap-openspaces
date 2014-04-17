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
import java.util.logging.Logger;

/**
 * A cache policy that stores data offheap and indexes onheap.
 *
 * @author yechielf, Kobi
 */
public class BlobStoreDataCachePolicy implements CachePolicy {
    private static final Logger _logger = Logger.getLogger(com.gigaspaces.logger.Constants.LOGGER_CONFIG);

    private Long avgObjectSizeKB;
    private Integer cacheEntriesPercentage;
    private OffHeapStorageHandler blobStorageHandler;

    private static final long DEFAULT_AVG_OBJECT_SIZE_KB = 5;
    private static final int DEFAULT_CACHE_ENTRIES_PERCENTAGE = 20;

    public BlobStoreDataCachePolicy() {
    }

    public BlobStoreDataCachePolicy(OffHeapStorageHandler offHeapStorageHandler) {
        this.blobStorageHandler = offHeapStorageHandler;
    }

    public void setOffHeapStorageHandler(OffHeapStorageHandler offHeapStorageHandler) {
        this.blobStorageHandler = offHeapStorageHandler;
    }

    public void setAvgObjectSizeKB(Long avgObjectSizeKB) {
        this.avgObjectSizeKB = avgObjectSizeKB;
    }

    public void setCacheEntriesPercentage(Integer cacheEntriesPercentage) {
        this.cacheEntriesPercentage = cacheEntriesPercentage;
    }

    public Properties toProps() {
        Properties props = new Properties();
        props.setProperty(Constants.CacheManager.FULL_CACHE_POLICY_PROP, "" + Constants.CacheManager.CACHE_POLICY_CACHED_INDICES_OFFHEAP_DATA);

        long blobStoreCacheSize;

        if(cacheEntriesPercentage == null){
            cacheEntriesPercentage = DEFAULT_CACHE_ENTRIES_PERCENTAGE;
        }
        if(avgObjectSizeKB == null) {
            avgObjectSizeKB = DEFAULT_AVG_OBJECT_SIZE_KB;
        }
        assertPropPositive("cacheEntriesPercentage", cacheEntriesPercentage);
        assertPropPositive("avgObjectSizeKB", avgObjectSizeKB);
        assertPropNotZero("avgObjectSizeKB", avgObjectSizeKB);

        if(cacheEntriesPercentage != 0){
            long maxMemoryInBytes = Runtime.getRuntime().maxMemory();
            if(maxMemoryInBytes == Long.MAX_VALUE){
                blobStoreCacheSize = Long.parseLong(Constants.CacheManager.CACHE_MANAGER_OFFHEAP_CACHE_SIZE_DELAULT);
                _logger.info("Blob Store Cache size [ " +blobStoreCacheSize +" ]");
            }else{
                double percentage = (double)cacheEntriesPercentage/100;
                blobStoreCacheSize = (long) ((maxMemoryInBytes*percentage)/(1024*avgObjectSizeKB));
            }
        }else{
            blobStoreCacheSize = 0;
        }

        props.setProperty(Constants.CacheManager.CACHE_MANAGER_OFFHEAP_CACHE_SIZE_PROP, String.valueOf(blobStoreCacheSize));
        _logger.info("Blob Store Cache size [ " +blobStoreCacheSize +" ]");


        if(blobStorageHandler != null){
            props.put(Constants.CacheManager.CACHE_MANAGER_OFFHEAP_STORAGE_HANDLER_PROP, blobStorageHandler);
        }

        return props;
    }

    private void assertPropPositive(String propName, long propValue){
        if(propValue < 0){
            throw new IllegalArgumentException(propName + " can not be negative");
        }
    }

    private void assertPropNotZero(String propName, long propValue){
        if(propValue == 0){
            throw new IllegalArgumentException(propName + " can not be zero");
        }
    }

}
