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

package org.openspaces.pu.service;

/**
 * A provider of one or more {@link org.openspaces.pu.service.ServiceDetails}. A bean within a processing
 * unit can implement it in order to expose "static" information regarding the service.
 *
 * @author kimchy
 * @see org.openspaces.pu.service.PlainServiceDetails
 */
public interface ServiceDetailsProvider {

    /**
     * Retruns one or more service details that the service exposes.
     */
    ServiceDetails[] getServicesDetails();
}
