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

import com.gigaspaces.client.ClearModifiers;
import com.google.common.collect.Sets;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/org/openspaces/itest/core/defaultmodifiers/clear-modifiers-context.xml")
public class DefaultClearModifiersTest {

    @Resource GigaSpace empty;
    @Resource GigaSpace none;
    @Resource GigaSpace evictOnly;
    @Resource GigaSpace memoryOnlySearch;
    @Resource GigaSpace evictOnlyAndMemoryOnlySearch;
    
    @Test
    public void testClearModifiers() {
        assertModifiers(empty, ClearModifiers.NONE);
        assertModifiers(none, ClearModifiers.NONE);
        assertModifiers(evictOnly, ClearModifiers.EVICT_ONLY);
        assertModifiers(memoryOnlySearch, ClearModifiers.MEMORY_ONLY_SEARCH);
        assertModifiers(evictOnlyAndMemoryOnlySearch, ClearModifiers.EVICT_ONLY.add(ClearModifiers.MEMORY_ONLY_SEARCH));
        
        assertAllModifiersCovered();
    }
    
    private void assertModifiers(GigaSpace gigaSpace, ClearModifiers expectedClearModifiers) {
        ClearModifiers actualClearModifiers = gigaSpace.getDefaultClearModifiers();
        Assert.assertEquals("Wrong clear modifiers", 
                            expectedClearModifiers.getCode(), actualClearModifiers.getCode());
    }
    
    // sanity test so that something fails when a new modifier is added as
    // a reminder to update the xsd
    private void assertAllModifiersCovered() {
        Set<String> coveredModifiers = Sets.newHashSet("NONE", 
                                                       "EVICT_ONLY",
                                                       "MEMORY_ONLY_SEARCH");
        
        Set<String> actualModifiers = new Constants(ClearModifiers.class).getNames("");
        
        Assert.assertEquals("Missing modifier should be added to openspaces-core.xsd!", 
                            coveredModifiers, actualModifiers);
    }
}
