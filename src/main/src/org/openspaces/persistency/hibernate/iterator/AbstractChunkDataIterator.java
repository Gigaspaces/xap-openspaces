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

package org.openspaces.persistency.hibernate.iterator;

import com.gigaspaces.datasource.DataIterator;
import com.j_spaces.core.client.SQLQuery;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.openspaces.persistency.support.ConcurrentMultiDataIterator;
import org.openspaces.persistency.support.MultiDataIterator;
import org.openspaces.persistency.support.SerialMultiDataIterator;

import java.util.ArrayList;

/**
 * A base class that accespts a batch size and will create several iterators on the given
 * entity by chunking it into batch size chuncks, each iterator will iterate only on the given
 * chunk.
 *
 * @author kimchy
 */
public abstract class AbstractChunkDataIterator implements MultiDataIterator {

    protected final String entityName;

    protected final SQLQuery sqlQuery;

    protected final String hQuery;

    protected final SessionFactory sessionFactory;

    protected final boolean perfromOrderById;

    protected final int fetchSize;

    protected final int chunkSize;

    private DataIterator[] iterators;

    private MultiDataIterator multiDataIterator;

    /**
     * Constructs a scrollable iterator over the given entity name.
     *
     * @param entityName       The entity name to scroll over
     * @param sessionFactory   The session factory to use to construct the session
     * @param fetchSize        The fetch size of the scrollabale result set
     * @param performOrderById Should the query perform order by id or not
     * @param chunkSize        The size of the chunks the entity table will be broken to
     */
    public AbstractChunkDataIterator(String entityName, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int chunkSize) {
        this.entityName = entityName;
        this.sqlQuery = null;
        this.hQuery = null;
        this.sessionFactory = sessionFactory;
        this.fetchSize = fetchSize;
        this.perfromOrderById = performOrderById;
        this.chunkSize = chunkSize;
    }

    /**
     * Constructs a scrollable iterator over the given GigaSpaces <code>SQLQuery</code>.
     *
     * @param sqlQuery         The <code>SQLQuery</code> to scroll over
     * @param sessionFactory   The session factory to use to construct the session
     * @param fetchSize        The fetch size of the scrollabale result set
     * @param performOrderById Should the query perform order by id or not
     * @param chunkSize        The size of the chunks the entity table will be broken to
     */
    public AbstractChunkDataIterator(SQLQuery sqlQuery, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int chunkSize) {
        this.sqlQuery = sqlQuery;
        this.entityName = null;
        this.hQuery = null;
        this.sessionFactory = sessionFactory;
        this.fetchSize = fetchSize;
        this.perfromOrderById = performOrderById;
        this.chunkSize = chunkSize;
    }

    /**
     * Constructs a scrollable iterator over the given hibernate query string.
     *
     * @param hQuery         The hiberante query string to scroll over
     * @param sessionFactory The session factory to use to construct the session
     * @param fetchSize      The fetch size of the scrollabale result set
     * @param chunkSize      The size of the chunks the entity table will be broken to
     */
    public AbstractChunkDataIterator(String hQuery, SessionFactory sessionFactory, int fetchSize, int chunkSize) {
        this.sqlQuery = null;
        this.entityName = null;
        this.hQuery = hQuery;
        this.sessionFactory = sessionFactory;
        this.fetchSize = fetchSize;
        this.perfromOrderById = false;
        this.chunkSize = chunkSize;
    }

    public DataIterator[] iterators() {
        initIterators();
        return this.iterators;
    }

    public boolean hasNext() {
        initIterators();
        if (multiDataIterator == null) {
            if (iterators.length == 1) {
                multiDataIterator = new SerialMultiDataIterator(iterators);
            } else {
                multiDataIterator = new ConcurrentMultiDataIterator(iterators, 10);
            }
        }
        return multiDataIterator.hasNext();
    }

    public Object next() {
        return multiDataIterator.next();
    }

    public void remove() {
        multiDataIterator.remove();
    }

    public void close() {
        if (multiDataIterator != null) {
            try {
                multiDataIterator.close();
            } finally {
                multiDataIterator = null;
            }
        }
    }

    private void initIterators() {
        if (iterators == null) {
            ArrayList<DataIterator> itList = new ArrayList<DataIterator>();
            Session session = sessionFactory.openSession();
            Transaction transaction = session.beginTransaction();
            try {
                Criteria criteria = session.createCriteria(entityName);
                criteria.setProjection(Projections.rowCount());
                int count = ((Number) criteria.uniqueResult()).intValue();
                int from = 0;
                while (from < count) {
                    if (entityName != null) {
                        itList.add(createIteartorByEntityName(entityName, sessionFactory, fetchSize, perfromOrderById, from, chunkSize));
                    } else if (sqlQuery != null) {
                        itList.add(createIteartorBySQLQuery(sqlQuery, sessionFactory, fetchSize, perfromOrderById, from, chunkSize));
                    } else if (hQuery != null) {
                        itList.add(createIteartorByHibernateQuery(hQuery, sessionFactory, fetchSize, from, chunkSize));
                    }
                    from += chunkSize;
                }
                iterators = itList.toArray(new DataIterator[itList.size()]);
            } finally {
                transaction.commit();
                session.close();
            }
        }
    }

    protected abstract DataIterator createIteartorByEntityName(String entityName, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int from, int size);

    protected abstract DataIterator createIteartorBySQLQuery(SQLQuery sqlQuery, SessionFactory sessionFactory, int fetchSize, boolean performOrderById, int from, int size);

    protected abstract DataIterator createIteartorByHibernateQuery(String hQuery, SessionFactory sessionFactory, int fetchSize, int from, int size);
}
