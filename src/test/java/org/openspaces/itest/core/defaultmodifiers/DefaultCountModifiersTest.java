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

import com.gigaspaces.client.CountModifiers;
import com.google.common.collect.Sets;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/org/openspaces/itest/core/defaultmodifiers/count-modifiers-context.xml")
public class DefaultCountModifiersTest {

    @Resource GigaSpace empty;
    @Resource GigaSpace none;
    @Resource GigaSpace dirtyRead;
    @Resource GigaSpace exclusiveReadLock;
    @Resource GigaSpace memoryOnlySearch;
    @Resource GigaSpace readCommitted;
    @Resource GigaSpace repeatableRead;
    @Resource GigaSpace memoryOnlySearchAndDirtyRead;
    
    @Test
    public void testCountModifiers() {
        assertModifiers(empty, CountModifiers.NONE);
        assertModifiers(none, CountModifiers.NONE);
        assertModifiers(dirtyRead, CountModifiers.DIRTY_READ);
        assertModifiers(exclusiveReadLock, CountModifiers.EXCLUSIVE_READ_LOCK);
        assertModifiers(memoryOnlySearch, CountModifiers.MEMORY_ONLY_SEARCH);
        assertModifiers(readCommitted, CountModifiers.READ_COMMITTED);
        assertModifiers(repeatableRead, CountModifiers.REPEATABLE_READ);
        assertModifiers(memoryOnlySearchAndDirtyRead, CountModifiers.MEMORY_ONLY_SEARCH.add(CountModifiers.DIRTY_READ));
        
        assertAllModifiersCovered();
    }
    
    private void assertModifiers(GigaSpace gigaSpace, CountModifiers expectedCountModifiers) {
        CountModifiers actualCountModifiers = gigaSpace.getDefaultCountModifiers();
        Assert.assertEquals("Wrong count modifiers", 
                            expectedCountModifiers.getCode(), actualCountModifiers.getCode());
    }
    
    // sanity test so that something fails when a new modifier is added as
    // a reminder to update the xsd
    private void assertAllModifiersCovered() {
        Set<String> coveredModifiers = Sets.newHashSet("NONE", 
                                                       "DIRTY_READ",
                                                       "EXCLUSIVE_READ_LOCK",
                                                       "MEMORY_ONLY_SEARCH",
                                                       "READ_COMMITTED",
                                                       "REPEATABLE_READ");
        
        Set<String> actualModifiers = new Constants(CountModifiers.class).getNames("");
        
        Assert.assertEquals("Missing modifier should be added to openspaces-core.xsd!", 
                            coveredModifiers, actualModifiers);
    }
}
