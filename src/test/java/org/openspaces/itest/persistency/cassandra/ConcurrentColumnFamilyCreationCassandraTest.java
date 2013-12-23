package org.openspaces.itest.persistency.cassandra;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openspaces.itest.persistency.common.mock.MockIntroduceTypeData;
import org.openspaces.persistency.cassandra.CassandraSpaceSynchronizationEndpoint;
import org.openspaces.persistency.cassandra.HectorCassandraClient;

import com.gigaspaces.metadata.SpaceTypeDescriptorBuilder;


public class ConcurrentColumnFamilyCreationCassandraTest extends AbstractCassandraTest
{

    private final int numberOfThreads = 20;

    SyncEndpointInitiator[] endpointInitiators = new SyncEndpointInitiator[numberOfThreads];
    TestThread[] typeIntroducers = new TestThread[numberOfThreads];

    @Test
    public void testConcurrentColumnFamilyCreation() throws Exception
    {
        testConcurrentMetaDataColumnFamilyCreation();
        testConcurrentRegularColumnFamilyCreation();
    }

    // First test concurrent column family metadata metadata creation 
    // (our internal column family that holds information on different types)
    // The creation attempt is executed during the endpoint construction
    // so it is sufficient to simply create many endpoints concurrently (of course we are
    // testing a concurrency issue here so nothing is guaranteed)
    private void testConcurrentMetaDataColumnFamilyCreation() throws Exception
    {
        for (int i = 0; i < endpointInitiators.length; i++)
        {
            endpointInitiators[i] = new SyncEndpointInitiator();
            endpointInitiators[i].start();
        }
        for (TestThread initiator : endpointInitiators)
            initiator.waitForThreadAndCheckValidExecution();
    }
    
    // Now lets test the same thing but with normal type introduction
    // (not the metadata type)
    private void testConcurrentRegularColumnFamilyCreation() throws Exception
    {
        for (int i = 0; i < endpointInitiators.length; i++)
        {
            final int index = i;
            typeIntroducers[i] = new TestThread() 
            {
                public void runImpl() throws Exception
                {
                    endpointInitiators[index].endpoint.onIntroduceType(new MockIntroduceTypeData(
                         new SpaceTypeDescriptorBuilder("MockType")
                            .addFixedProperty("id", String.class)
                            .addFixedProperty("name", String.class)
                            .idProperty("id", false)
                            .create()));
                }
            };
            typeIntroducers[i].start();
        }
        for (TestThread typeIntroducer : typeIntroducers)
            typeIntroducer.waitForThreadAndCheckValidExecution();
    }

    private class SyncEndpointInitiator extends TestThread
    {
        HectorCassandraClient                 hectorClient;
        CassandraSpaceSynchronizationEndpoint endpoint;
        int                                   index = 0;

        public void close()
        {
            if (hectorClient != null)
                hectorClient.close();
        }

        @Override
        public void runImpl()
        {
            hectorClient = createCassandraHectorClient("cluster-sync" + (index++));
            endpoint = createCassandraSyncEndpointInterceptor(hectorClient);
        }
    }

    @Override
    @Before
    public void initialSetup()
    {
        server.initialize(isEmbedded());
    }

    @Override
    @After
    public void finalTeardown()
    {
        server.destroy();
        if (endpointInitiators != null) 
        {
            for (int i = 0; i < endpointInitiators.length; i++)
                endpointInitiators[i].close();
        }
    }
    
    abstract private class TestThread extends Thread {
        
        BlockingQueue<Object>  error = new ArrayBlockingQueue<Object>(1);
        
        @Override
        public void run()
        {
            try
            {
                runImpl();
                error.add(new Object());
            }
            catch (Exception e)
            {
                error.add(e);
            }
        }
        
        public void waitForThreadAndCheckValidExecution() throws Exception
        {
            Object o = error.take();
            if (o instanceof Exception)
                throw (Exception) o;
        }
        
        abstract public void runImpl() throws Exception;
        
    }
    
}
