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
package org.openspaces.persistency.hibernate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.SessionFactory;

/**
 * A configurer class which is used to configure a {@link DefaultHibernateSynchronizationEndpointInterceptor}.
 * @author eitany
 * @since 9.5
 */
public class DefaultHibernateSynchronizationEndpointInterceptorConfigurer {
    
    private SessionFactory sessionFactory;

    private Set<String> managedEntries;
    
    private boolean useMerge = false;
    
    private boolean deleteById = true;
    

    /**
     * If set to <code>true</code>, will use Hibernate <code>merge</code> to perform the create/update, and will
     * merge before calling delete. This might be required for complex mappings (depends on Hibernate) at the
     * expense of slower performance. Defaults to <code>false</code>.
     * @return 
     */
    public DefaultHibernateSynchronizationEndpointInterceptorConfigurer useMerge(boolean useMerge) {
        this.useMerge = useMerge;
        return this;
    }

    /**    
     * If set to <code>true</code> the object will be deleted using only its id.
     * If set to <code>false</code> the object will be deleted using the whole object.
     * Defaults to <code>true</code>.
     * @param deleteById
     * @return 
     */
    public DefaultHibernateSynchronizationEndpointInterceptorConfigurer deleteById(boolean deleteById) {
        this.deleteById = deleteById;
        return this;
    }
    
    /**
     * Injects the Hibernate SessionFactory to be used with this synchronization endpoint interceptor.
     */
    public DefaultHibernateSynchronizationEndpointInterceptorConfigurer sessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        return this;
    }

    /**
     * Sets all the entries this Hibernate synchronization endpoint interceptor will work with. By default, will use Hibernate meta
     * data API in order to get the list of all the given entities it handles.
     *
     * <p>This list is used to filter out entities when performing all synchronization endpoint interceptor operations.
     *
     * <p>Usually, there is no need to explicitly set this.
     */
    public DefaultHibernateSynchronizationEndpointInterceptorConfigurer managedEntries(String... entries) {
        this.managedEntries = new HashSet<String>();
        this.managedEntries.addAll(Arrays.asList(entries));
        return this;
    }
    
    /**
     * Creates a {@link DefaultHibernateSynchronizationEndpointInterceptor} with the setup configuration.
     */
    public DefaultHibernateSynchronizationEndpointInterceptor create(){
        return new DefaultHibernateSynchronizationEndpointInterceptor(sessionFactory, managedEntries, useMerge, deleteById);
    }
}
