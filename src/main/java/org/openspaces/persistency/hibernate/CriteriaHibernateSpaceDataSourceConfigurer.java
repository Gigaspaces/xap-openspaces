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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;

/**
 * A configurer class which is used to configure a {@link CriteriaHibernateSpaceDataSource}
 * @author eitany
 * @since 9.5
 */
public class CriteriaHibernateSpaceDataSourceConfigurer {
    protected final Log logger = LogFactory.getLog(getClass());

    private SessionFactory sessionFactory;

    private Set<String> managedEntries;

    private String[] initialLoadEntries;

    private int fetchSize = 100;

    private int initialLoadThreadPoolSize = 10;

    private int initialLoadChunkSize = 100000;

    private boolean performOrderById = true;

    private boolean useScrollableResultSet = true;

    /**
     * Injects the Hibernate SessionFactory to be used with this data source.
     */
    public CriteriaHibernateSpaceDataSourceConfigurer sessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        return this;
    }

    /**
     * Sets all the entries this Hibernate data source will work with. By default, will use Hibernate meta
     * data API in order to get the list of all the given entities it handles.
     *
     * <p>This list is used to filter out entities when performing all data source operations exception for
     * the {@link #initialLoad()} operation.
     *
     * <p>Usually, there is no need to explicitly set this.
     */
    public CriteriaHibernateSpaceDataSourceConfigurer managedEntries(String... entries) {
        this.managedEntries = new HashSet<String>();
        this.managedEntries.addAll(Arrays.asList(entries));
        return this;
    }

    /**
     * Sets the fetch size that will be used when working with scrollable results. Defaults to
     * <code>100</code>.
     *
     * @see org.hibernate.Criteria#setFetchSize(int)
     */
    public CriteriaHibernateSpaceDataSourceConfigurer fetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
        return this;
    }

    /**
     * When performing initial load, this flag indicates if the generated query will order to results by
     * the id. By default set to <code>true</code> as it most times results in better initial load performance.
     * @return 
     */
    public CriteriaHibernateSpaceDataSourceConfigurer performOrderById(boolean performOrderById) {
        this.performOrderById = performOrderById;
        return this;
    }

    /**
     * Sets a list of entries that will be used to perform the {@link #initialLoad()} operation. By default, will
     * try and build a sensible list based on Hibernate meta data.
     *
     * <p>Note, sometimes an explicit list should be provided. For example, if we have a class A and class B, and
     * A has a relationship to B which is not component. If in the space, we only wish to have A, and have B just
     * as a field in A (and not as an Entry), then we need to explicitly set the list just to A. By default, if
     * we won't set it, it will result in two entries existing in the Space, A and B, with A having a field of B
     * as well.
     */
    public CriteriaHibernateSpaceDataSourceConfigurer initialLoadEntries(String... initialLoadEntries) {
        this.initialLoadEntries = initialLoadEntries;
        return this;
    }

    /**
     * The initial load operation uses the {@link org.openspaces.persistency.support.ConcurrentMultiDataIterator}.
     * This property allows to control the thread pool size of the concurrent multi data iterator. Defaults to
     * <code>10</code>.
     *
     * <p>Note, this usually will map one to one to the number of open connections / cursors against the database.
     */
    public CriteriaHibernateSpaceDataSourceConfigurer initialLoadThreadPoolSize(int initialLoadThreadPoolSize) {
        this.initialLoadThreadPoolSize = initialLoadThreadPoolSize;
        return this;
    }

    /**
     * By default, the initial load process will chunk large tables and will iterate over the table (entity) per
     * chunk (concurrently). This setting allows to control the chunk size to split the table by. By default, set
     * to <code>100,000</code>. Batching can be disabled by setting <code>-1</code>.
     */
    public CriteriaHibernateSpaceDataSourceConfigurer initialLoadChunkSize(int initalLoadChunkSize) {
        this.initialLoadChunkSize = initalLoadChunkSize;
        return this;
    }

    /**
     * Controls if scrollable result sets will be used with initial load operation. Defaults to <code>true</code>.
     */
    public CriteriaHibernateSpaceDataSourceConfigurer useScrollableResultSet(boolean useScrollableResultSet) {
        this.useScrollableResultSet = useScrollableResultSet;
        return this;
    }
    
    /**
     * Creates a {@link DefaultHibernateExternalDataSource} with the setup configuration.
     */
    public CriteriaHibernateSpaceDataSource create(){
        return new CriteriaHibernateSpaceDataSource(sessionFactory,
                managedEntries, fetchSize, performOrderById, initialLoadEntries, initialLoadThreadPoolSize,
                initialLoadChunkSize, useScrollableResultSet);
    }

}
