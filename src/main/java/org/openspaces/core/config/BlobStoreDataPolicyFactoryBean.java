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
package org.openspaces.core.config;

import com.j_spaces.core.cache.offHeap.OffHeapStorageHandler;
import org.openspaces.core.space.BlobStoreDataCachePolicy;
import org.openspaces.core.space.CachePolicy;
import org.springframework.beans.factory.InitializingBean;

/**
 * A factory for creating {@link org.openspaces.core.space.BlobStoreDataCachePolicy} instance.
 * @author Kobi
 * @since 10.0.0
 *
 */
public class BlobStoreDataPolicyFactoryBean implements InitializingBean {

    private Integer offHeapCacheSize;
    private OffHeapStorageHandler blobStoreHandler;

    public Integer getOffHeapCacheSize() {
        return offHeapCacheSize;
    }

    public void setOffHeapCacheSize(Integer offHeapCacheSize) {
        this.offHeapCacheSize = offHeapCacheSize;
    }

    public OffHeapStorageHandler getBlobStoreHandler() {
        return blobStoreHandler;
    }

    public void setBlobStoreHandler(OffHeapStorageHandler blobStoreHandler) {
        this.blobStoreHandler = blobStoreHandler;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    public CachePolicy asCachePolicy() {
        final BlobStoreDataCachePolicy policy = new BlobStoreDataCachePolicy();
        if (offHeapCacheSize != null)
            policy.setOffHeapCacheSize(offHeapCacheSize);
        if (blobStoreHandler != null)
            policy.setOffHeapStorageHandler(blobStoreHandler);
        return policy;
    }
     
    
}
