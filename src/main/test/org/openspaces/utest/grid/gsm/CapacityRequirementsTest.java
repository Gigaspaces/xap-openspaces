package org.openspaces.utest.grid.gsm;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.grid.gsm.capacity.CapacityRequirements;
import org.openspaces.grid.gsm.capacity.CpuCapacityRequirement;
import org.openspaces.grid.gsm.capacity.MemoryCapacityRequirement;

public class CapacityRequirementsTest extends TestCase {

    private static final CapacityRequirements EMPTY = new CapacityRequirements();
    private static final CapacityRequirements CPU = new CapacityRequirements(new CpuCapacityRequirement(Fraction.ONE));
    private static final CapacityRequirements MEMORY = new CapacityRequirements(new MemoryCapacityRequirement(1L));
    private static final CapacityRequirements MEMORY_CPU = new CapacityRequirements(new MemoryCapacityRequirement(1L),new CpuCapacityRequirement(Fraction.ONE));
    private static final CapacityRequirements MEMORY2_CPU2 = new CapacityRequirements(new MemoryCapacityRequirement(2L),new CpuCapacityRequirement(Fraction.TWO));
    
    @Test
    public void testAdd() {
        Assert.assertEquals(MEMORY_CPU,MEMORY.add(CPU));
    }
    
    @Test
    public void testEqualsZero() {
        Assert.assertTrue(EMPTY.equalsZero());
        Assert.assertTrue(EMPTY.getRequirement(new CpuCapacityRequirement().getType()).equalsZero());
        Assert.assertFalse(CPU.equalsZero());
        Assert.assertFalse(CPU.getRequirement(new CpuCapacityRequirement().getType()).equalsZero());
    }
    
    @Test
    public void testNegativeCpuException() {
        try {
            MEMORY.subtract(CPU);
            Assert.fail("expected exception");
        } catch (IllegalArgumentException e) {

        }
    }
    
    @Test
    public void testNegativeMemoryException() {
        try {
            CPU.subtract(MEMORY);
            Assert.fail("expected exception");
        } catch (IllegalArgumentException e) {

        }
    }
    
    @Test
    public void testSubtractCpu() {
        Assert.assertEquals(EMPTY, CPU.subtract(CPU));
    }
    
    @Test
    public void testSubtractMemory() {
        Assert.assertEquals(CPU, MEMORY_CPU.subtract(MEMORY));
    }
    
    @Test
    public void testSubtractOrZero() {
        Assert.assertTrue(CPU.subtractOrZero(CPU).equalsZero());
        Assert.assertTrue(EMPTY.subtractOrZero(MEMORY_CPU).equalsZero());
    }
    
    @Test
    public void testGreaterOrEquals() {
        Assert.assertTrue(MEMORY_CPU.greaterOrEquals(MEMORY_CPU));
        Assert.assertTrue(MEMORY_CPU.add(CPU).greaterOrEquals(MEMORY_CPU));
        Assert.assertTrue(MEMORY_CPU.add(MEMORY).greaterOrEquals(MEMORY_CPU));
        Assert.assertFalse(MEMORY_CPU.greaterOrEquals(MEMORY_CPU.add(CPU)));
        Assert.assertTrue(MEMORY_CPU.multiply(2).greaterOrEquals(MEMORY_CPU));
        Assert.assertFalse(MEMORY_CPU.greaterOrEquals(MEMORY_CPU.multiply(2)));
    }
    
    @Test
    public void testGreaterThan() {
        Assert.assertFalse(MEMORY_CPU.greaterThan(MEMORY_CPU));
        Assert.assertTrue(MEMORY_CPU.add(CPU).greaterThan(MEMORY_CPU));
        Assert.assertTrue(MEMORY_CPU.add(MEMORY).greaterThan(MEMORY_CPU));
        Assert.assertFalse(MEMORY_CPU.greaterThan(MEMORY_CPU.add(CPU)));
        Assert.assertTrue(MEMORY_CPU.multiply(2).greaterThan(MEMORY_CPU));
        Assert.assertFalse(MEMORY_CPU.greaterThan(MEMORY_CPU.multiply(2)));
    }
    
    
    @Test
    public void testMin() {
        Assert.assertEquals(MEMORY_CPU,MEMORY_CPU.min(MEMORY_CPU));
        Assert.assertEquals(MEMORY,MEMORY_CPU.min(MEMORY));
        Assert.assertEquals(CPU,MEMORY_CPU.min(CPU));
    }
    
    @Test
    public void testMax() {
        Assert.assertEquals(MEMORY_CPU,MEMORY_CPU.max(MEMORY_CPU));
        Assert.assertEquals(MEMORY_CPU,MEMORY_CPU.max(MEMORY));
        Assert.assertEquals(MEMORY_CPU,MEMORY_CPU.max(CPU));
    }
    
    @Test
    public void testMultiply() {
        Assert.assertEquals(MEMORY2_CPU2,MEMORY_CPU.multiply(2));
    }
    
    @Test
    public void testDivide() {
        Assert.assertEquals(MEMORY_CPU,MEMORY2_CPU2.divide(2));
    }
    
    public void testDivideExactly() {
        Assert.assertEquals(2,MEMORY2_CPU2.divideExactly(MEMORY_CPU));
        Assert.assertEquals(-1,MEMORY2_CPU2.divideExactly(CPU));
        Assert.assertEquals(-1,MEMORY2_CPU2.divideExactly(CPU.add(MEMORY_CPU)));
    }
    
    public void testSet() {
        Assert.assertEquals(MEMORY2_CPU2.subtract(MEMORY),MEMORY2_CPU2.set(new MemoryCapacityRequirement(1L)));
    }
}
