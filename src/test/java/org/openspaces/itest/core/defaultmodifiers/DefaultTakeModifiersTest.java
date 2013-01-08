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

import com.gigaspaces.client.TakeModifiers;
import com.google.common.collect.Sets;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/org/openspaces/itest/core/defaultmodifiers/take-modifiers-context.xml")
public class DefaultTakeModifiersTest {

    @Resource GigaSpace empty;
    @Resource GigaSpace none;
    @Resource GigaSpace evictOnly;
    @Resource GigaSpace fifoGroupingPoll;
    @Resource GigaSpace fifo;
    @Resource GigaSpace ignorePatialFailure;
    @Resource GigaSpace memoryOnlySearch;
    @Resource GigaSpace memoryOnlyAndFifo;
    
    @Test
    public void testTakeModifiers() {
        assertModifiers(empty, TakeModifiers.NONE);
        assertModifiers(none, TakeModifiers.NONE);
        assertModifiers(evictOnly, TakeModifiers.EVICT_ONLY);
        assertModifiers(fifoGroupingPoll, TakeModifiers.FIFO_GROUPING_POLL);
        assertModifiers(fifo, TakeModifiers.FIFO);
        assertModifiers(ignorePatialFailure, TakeModifiers.IGNORE_PARTIAL_FAILURE);
        assertModifiers(memoryOnlySearch, TakeModifiers.MEMORY_ONLY_SEARCH);
        assertModifiers(memoryOnlyAndFifo, TakeModifiers.MEMORY_ONLY_SEARCH.add(TakeModifiers.FIFO));
        
        assertAllModifiersCovered();
    }
    
    private void assertModifiers(GigaSpace gigaSpace, TakeModifiers expectedTakeModifiers) {
        TakeModifiers actualTakeModifiers = gigaSpace.getDefaultTakeModifiers();
        Assert.assertEquals("Wrong take modifiers", 
                            expectedTakeModifiers.getCode(), actualTakeModifiers.getCode());
    }
    
    // sanity test so that something fails when a new modifier is added as
    // a reminder to update the xsd
    private void assertAllModifiersCovered() {
        Set<String> coveredModifiers = Sets.newHashSet("NONE", 
                                                       "EVICT_ONLY",
                                                       "FIFO_GROUPING_POLL",
                                                       "FIFO",
                                                       "IGNORE_PARTIAL_FAILURE",
                                                       "MEMORY_ONLY_SEARCH");
        
        Set<String> actualModifiers = new Constants(TakeModifiers.class).getNames("");
        
        Assert.assertEquals("Missing modifier should be added to openspaces-core.xsd!", 
                            coveredModifiers, actualModifiers);
    }
}
