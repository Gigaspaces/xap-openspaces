package org.openspaces.itest.core.defaultmodifiers;

import java.util.Set;

import javax.annotation.Resource;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.core.GigaSpace;
import org.springframework.core.Constants;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gigaspaces.client.ReadModifiers;
import com.google.common.collect.Sets;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/org/openspaces/itest/core/defaultmodifiers/read-modifiers-context.xml")
public class DefaultReadModifiersTest {

    @Resource GigaSpace empty;
    @Resource GigaSpace none;
    @Resource GigaSpace dirtyRead;
    @Resource GigaSpace exclusiveReadLock;
    @Resource GigaSpace fifoGroupingPoll;
    @Resource GigaSpace fifo;
    @Resource GigaSpace ignorePartialFailure;
    @Resource GigaSpace memoryOnlySearch;
    @Resource GigaSpace readCommitted;
    @Resource GigaSpace repeatableRead;
    @Resource GigaSpace memoryOnlySearchAndRepeatableRead;
    
    @Test
    public void testReadModifiers() {
        assertModifiers(empty, ReadModifiers.NONE);
        assertModifiers(none, ReadModifiers.NONE);
        assertModifiers(dirtyRead, ReadModifiers.DIRTY_READ);
        assertModifiers(exclusiveReadLock, ReadModifiers.EXCLUSIVE_READ_LOCK);
        assertModifiers(fifoGroupingPoll, ReadModifiers.FIFO_GROUPING_POLL);
        assertModifiers(fifo, ReadModifiers.FIFO);
        assertModifiers(ignorePartialFailure, ReadModifiers.IGNORE_PARTIAL_FAILURE);
        assertModifiers(memoryOnlySearch, ReadModifiers.MEMORY_ONLY_SEARCH);
        assertModifiers(readCommitted, ReadModifiers.READ_COMMITTED);
        assertModifiers(repeatableRead, ReadModifiers.REPEATABLE_READ);
        assertModifiers(memoryOnlySearchAndRepeatableRead, ReadModifiers.MEMORY_ONLY_SEARCH.add(ReadModifiers.REPEATABLE_READ));
        
        assertAllModifiersCovered();
    }
    
    private void assertModifiers(GigaSpace gigaSpace, ReadModifiers expectedReadModifiers) {
        ReadModifiers actualReadModifiers = gigaSpace.getDefaultReadModifiers();
        Assert.assertEquals("Wrong read modifiers", 
                            expectedReadModifiers.getCode(), actualReadModifiers.getCode());
    }
    
    // sanity test so that something fails when a new modifier is added as
    // a reminder to update the xsd
    private void assertAllModifiersCovered() {
        Set<String> coveredModifiers = Sets.newHashSet("NONE", 
                                                       "DIRTY_READ",
                                                       "EXCLUSIVE_READ_LOCK",
                                                       "FIFO_GROUPING_POLL",
                                                       "FIFO",
                                                       "IGNORE_PARTIAL_FAILURE",
                                                       "MEMORY_ONLY_SEARCH",
                                                       "READ_COMMITTED",
                                                       "REPEATABLE_READ");
        
        Set<String> actualModifiers = new Constants(ReadModifiers.class).getNames("");
        
        Assert.assertEquals("Missing modifier should be added to openspaces-core.xsd!", 
                            coveredModifiers, actualModifiers);
    }
}
