/*
 * Copyright 2006-2007 the original author or authors.
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

package org.openspaces.admin.esm.events;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.esm.ElasticServiceManager;

/**
 * An event listener to be notified when a elastic service manager is added.
 *
 * @author Moran Avigdor
 * @see org.openspaces.admin.esm.ElasticServiceManagers#getElasticServiceManagerAdded()
 */
public interface ElasticServiceManagerAddedEventListener extends AdminEventListener {

    /**
     * Called when an elastic service manager is added.
     */
    void elasticServiceManagerAdded(ElasticServiceManager elasticServiceManager);
}