package org.openspaces.utest.grid.gsm;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.openspaces.core.internal.commons.math.fraction.Fraction;
import org.openspaces.grid.gsm.capacity.AggregatedAllocatedCapacity;
import org.openspaces.grid.gsm.capacity.AllocatedCapacity;
import org.openspaces.grid.gsm.machines.BinPackingSolver;

public class BinPackingSolverTest extends TestCase {

    private static final Log logger = LogFactory.getLog(BinPackingSolverTest.class);

    // agent1 can hold 3 containers
    private static final String AGENT1_UID = "AGENT1";
    private static final String AGENT2_UID = "AGENT2";
    private static final String AGENT3_UID = "AGENT3";
    private static final String AGENT4_UID = "AGENT4";
    
    private static final AllocatedCapacity SIX_PRIMARY_CONTAINER_CAPACITY = new AllocatedCapacity(
            new Fraction(2 * 6, 1), 512 * 6);

    private static final long MAX_MEMORY_MEGABYTES = new AllocatedCapacity(Fraction.TWO, 512).getMemoryInMB() * 100;

    

    /**
     * Tests allocation of a single container on a single machine using number of machines solver.
     */
    @Test
    public void testOneMachine() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity());
        solver.setContainerMemoryCapacityInMB(new AllocatedCapacity(Fraction.TWO, 512).getMemoryInMB());
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);
        solver.setMinimumNumberOfMachines(1);
        AggregatedAllocatedCapacity unallocatedCapacity = new AggregatedAllocatedCapacity().add(AGENT1_UID,
                new AllocatedCapacity(new Fraction(2 * 3, 1), 512 * 3));
        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.solveNumberOfMachines(1);
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        AggregatedAllocatedCapacity allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT1_UID));
        Assert.assertEquals(1, allocatedCapacity.getAgentUids().size());

        // when asking for 1 machine, the algorithm is expected to return only memory, since it does
        // not know how much cpu is needed.
        Assert.assertEquals(new AllocatedCapacity(Fraction.ZERO, 512), allocatedCapacity.getAgentCapacity(AGENT1_UID));
    }

    /**
     * Tests allocation of a two machines given one unallocated machine.
     */
    @Test
    public void testTwoMachinesOverflow() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity());
        solver.setContainerMemoryCapacityInMB(new AllocatedCapacity(Fraction.TWO, 512).getMemoryInMB());
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);
        solver.setMinimumNumberOfMachines(2);
        AggregatedAllocatedCapacity unallocatedCapacity = new AggregatedAllocatedCapacity().add(AGENT1_UID,
                new AllocatedCapacity(new Fraction(2 * 3, 1), 512 * 3));
        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.solveNumberOfMachines(2);
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        AggregatedAllocatedCapacity allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT1_UID));
        Assert.assertEquals(1, allocatedCapacity.getAgentUids().size());

        // when asking for 1 machine, the algorithm is expected to return only memory, since it does
        // not know how much cpu is needed.
        Assert.assertEquals(new AllocatedCapacity(Fraction.ZERO, 512), allocatedCapacity.getAgentCapacity(AGENT1_UID));
    }

    /**
     * Tests allocation of a two machines given three unallocated machines.
     */
    @Test
    public void testTwoMachines() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity());
        solver.setContainerMemoryCapacityInMB(new AllocatedCapacity(Fraction.TWO, 512).getMemoryInMB());
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        AggregatedAllocatedCapacity unallocatedCapacity = new AggregatedAllocatedCapacity().add(AGENT1_UID,
                new AllocatedCapacity(new Fraction(2 * 3, 1), 512 * 3)).add(AGENT2_UID,
                new AllocatedCapacity(new Fraction(2 * 3, 1), 512 * 3)).add(AGENT3_UID,
                new AllocatedCapacity(new Fraction(2 * 3, 1), 512 * 3));

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        solver.solveNumberOfMachines(2);
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        AggregatedAllocatedCapacity allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        Assert.assertEquals(2, allocatedCapacity.getAgentUids().size());

        // when asking for 2 machine, the algorithm is expected to return only memory, since it does
        // not know how much cpu is needed.
        for (String agentUid : allocatedCapacity.getAgentUids()) {
            Assert.assertEquals(new AllocatedCapacity(Fraction.ZERO, 512), allocatedCapacity.getAgentCapacity(agentUid));
        }
    }

    /**
     * Tests allocation of a two machines given three unallocated machines, two of them already
     * occupied by this pu.
     */
    @Test
    public void testTwoNewMachinesOverflow() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity().add(AGENT1_UID,
                new AllocatedCapacity(Fraction.TWO, 512)).add(AGENT2_UID, new AllocatedCapacity(Fraction.TWO, 512)));

        solver.setContainerMemoryCapacityInMB(new AllocatedCapacity(Fraction.TWO, 512).getMemoryInMB());
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        AggregatedAllocatedCapacity unallocatedCapacity = new AggregatedAllocatedCapacity().add(AGENT1_UID,
                new AllocatedCapacity(new Fraction(2 * 3, 1), 512 * 3)).add(AGENT2_UID,
                new AllocatedCapacity(new Fraction(2 * 3, 1), 512 * 3)).add(AGENT3_UID,
                new AllocatedCapacity(new Fraction(2 * 3, 1), 512 * 3));

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        solver.solveNumberOfMachines(2);
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        AggregatedAllocatedCapacity allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        Assert.assertEquals(1, allocatedCapacity.getAgentUids().size());
        Assert.assertFalse(allocatedCapacity.getAgentUids().contains(AGENT1_UID));
        Assert.assertFalse(allocatedCapacity.getAgentUids().contains(AGENT2_UID));
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT3_UID));

        // when asking for machines, the algorithm is expected to return only memory, since it does
        // not know how much cpu is needed.
        Assert.assertEquals(new AllocatedCapacity(Fraction.ZERO, 512), allocatedCapacity.getAgentCapacity(AGENT3_UID));
    }

    /**
     * Tests allocation of a two machines given three unallocated machines.
     */
    @Test
    public void testThreeMachines() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity());
        solver.setContainerMemoryCapacityInMB(new AllocatedCapacity(Fraction.TWO, 512).getMemoryInMB());
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        AggregatedAllocatedCapacity unallocatedCapacity = new AggregatedAllocatedCapacity().add(AGENT1_UID,
                new AllocatedCapacity(new Fraction(2 * 3, 1), 512 * 3)).add(AGENT2_UID,
                new AllocatedCapacity(new Fraction(2 * 3, 1), 512 * 3)).add(AGENT3_UID,
                new AllocatedCapacity(new Fraction(2 * 3, 1), 512 * 3));

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        solver.solveNumberOfMachines(3);
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        AggregatedAllocatedCapacity allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT1_UID));
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT2_UID));
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT3_UID));
        Assert.assertEquals(3, allocatedCapacity.getAgentUids().size());

        // when asking for 2 machine, the algorithm is expected to return only memory, since it does
        // not know how much cpu is needed.
        Assert.assertEquals(new AllocatedCapacity(Fraction.ZERO, 512), allocatedCapacity.getAgentCapacity(AGENT1_UID));
        Assert.assertEquals(new AllocatedCapacity(Fraction.ZERO, 512), allocatedCapacity.getAgentCapacity(AGENT2_UID));
        Assert.assertEquals(new AllocatedCapacity(Fraction.ZERO, 512), allocatedCapacity.getAgentCapacity(AGENT3_UID));
    }

    /**
     * Tests allocation of a single container on a single machine using capacity solver.
     */
    @Test
    public void testCapacity() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity());
        solver.setContainerMemoryCapacityInMB(new AllocatedCapacity(Fraction.TWO, 512).getMemoryInMB());
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        AggregatedAllocatedCapacity unallocatedCapacity = new AggregatedAllocatedCapacity().add(AGENT1_UID,
                new AllocatedCapacity(new Fraction(2 * 3, 1), 512 * 3));
        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(1);
        solver.solveManualCapacity(new AllocatedCapacity(Fraction.TWO, 512));
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        AggregatedAllocatedCapacity allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT1_UID));
        Assert.assertEquals(1, allocatedCapacity.getAgentUids().size());
        Assert.assertEquals(new AllocatedCapacity(Fraction.TWO, 512), allocatedCapacity.getAgentCapacity(AGENT1_UID));
    }

    /**
     * Tests allocation of two containers capacity on a single machine that has only room for one
     * container.
     */
    @Test
    public void testCapacityOverflow() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity());
        solver.setContainerMemoryCapacityInMB(new AllocatedCapacity(Fraction.TWO, 512).getMemoryInMB());
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        AggregatedAllocatedCapacity unallocatedCapacity = new AggregatedAllocatedCapacity().add(AGENT1_UID,
                new AllocatedCapacity(Fraction.TWO, 512));
        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(1);
        solver.solveManualCapacity(new AllocatedCapacity(new Fraction(2 * 2, 1), 512 * 2));
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        AggregatedAllocatedCapacity allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT1_UID));
        Assert.assertEquals(1, allocatedCapacity.getAgentUids().size());
        Assert.assertEquals(new AllocatedCapacity(Fraction.TWO, 512), allocatedCapacity.getAgentCapacity(AGENT1_UID));
    }

    /**
     * Tests allocation of a backup container on agent1 that already has 2 primary containers using
     * capacity solver.
     */
    @Test
    public void testIncrementalCapacity() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity().add(AGENT1_UID, new AllocatedCapacity(
                new Fraction(2 * 2, 1), 512 * 2)));
        solver.setContainerMemoryCapacityInMB(new AllocatedCapacity(Fraction.TWO, 512).getMemoryInMB());
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        AggregatedAllocatedCapacity unallocatedCapacity = new AggregatedAllocatedCapacity().add(AGENT1_UID,
                new AllocatedCapacity(Fraction.TWO, 512));
        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(1);
        solver.solveManualCapacity(new AllocatedCapacity(Fraction.ZERO, 512));
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        AggregatedAllocatedCapacity allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT1_UID));
        Assert.assertEquals(1, allocatedCapacity.getAgentUids().size());
        Assert.assertEquals(new AllocatedCapacity(Fraction.ZERO, 512), allocatedCapacity.getAgentCapacity(AGENT1_UID));
    }

    /**
     * Tests allocation of a primary container on agent1 that only has 2 CPU cores left, but no
     * memory left.
     */
    @Test
    public void testOverflowIncrementalCapacity() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity().add(AGENT1_UID, new AllocatedCapacity(
                new Fraction(2 * 2, 1), 512 * 2)));
        solver.setContainerMemoryCapacityInMB(new AllocatedCapacity(Fraction.TWO, 512).getMemoryInMB());
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        AggregatedAllocatedCapacity unallocatedCapacity = new AggregatedAllocatedCapacity().add(AGENT1_UID,
                new AllocatedCapacity(Fraction.TWO, 0));
        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(1);
        solver.solveManualCapacity(new AllocatedCapacity(Fraction.ZERO, 512));
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        AggregatedAllocatedCapacity allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertFalse(allocatedCapacity.getAgentUids().contains(AGENT1_UID));
    }

    /**
     * Tests allocation of two more CPU cores on agent1 that already has a container deployed.
     */
    @Test
    public void testIncrementalCpuCoreCapacity() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity().add(AGENT1_UID, new AllocatedCapacity(
                Fraction.TWO, 512)));
        solver.setContainerMemoryCapacityInMB(new AllocatedCapacity(Fraction.TWO, 512).getMemoryInMB());
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        AggregatedAllocatedCapacity unallocatedCapacity = new AggregatedAllocatedCapacity().add(AGENT1_UID,
                new AllocatedCapacity(Fraction.TWO, 512));
        solver.setUnallocatedCapacity(unallocatedCapacity);
        // request only cpu cores, not memory
        AllocatedCapacity capacityToAllocate = new AllocatedCapacity(
                new AllocatedCapacity(Fraction.TWO, 512).getCpuCores(), 0);
        solver.setMinimumNumberOfMachines(1);
        solver.solveManualCapacity(capacityToAllocate);
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        AggregatedAllocatedCapacity allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(allocatedCapacity.getAgentUids().contains(AGENT1_UID));
        Assert.assertEquals(1, allocatedCapacity.getAgentUids().size());
        Assert.assertEquals(capacityToAllocate, allocatedCapacity.getAgentCapacity(AGENT1_UID));
    }

    /**
     * Tests deallocation of a container that is moved from agent2 to agent3 due to cpu constraints
     */
    @Test
    public void testIncrementalCpuCoreCapacityWithDeallocation() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity().add(AGENT1_UID,
                new AllocatedCapacity(Fraction.TWO, 512)).add(AGENT2_UID, new AllocatedCapacity(new Fraction(4), 1024)));

        solver.setContainerMemoryCapacityInMB(new AllocatedCapacity(Fraction.TWO, 512).getMemoryInMB());
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(512 * 100);
        solver.setLogger(logger);

        solver.setUnallocatedCapacity(new AggregatedAllocatedCapacity().add(AGENT1_UID,
                new AllocatedCapacity(new Fraction(2), 512)).add(AGENT3_UID,
                new AllocatedCapacity(new Fraction(4), 1024)));

        AllocatedCapacity capacityToAllocate = new AllocatedCapacity(new Fraction(6), 0);
        solver.setMinimumNumberOfMachines(1);
        solver.solveManualCapacity(capacityToAllocate);
        AggregatedAllocatedCapacity deallocatedCapacity = solver.getDeallocatedCapacityResult();
        Assert.assertTrue(deallocatedCapacity.getAgentUids().contains(AGENT2_UID));
        Assert.assertEquals(1, deallocatedCapacity.getAgentUids().size());
        Assert.assertEquals(new AllocatedCapacity(Fraction.ZERO, 512), deallocatedCapacity.getAgentCapacity(AGENT2_UID));

        AggregatedAllocatedCapacity allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertEquals(2, allocatedCapacity.getAgentUids().size());
        Assert.assertEquals(new AllocatedCapacity(Fraction.TWO, 0),
                allocatedCapacity.getAgentCapacityOrZero(AGENT1_UID));
        Assert.assertEquals(new AllocatedCapacity(new Fraction(4), 512),
                allocatedCapacity.getAgentCapacityOrZero(AGENT3_UID));
    }

    /**
     * Tests allocation of too many containers, and rejects due to max memory
     */
    @Test
    public void testMaxNumberOfContainers() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity().add(AGENT1_UID, new AllocatedCapacity(
                Fraction.TWO, 512)));

        solver.setContainerMemoryCapacityInMB(512);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(512);
        solver.setLogger(logger);

        solver.setUnallocatedCapacity(new AggregatedAllocatedCapacity().add(AGENT1_UID, new AllocatedCapacity(
                Fraction.TWO, 512)));

        AllocatedCapacity capacityToAllocate = new AllocatedCapacity(new Fraction(4), 1024);
        solver.setMinimumNumberOfMachines(1);
        solver.solveManualCapacity(capacityToAllocate);
        AggregatedAllocatedCapacity deallocatedCapacity = solver.getDeallocatedCapacityResult();
        Assert.assertTrue(deallocatedCapacity.equalsZero());

        AggregatedAllocatedCapacity allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertEquals(1, allocatedCapacity.getAgentUids().size());
        Assert.assertEquals(new AllocatedCapacity(new Fraction(2), 0), allocatedCapacity.getAgentCapacity(AGENT1_UID));
    }

    /**
     * Tests allocation of too many containers, and rejects due to max memory
     */
    @Test
    public void testTwoMaxNumberOfContainers() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity().add(AGENT1_UID, new AllocatedCapacity(
                Fraction.TWO, 512)));

        solver.setContainerMemoryCapacityInMB(512);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(1024);
        solver.setLogger(logger);

        solver.setUnallocatedCapacity(new AggregatedAllocatedCapacity().add(AGENT1_UID,
                new AllocatedCapacity(new Fraction(2 * 2), 512 * 2)).add(AGENT2_UID,
                new AllocatedCapacity(new Fraction(2 * 2), 512 * 2)));

        AllocatedCapacity capacityToAllocate = new AllocatedCapacity(new Fraction(2 * 3), 512 * 3);
        solver.setMinimumNumberOfMachines(2);
        solver.solveManualCapacity(capacityToAllocate);
        AggregatedAllocatedCapacity deallocatedCapacity = solver.getDeallocatedCapacityResult();
        Assert.assertTrue(deallocatedCapacity.equalsZero());

        AggregatedAllocatedCapacity allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertEquals(new AllocatedCapacity(Fraction.TWO, 0), allocatedCapacity.getAgentCapacity(AGENT1_UID));
        Assert.assertEquals(new AllocatedCapacity(new Fraction(4), 512), allocatedCapacity.getAgentCapacity(AGENT2_UID));
    }

    /**
     * Tests allocation of a 6 containers, given three machines each can hold three containers.
     */
    @Test
    public void testCapacitySixContainersThreeMachines() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity());
        solver.setContainerMemoryCapacityInMB(new AllocatedCapacity(Fraction.TWO, 512).getMemoryInMB());
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        AggregatedAllocatedCapacity unallocatedCapacity = new AggregatedAllocatedCapacity().add(AGENT1_UID,
                new AllocatedCapacity(new Fraction(2 * 3), 512 * 3)).add(AGENT2_UID,
                new AllocatedCapacity(new Fraction(2 * 3), 512 * 3)).add(AGENT3_UID,
                new AllocatedCapacity(new Fraction(2 * 3), 512 * 3));

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        solver.solveManualCapacity(new AllocatedCapacity(new Fraction(2 * 6), 512 * 6));
        AggregatedAllocatedCapacity allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        Assert.assertEquals(2, allocatedCapacity.getAgentUids().size());
        for (String agentUid : allocatedCapacity.getAgentUids()) {
            Assert.assertEquals(new AllocatedCapacity(new Fraction(2 * 3, 1), 512 * 3),
                    allocatedCapacity.getAgentCapacity(agentUid));
        }
    }

    /**
     * Tests allocation of a 6 primary containers, given three machines each can hold two primary
     * containers and one backup container.
     */
    @Test
    public void testCapacitySixContainersThreeMachinesThatCanHoldTwoEach() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity());
        solver.setContainerMemoryCapacityInMB(new AllocatedCapacity(Fraction.TWO, 512).getMemoryInMB());
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        AllocatedCapacity twoPrimariesOneBackupCapacity = new AllocatedCapacity(new Fraction(2 * 2, 1), 512 * 2).add(new AllocatedCapacity(
                Fraction.ZERO, 512));
        AggregatedAllocatedCapacity unallocatedCapacity = new AggregatedAllocatedCapacity().add(AGENT1_UID,
                twoPrimariesOneBackupCapacity).add(AGENT2_UID, twoPrimariesOneBackupCapacity).add(AGENT3_UID,
                twoPrimariesOneBackupCapacity);
        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        solver.solveManualCapacity(SIX_PRIMARY_CONTAINER_CAPACITY);
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        solver.getAllocatedCapacityResult();
        AggregatedAllocatedCapacity allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertEquals(3, allocatedCapacity.getAgentUids().size());
        for (String agentUid : allocatedCapacity.getAgentUids()) {
            Assert.assertEquals(new AllocatedCapacity(new Fraction(2 * 2, 1), 512 * 2),
                    allocatedCapacity.getAgentCapacity(agentUid));
        }
    }

    /**
     * Tests allocation of a two "best" machines given three unallocated machines.
     */

    @Test
    public void testChooseTwoMachinesWithLessMemory() {

        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity());
        solver.setContainerMemoryCapacityInMB(new AllocatedCapacity(Fraction.TWO, 512).getMemoryInMB());
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(MAX_MEMORY_MEGABYTES);
        solver.setLogger(logger);

        AggregatedAllocatedCapacity unallocatedCapacity = new AggregatedAllocatedCapacity().add(AGENT1_UID,
                SIX_PRIMARY_CONTAINER_CAPACITY).add(AGENT2_UID, new AllocatedCapacity(new Fraction(4), 512 * 2)).add(
                AGENT3_UID, new AllocatedCapacity(new Fraction(6), 512 * 3));

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        solver.solveNumberOfMachines(2);
        AggregatedAllocatedCapacity allocatedCapacity = solver.getAllocatedCapacityResult();
        Assert.assertTrue(solver.getDeallocatedCapacityResult().equalsZero());
        Assert.assertEquals(2, allocatedCapacity.getAgentUids().size());

        // when asking for 2 machine, the algorithm is expected to return only memory, since it does
        // not know how much cpu is needed.
        for (String agentUid : allocatedCapacity.getAgentUids()) {
            Assert.assertEquals(new AllocatedCapacity(Fraction.ZERO, 512), allocatedCapacity.getAgentCapacity(agentUid));
        }
    }

    /**
     * starts with memory only (1000 containers on two machines) and then adds cpu requirements
     * (will cause a spread of 1000 containers to 1000 empty machines).
     */
    @Test
    public void testLargeScaleAllocation() {
        final int nMACHINES = 100;
        
        BinPackingSolver solver = new BinPackingSolver();
        //allocate 2 machines each with - 500 containers. Each container with 512 MB needing 1 CPU
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity()
                .add(AGENT1_UID, new AllocatedCapacity(new Fraction(500), 512 * 500))
                .add(AGENT2_UID, new AllocatedCapacity(new Fraction(500), 512 * 500)));

        solver.setContainerMemoryCapacityInMB(new AllocatedCapacity(Fraction.ONE, 512).getMemoryInMB());
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(new AllocatedCapacity(new Fraction(nMACHINES), 512 * nMACHINES).getMemoryInMB() * 100);
        solver.setLogger(logger);

        AggregatedAllocatedCapacity unallocatedCapacity = new AggregatedAllocatedCapacity();
        for (int i = 3; i <= nMACHINES; ++i) {
            // add more machines - each can occupy 1 container
            unallocatedCapacity = unallocatedCapacity.add("AGENT" + i, new AllocatedCapacity(Fraction.ONE, 512));
        }

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        // goal - scale to nMACHINES machines.
        // result should be that the solver will move 998 containers from the two machines into the 998 free machines
        // occupying 1 container per machine.
        solver.solveManualCapacity(new AllocatedCapacity(new Fraction(nMACHINES), 0));
        Assert.assertEquals(0, solver.getDeallocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(Fraction.ZERO, solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity().getCpuCores());
        Assert.assertEquals(0, solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity().getMemoryInMB());
        
        Assert.assertEquals(nMACHINES -2, solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(new Fraction(nMACHINES -2), solver.getAllocatedCapacityResult().getTotalAllocatedCapacity().getCpuCores());
        Assert.assertEquals(512 * (nMACHINES -2), solver.getAllocatedCapacityResult().getTotalAllocatedCapacity().getMemoryInMB());
    }
    
    /**
     * Check how much time does it takes when there is 1000 allocated machines without unallocated capacity, and a request for more memory.
     * This simulates the time period in which a new virtual machine is being started and we keep pounding the algorithm again and again. 
     */
    @Test
    public void testLargeScaleAllocation2() {
        final int nMACHINES = 100;
        
        BinPackingSolver solver = new BinPackingSolver();
        
        AggregatedAllocatedCapacity allocatedCapacity = new AggregatedAllocatedCapacity();
        for (int i=1; i<=nMACHINES; ++i) {
            allocatedCapacity = allocatedCapacity.add("AGENT" + i, new AllocatedCapacity(Fraction.ONE, 512));
        }
        
        //allocate nMACHINES machines each with - 1 containers. Each container with 512 MB needing 1 CPU
        solver.setAllocatedCapacityForPu(allocatedCapacity);

        solver.setContainerMemoryCapacityInMB(new AllocatedCapacity(Fraction.ONE, 512).getMemoryInMB());
        
        //twice as much as we currently have allocated
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(new AllocatedCapacity(new Fraction(2000), 2 * 512 * nMACHINES).getMemoryInMB() * 100);
        solver.setLogger(logger);

        AggregatedAllocatedCapacity unallocatedCapacity = new AggregatedAllocatedCapacity();
        for (int i = 1; i <= nMACHINES; ++i) {
            //each can occupy less than 1 container
            unallocatedCapacity = unallocatedCapacity.add("AGENT" + i, new AllocatedCapacity(Fraction.ONE, 128));
        }

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        // goal - scale by 512 MB.
        // result should be that the solver will not be able to move any containers
        solver.solveManualCapacity(new AllocatedCapacity(Fraction.ZERO, 512));
        
        Assert.assertEquals(0, solver.getDeallocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(Fraction.ZERO, solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity().getCpuCores());
        Assert.assertEquals(0, solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity().getMemoryInMB());
        
        Assert.assertEquals(0, solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(Fraction.ZERO, solver.getAllocatedCapacityResult().getTotalAllocatedCapacity().getCpuCores());
        Assert.assertEquals(0, solver.getAllocatedCapacityResult().getTotalAllocatedCapacity().getMemoryInMB());
    }
    
    
    @Test
    public void testRelocateCpuFromSourceMachine() {
        BinPackingSolver solver = new BinPackingSolver();
        
        solver.setAllocatedCapacityForPu(new AggregatedAllocatedCapacity()
                                .add("AGENT_1", new AllocatedCapacity(new Fraction(4), 512))
                                .add("AGENT_2", new AllocatedCapacity(new Fraction(1), 128)));
        
        solver.setContainerMemoryCapacityInMB(new AllocatedCapacity(new Fraction(1), 128).getMemoryInMB());
        
        solver.setUnallocatedCapacity(new AggregatedAllocatedCapacity()
                                .add("AGENT_2", new AllocatedCapacity(new Fraction(5), 128*5)));
        
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(new AllocatedCapacity(new Fraction(100), 1024).getMemoryInMB() * 100);
        solver.setMinimumNumberOfMachines(2);
        solver.solveManualCapacity(new AllocatedCapacity(new Fraction(1), 0));
        
        Assert.assertEquals(new Fraction(6), solver.getAllocatedCapacityForPu().getTotalAllocatedCapacity().getCpuCores());
        Assert.assertEquals(640, solver.getAllocatedCapacityForPu().getTotalAllocatedCapacity().getMemoryInMB());
        
        Assert.assertEquals(1, solver.getDeallocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(new Fraction(2).divide(5), solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity().getCpuCores());
        
        Assert.assertEquals(1, solver.getAllocatedCapacityResult().getAgentUids().size());
        Assert.assertEquals(new Fraction(7).divide(5), solver.getAllocatedCapacityResult().getTotalAllocatedCapacity().getCpuCores());
        Assert.assertEquals(128, solver.getAllocatedCapacityResult().getTotalAllocatedCapacity().getMemoryInMB());
    }
    
    /**
     * Tests allocation of a fourth machine taking from one of the first three due to max memory limit
     */
    @Test
    public void testThreeMachinesScaleOutCpuToFourMachines() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(
                new AggregatedAllocatedCapacity()
                .add(AGENT1_UID,new AllocatedCapacity(new Fraction(2), 500))
                .add(AGENT2_UID,new AllocatedCapacity(new Fraction(2), 500))
                .add(AGENT3_UID,new AllocatedCapacity(new Fraction(2), 500))        
        );
        solver.setContainerMemoryCapacityInMB(250);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(6*250);
        solver.setLogger(logger);

        AggregatedAllocatedCapacity unallocatedCapacity = 
            new AggregatedAllocatedCapacity()
            .add(AGENT1_UID,new AllocatedCapacity(new Fraction(0), 500))
            .add(AGENT2_UID,new AllocatedCapacity(new Fraction(0), 500))
            .add(AGENT3_UID,new AllocatedCapacity(new Fraction(0), 500))
            .add(AGENT4_UID,new AllocatedCapacity(new Fraction(2), 1000));

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        solver.solveManualCapacity(new AllocatedCapacity(new Fraction(2),0));
        
        Assert.assertEquals(new AllocatedCapacity(new Fraction(2),250),solver.getAllocatedCapacityResult().getTotalAllocatedCapacity());
        Assert.assertEquals(new AllocatedCapacity(new Fraction(0),250),solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity());
        
   }
    
    /**
     * Tests no allocation of a fourth machine due to max memory limit
     */
    @Test
    public void testThreeMachinesNoScaleOutCpuToFourMachines() {
        BinPackingSolver solver = new BinPackingSolver();
        solver.setAllocatedCapacityForPu(
                new AggregatedAllocatedCapacity()
                .add(AGENT1_UID,new AllocatedCapacity(new Fraction(2), 250))
                .add(AGENT2_UID,new AllocatedCapacity(new Fraction(2), 250))
                .add(AGENT3_UID,new AllocatedCapacity(new Fraction(2), 250))        
        );
        solver.setContainerMemoryCapacityInMB(250);
        solver.setMaxAllocatedMemoryCapacityOfPuInMB(3*250);
        solver.setLogger(logger);

        AggregatedAllocatedCapacity unallocatedCapacity = 
            new AggregatedAllocatedCapacity()
            .add(AGENT1_UID,new AllocatedCapacity(new Fraction(0), 250))
            .add(AGENT2_UID,new AllocatedCapacity(new Fraction(0), 250))
            .add(AGENT3_UID,new AllocatedCapacity(new Fraction(0), 250))
            .add(AGENT4_UID,new AllocatedCapacity(new Fraction(2), 500));

        solver.setUnallocatedCapacity(unallocatedCapacity);
        solver.setMinimumNumberOfMachines(2);
        solver.solveManualCapacity(new AllocatedCapacity(new Fraction(2),0));
        
        Assert.assertEquals(new AllocatedCapacity(new Fraction(0),0),solver.getAllocatedCapacityResult().getTotalAllocatedCapacity());
        Assert.assertEquals(new AllocatedCapacity(new Fraction(0),0),solver.getDeallocatedCapacityResult().getTotalAllocatedCapacity());
        
   }

}
