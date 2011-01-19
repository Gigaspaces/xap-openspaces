package org.openspaces.itest.core.space.mirror;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.datasource.DataSourceException;
import com.gigaspaces.datasource.ManagedDataSource;

import java.util.Properties;

/**
 * @author kimchy
 */
public class MockManagedDataSource implements ManagedDataSource {

    private boolean initCalled;

    public void init(Properties properties) throws DataSourceException {
        initCalled = true;
    }

    public void shutdown() throws DataSourceException {
    }

    public boolean isInitCalled() {
        return initCalled;
    }

    public DataIterator initialLoad() throws DataSourceException {
        return null;
    }
}
