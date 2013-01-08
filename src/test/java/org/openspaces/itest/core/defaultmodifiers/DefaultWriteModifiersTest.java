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

import com.gigaspaces.client.WriteModifiers;
import com.google.common.collect.Sets;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/org/openspaces/itest/core/defaultmodifiers/write-modifiers-context.xml")
public class DefaultWriteModifiersTest {

    @Resource GigaSpace empty;
    @Resource GigaSpace none;
    @Resource GigaSpace memoryOnly;
    @Resource GigaSpace oneWay;
    @Resource GigaSpace returnPrev;
    @Resource GigaSpace upateOnly;
    @Resource GigaSpace writeOnly;
    @Resource GigaSpace updateOrWrite;
    @Resource GigaSpace partialUpdate;
    @Resource GigaSpace memoryOnlyAndOneWay;
    
    @Test
    public void testWriteModifiers() {
        assertModifiers(empty, WriteModifiers.NONE);
        assertModifiers(none, WriteModifiers.NONE);
        assertModifiers(memoryOnly, WriteModifiers.MEMORY_ONLY_SEARCH);
        assertModifiers(oneWay, WriteModifiers.ONE_WAY);
        assertModifiers(returnPrev, WriteModifiers.RETURN_PREV_ON_UPDATE);
        assertModifiers(upateOnly, WriteModifiers.UPDATE_ONLY);
        assertModifiers(writeOnly, WriteModifiers.WRITE_ONLY);
        assertModifiers(updateOrWrite, WriteModifiers.UPDATE_OR_WRITE);
        assertModifiers(partialUpdate, WriteModifiers.PARTIAL_UPDATE);
        assertModifiers(memoryOnlyAndOneWay, WriteModifiers.MEMORY_ONLY_SEARCH.add(WriteModifiers.ONE_WAY));
        
        assertAllModifiersCovered();
    }
    
    private void assertModifiers(GigaSpace gigaSpace, WriteModifiers expectedWriteModifiers) {
        WriteModifiers actualWriteModifiers = gigaSpace.getDefaultWriteModifiers();
        Assert.assertEquals("Wrong write modifiers", 
                            expectedWriteModifiers.getCode(), actualWriteModifiers.getCode());
    }
    
    // sanity test so that something fails when a new modifier is added as
    // a reminder to update the xsd
    private void assertAllModifiersCovered() {
        Set<String> coveredModifiers = Sets.newHashSet("NONE", 
                                                       "MEMORY_ONLY_SEARCH",
                                                       "ONE_WAY",
                                                       "RETURN_PREV_ON_UPDATE",
                                                       "UPDATE_ONLY",
                                                       "WRITE_ONLY",
                                                       "UPDATE_OR_WRITE",
                                                       "PARTIAL_UPDATE");
        
        Set<String> actualModifiers = new Constants(WriteModifiers.class).getNames("");
        
        Assert.assertEquals("Missing modifier should be added to openspaces-core.xsd!", 
                            coveredModifiers, actualModifiers);
    }
}
