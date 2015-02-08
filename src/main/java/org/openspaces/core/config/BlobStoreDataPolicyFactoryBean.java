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

import com.gigaspaces.server.blobstore.BlobStoreStorageHandler;
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

    private Long avgObjectSizeKB;
    private Integer cacheEntriesPercentage;
    private Boolean recoverFromBlobStore;
    private BlobStoreStorageHandler blobStoreHandler;

    public Long getAvgObjectSizeKB() {
        return avgObjectSizeKB;
    }

    public void setAvgObjectSizeKB(Long avgObjectSizeKB) {
        this.avgObjectSizeKB = avgObjectSizeKB;
    }

    public Integer getCacheEntriesPercentage() {
        return cacheEntriesPercentage;
    }

    public void setCacheEntriesPercentage(Integer cacheEntriesPercentage) {
        this.cacheEntriesPercentage = cacheEntriesPercentage;
    }

    public Boolean getRecoverFromBlobStore() {
        return recoverFromBlobStore;
    }

    public void setRecoverFromBlobStore(Boolean recoverFromBlobStore) {
        this.recoverFromBlobStore = recoverFromBlobStore;
    }

    public BlobStoreStorageHandler getBlobStoreHandler() {
        return blobStoreHandler;
    }

    public void setBlobStoreHandler(BlobStoreStorageHandler blobStoreHandler) {
        this.blobStoreHandler = blobStoreHandler;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    public CachePolicy asCachePolicy() {
        final BlobStoreDataCachePolicy policy = new BlobStoreDataCachePolicy();
        if (avgObjectSizeKB != null)
            policy.setAvgObjectSizeKB(avgObjectSizeKB);
        if (cacheEntriesPercentage != null)
            policy.setCacheEntriesPercentage(cacheEntriesPercentage);
        if (recoverFromBlobStore != null) {
            policy.setRecoverFromBlobStore(recoverFromBlobStore);
        } else {
            policy.setRecoverFromBlobStore(true);
        }
        if (blobStoreHandler != null)
            policy.setBlobStoreHandler(blobStoreHandler);
        return policy;
    }
     
    
}
