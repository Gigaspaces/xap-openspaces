package org.openspaces.itest.persistency.cassandra;

import com.gigaspaces.datasource.DataIterator;
import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.metadata.SpaceTypeDescriptor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openspaces.itest.persistency.common.data.TestPojoCyclicBottom;
import org.openspaces.itest.persistency.common.data.TestPojoCyclicTop;
import org.openspaces.itest.persistency.common.mock.MockDataSourceQuery;
import org.openspaces.itest.persistency.common.mock.MockDataSourceSqlQuery;
import org.openspaces.itest.persistency.common.mock.MockIntroduceTypeData;
import org.openspaces.itest.persistency.common.mock.MockOperationsBatchDataBuilder;

import java.util.HashSet;
import java.util.Set;

public class CyclicReferencePropertyCassandraTest extends AbstractCassandraTest
{

    private SpaceDocument _topLevelSpaceDocument;
    private SpaceTypeDescriptor _typeDescriptor;

    @Before
    public void before()
    {
        _topLevelSpaceDocument = createTopLevelDocument();
        
        SpaceDocument indexes = new SpaceDocument("indexes")
            .setProperty("top.number", null)
            .setProperty("top.bottom.number", null)
            .setProperty("top.bottom.top.number", null)
            .setProperty("top.bottom.top.bottom.number", null)
            .setProperty("top.bottom.top.bottom.top.number", null)
            .setProperty("top.bottom.top.bottom.top.bottom.number", null)
            .setProperty("top.bottom.top.bottom.top.bottom.top.number", null)
            .setProperty("top.bottom.top.bottom.top.bottom.top.bottom.number", null)
            .setProperty("top.bottom.top.bottom.top.bottom.top.bottom.top.number", null)
            .setProperty("top.bottom.top.bottom.top.bottom.top.bottom.top.bottom.number", null)
            .setProperty("top.bottom.top.bottom.top.bottom.top.bottom.top.bottom.top.number", null);
        
        MockIntroduceTypeData typeData = createIntroduceTypeDataFromSpaceDocument(_topLevelSpaceDocument, "key", indexes.getProperties().keySet());
        _typeDescriptor = typeData.getTypeDescriptor();
        
        _syncInterceptor.onIntroduceType(typeData);
        
        MockOperationsBatchDataBuilder builder = new MockOperationsBatchDataBuilder();
        builder.write(_topLevelSpaceDocument, "key");
        _syncInterceptor.onOperationsBatchSynchronization(builder.build());
        
        _dataSource.initialMetadataLoad();
    }
    
    @Test
    public void test()
    {
        assertNestingLevel ("");
        assertNestingLevel ("top.number = ?");
        assertNestingLevel ("top.bottom.number = ?");
        assertNestingLevel ("top.bottom.top.number = ?");
        assertNestingLevel ("top.bottom.top.bottom.number = ?");
        assertNestingLevel ("top.bottom.top.bottom.top.number = ?");
        assertNestingLevel ("top.bottom.top.bottom.top.bottom.number = ?");
        assertNestingLevel ("top.bottom.top.bottom.top.bottom.top.number = ?");
        assertNestingLevel ("top.bottom.top.bottom.top.bottom.top.bottom.number = ?");
        assertNestingLevel ("top.bottom.top.bottom.top.bottom.top.bottom.top.number = ?");
        assertNestingLevel ("top.bottom.top.bottom.top.bottom.top.bottom.top.bottom.number = ?");
        assertEntryNotFound("top.bottom.top.bottom.top.bottom.top.bottom.top.bottom.top.number = ?");
    }
    
    private void assertEntryNotFound(String query)
    {
        DataIterator<Object> iterator = getDataIterator(query);
        Assert.assertFalse("Unexpected result", iterator.hasNext());
    }

    @SuppressWarnings("all")
    private void assertNestingLevel(String query)
    {
        DataIterator<Object> iterator = getDataIterator(query);
        Assert.assertTrue("Missing result", iterator.hasNext());
        SpaceDocument result = (SpaceDocument) iterator.next();
        TestPojoCyclicTop top = result.getProperty("top");
        TestPojoCyclicBottom buttom = null;
        
        // top is no longer cyclic
        Set<TestPojoCyclicTop> tops = new HashSet<TestPojoCyclicTop>();
        Set<TestPojoCyclicBottom> buttoms = new HashSet<TestPojoCyclicBottom>();
        
        boolean currentlyTop = true;
        while (true)
        {
            if (currentlyTop)
            {
                if (!tops.add(top))
                    break;
                currentlyTop = false;
                buttom = top.getBottom();
            }
            else
            {
                if (!buttoms.add(buttom))
                    break;
                currentlyTop = true;
                top = buttom.getTop();
            }
        }
        
        Assert.assertEquals("tops count", 6, tops.size());
        Assert.assertEquals("buttoms count", 6, buttoms.size());
        
        iterator.close();
    }

    private DataIterator<Object> getDataIterator(String query)
    {
        MockDataSourceSqlQuery sqlQuery = new MockDataSourceSqlQuery(query, new Object[] { 1 });
        MockDataSourceQuery sourceQuery = new MockDataSourceQuery(_typeDescriptor, sqlQuery, Integer.MAX_VALUE);
        DataIterator<Object> iterator = _dataSource.getDataIterator(sourceQuery);
        return iterator;
    }
    
    private SpaceDocument createTopLevelDocument()
    {
        TestPojoCyclicTop top = new TestPojoCyclicTop();
        TestPojoCyclicBottom buttom = new TestPojoCyclicBottom();
        top.setBottom(buttom);
        buttom.setTop(top);
        return new SpaceDocument("CyclicRefernceType")
            .setProperty("key", random.nextLong())
            .setProperty("top", top);
    }
    
}
