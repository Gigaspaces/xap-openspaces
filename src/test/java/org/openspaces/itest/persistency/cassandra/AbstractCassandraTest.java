package org.openspaces.itest.persistency.cassandra;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;
import com.gigaspaces.metadata.index.SpaceIndexType;
import org.apache.cassandra.cql.jdbc.CassandraDataSource;
import org.junit.After;
import org.junit.Before;
import org.openspaces.itest.persistency.common.mock.MockIntroduceTypeData;
import org.openspaces.persistency.cassandra.*;
import org.openspaces.persistency.cassandra.meta.mapping.filter.FlattenedPropertiesFilter;
import org.openspaces.persistency.cassandra.meta.mapping.filter.PropertyContext;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

abstract public class AbstractCassandraTest
{
    protected static final Random                         random        = new Random();

    private static final String                           CQL_VERSION   = "2.0.0";
    private static final String                           LOCALHOST     = "127.0.0.1";
    private static final String                           DEFAULT_AUTH  = "default";
    
    protected final CassandraTestServer server = new CassandraTestServer();
    protected CassandraSpaceSynchronizationEndpoint _syncInterceptor;
    protected CassandraSpaceDataSource _dataSource;
    private HectorCassandraClient _syncInterceptorHectorClient;
    private HectorCassandraClient _dataSourceHectorClient;
    
    @Before
    public void initialSetup()
    {
       
    	server.initialize(isEmbedded());
    	
        _syncInterceptorHectorClient = createCassandraHectorClient("cluster-sync");
        _syncInterceptor = createCassandraSyncEndpointInterceptor(_syncInterceptorHectorClient);
        _dataSourceHectorClient = createCassandraHectorClient("cluster-datasource");
        _dataSource = createCassandraSpaceDataSource(_dataSourceHectorClient);
    }
    
    @After
    public void finalTeardown()
    {
    	if (_syncInterceptorHectorClient != null) {
    	    _syncInterceptorHectorClient.close();
    	}
    	
        if (_dataSourceHectorClient != null) {
            _dataSourceHectorClient.close();
        }
    	
    	if (_dataSource != null) {
    		_dataSource.close();
    	}
    	
    	server.destroy();
    }

    protected boolean isEmbedded()
    {
        return false;
    }
    
    protected HectorCassandraClient createCassandraHectorClient(String clusterName)
    {
        HectorCassandraClient hectorClient = 
                new HectorCassandraClientConfigurer()
                .hosts(LOCALHOST)
                .port(server.getPort())
                .keyspaceName(server.getKeySpaceName())
                .clusterName(clusterName)
                .create();
        
        return hectorClient;
    }
    
    protected CassandraSpaceSynchronizationEndpoint createCassandraSyncEndpointInterceptor(
            HectorCassandraClient hectorClient)
    {
        CassandraSpaceSynchronizationEndpoint syncInterceptor = 
                new CassandraSpaceSynchronizationEndpointConfigurer()
                    .flattenedPropertiesFilter(new FlattenedPropertiesFilter() {
                        public boolean shouldFlatten(PropertyContext context) {
                            return context.getCurrentNestingLevel() <= 10;
                        }
                    })
                    .hectorClient(hectorClient).create();
        return syncInterceptor;
    }

    protected CassandraSpaceDataSource createCassandraSpaceDataSource(HectorCassandraClient hectorClient)
    {
        CassandraDataSource ds = createCassandraDataSource();
        CassandraSpaceDataSource dataSource = new CassandraSpaceDataSource(null,
                                                                           null, 
                                                                           ds,
                                                                           hectorClient,
                                                                           5,
                                                                           30,
                                                                           10000,
                                                                           null,
				                                                           true,
                                                                           null);
        return dataSource;
    }

    protected CassandraDataSource createCassandraDataSource()
    {
        return new CassandraDataSource(LOCALHOST,
                                       server.getPort(),
                                       server.getKeySpaceName(),
                                       DEFAULT_AUTH,
                                       DEFAULT_AUTH,
                                       CQL_VERSION);
    }
    
    protected MockIntroduceTypeData createIntroduceTypeDataFromSpaceDocument(
            SpaceDocument document, String key)
    {
        return createIntroduceTypeDataFromSpaceDocument(document, key, new HashSet<String>());
    }
    
    protected MockIntroduceTypeData createIntroduceTypeDataFromSpaceDocument(
            SpaceDocument document, String key, Set<String> indexes)
    {
        SpaceTypeDescriptorBuilder builder = new SpaceTypeDescriptorBuilder(document.getTypeName());
        for (Entry<String, Object> entry : document.getProperties().entrySet())
            builder.addFixedProperty(entry.getKey(), entry.getValue().getClass());
        for (String index : indexes)
            builder.addPathIndex(index, SpaceIndexType.BASIC);
        builder.idProperty(key);
        return new MockIntroduceTypeData(builder.create());
    }
    
}
