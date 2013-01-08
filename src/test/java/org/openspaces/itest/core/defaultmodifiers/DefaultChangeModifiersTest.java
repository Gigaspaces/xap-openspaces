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

import com.gigaspaces.client.ChangeModifiers;
import com.google.common.collect.Sets;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/org/openspaces/itest/core/defaultmodifiers/change-modifiers-context.xml")
public class DefaultChangeModifiersTest {

    @Resource GigaSpace empty;
    @Resource GigaSpace none;
    @Resource GigaSpace memoryOnly;
    @Resource GigaSpace oneWay;
    @Resource GigaSpace returnDetailedResults;
    @Resource GigaSpace memoryOnlyAndOneWay;
    
    @Test
    public void testChangeModifiers() {
        assertModifiers(empty, ChangeModifiers.NONE);
        assertModifiers(none, ChangeModifiers.NONE);
        assertModifiers(memoryOnly, ChangeModifiers.MEMORY_ONLY_SEARCH);
        assertModifiers(oneWay, ChangeModifiers.ONE_WAY);
        assertModifiers(returnDetailedResults, ChangeModifiers.RETURN_DETAILED_RESULTS);
        assertModifiers(memoryOnlyAndOneWay, ChangeModifiers.MEMORY_ONLY_SEARCH.add(ChangeModifiers.ONE_WAY));
        
        assertAllModifiersCovered();
    }
    
    private void assertModifiers(GigaSpace gigaSpace, ChangeModifiers expectedChangeModifiers) {
        ChangeModifiers actualChangeModifiers = gigaSpace.getDefaultChangeModifiers();
        Assert.assertEquals("Wrong change modifiers", 
                            expectedChangeModifiers.getCode(), actualChangeModifiers.getCode());
    }
    
    // sanity test so that something fails when a new modifier is added as
    // a reminder to update the xsd
    private void assertAllModifiersCovered() {
        Set<String> coveredModifiers = Sets.newHashSet("NONE", 
                                                       "MEMORY_ONLY_SEARCH",
                                                       "ONE_WAY",
                                                       "RETURN_DETAILED_RESULTS");
        
        Set<String> actualModifiers = new Constants(ChangeModifiers.class).getNames("");
        
        Assert.assertEquals("Missing modifier should be added to openspaces-core.xsd!", 
                            coveredModifiers, actualModifiers);
    }
}
