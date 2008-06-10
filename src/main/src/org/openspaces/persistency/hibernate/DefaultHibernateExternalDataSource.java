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

package org.openspaces.persistency.hibernate;

import com.gigaspaces.datasource.BulkDataPersister;
import com.gigaspaces.datasource.BulkItem;
import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.datasource.SQLDataProvider;
import com.j_spaces.core.client.SQLQuery;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.openspaces.persistency.hibernate.iterator.DefaultChunkScrollableDataIterator;
import org.openspaces.persistency.hibernate.iterator.DefaultListQueryDataIterator;
import org.openspaces.persistency.hibernate.iterator.DefaultScrollableDataIterator;
import org.openspaces.persistency.hibernate.iterator.HibernateProxyRemoverIterator;

import java.util.List;

/**
 * The default Hiberante extenral data source implemenation. Based on Hiberante {@link Session}.
 *
 * @author kimchy
 */
public class DefaultHibernateExternalDataSource extends AbstractHibernateExternalDataSource implements BulkDataPersister, SQLDataProvider {

    /**
     * Perform the given bulk changes using Hibernate {@link org.hibernate.Session}.
     *
     * <p>Note, this implemenation relies on Hibernate {@link org.hibernate.NonUniqueObjectException} in case
     * the entity is alredy associated wih the given session, and in such a case, will result in performing
     * merge operation (which is more expensive).
     */
    public void executeBulk(List<BulkItem> bulkItems) throws DataSourceException {
        Session session = getSessionFactory().openSession();
        Transaction tr = session.beginTransaction();
        Object latest = null;
        try {
            for (BulkItem bulkItem : bulkItems) {
                Object entry = bulkItem.getItem();
                if (!isManagedEntry(entry.getClass().getName())) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Entry [" + entry + "] is not managed, filtering it out");
                    }
                    continue;
                }
                latest = entry;
                switch (bulkItem.getOperation()) {
                    case BulkItem.REMOVE:
                        if (logger.isTraceEnabled()) {
                            logger.trace("Deleting Entry [" + entry + "]");
                        }
                        try {
                            session.delete(entry);
                        } catch (NonUniqueObjectException e) {
                            session.delete(session.merge(entry));
                        }
                        break;
                    case BulkItem.WRITE:
                        if (logger.isTraceEnabled()) {
                            logger.trace("Write Entry [" + entry + "]");
                        }
                        try {
                            session.saveOrUpdate(entry);
                        } catch (NonUniqueObjectException e) {
                            session.merge(entry);
                        }
                        break;
                    case BulkItem.UPDATE:
                        if (logger.isTraceEnabled()) {
                            logger.trace("Update Entry [" + entry + "]");
                        }
                        try {
                            session.saveOrUpdate(entry);
                        } catch (NonUniqueObjectException e) {
                            session.merge(entry);
                        }
                        break;
                    default:
                        break;
                }
            }
            tr.commit();
        } catch (Exception e) {
            rollbackTx(tr);
            throw new DataSourceException("Failed to execute bulk operation, latest object [" + latest + "]", e);
        } finally {
            closeSession(session);
        }
    }

    /**
     * Returns a {@link org.openspaces.persistency.hibernate.iterator.DefaultListQueryDataIterator} for the
     * given sql query.
     */
    public DataIterator iterator(SQLQuery sqlQuery) throws DataSourceException {
        if (logger.isTraceEnabled()) {
            logger.trace("Iterator over query [" + sqlQuery + "]");
        }
        return new HibernateProxyRemoverIterator(new DefaultListQueryDataIterator(sqlQuery, getSessionFactory()));
    }

    /**
     * Performs the inital load operation. Iterates over the {@link #setInitialLoadEntries(String[])} inital load
     * entries. If {@link #getInitalLoadChunkSize()} is set to <code>-1</code>, will use
     * {@link org.openspaces.persistency.hibernate.iterator.DefaultScrollableDataIterator} for each entity. If
     * {@link # getInitalLoadChunkSize ()} is set to a non <code>-1</code> value, will use the
     * {@link org.openspaces.persistency.hibernate.iterator.DefaultChunkScrollableDataIterator}.
     */
    public DataIterator initialLoad() throws DataSourceException {
        DataIterator[] iterators = new DataIterator[getInitialLoadEntries().length];
        int iteratorCounter = 0;
        for (String entityName : getInitialLoadEntries()) {
            if (getInitalLoadChunkSize() == -1) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Creating inital load scrollable iterator for entry [" + entityName + "]");
                }
                iterators[iteratorCounter++] = new DefaultScrollableDataIterator(entityName, getSessionFactory(), getFetchSize(), isPerformOrderById());
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("Creating inital load chunk scrollable iterator for entry [" + entityName + "]");
                }
                iterators[iteratorCounter++] = new DefaultChunkScrollableDataIterator(entityName, getSessionFactory(), getFetchSize(), isPerformOrderById(), getInitalLoadChunkSize());
            }
        }
        return createInitialLoadIterator(iterators);
    }

    private void rollbackTx(Transaction tr) {
        try {
            tr.rollback();
        } catch (Exception e) {
            // ignore this exception
        }
    }

    private void closeSession(Session session) {
        if (session.isOpen()) {
            session.close();
        }
    }
}
