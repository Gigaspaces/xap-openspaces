/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.itest.pu.sla.requirements;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openspaces.pu.sla.InstanceSLA;
import org.openspaces.pu.sla.SLA;
import org.openspaces.pu.sla.requirement.CpuRequirement;
import org.openspaces.pu.sla.requirement.HostRequirement;
import org.openspaces.pu.sla.requirement.MemoryRequirement;
import org.openspaces.pu.sla.requirement.SystemRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


/**
 * @author kimchy
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/org/openspaces/itest/pu/sla/requirements/requirements.xml")
public class RequirementsSlaSchameTests   { 
@Autowired
    protected ApplicationContext ac;

    public RequirementsSlaSchameTests() {
 
    }

    protected String[] getConfigLocations() {
        return new String[]{"/org/openspaces/itest/pu/sla/requirements/requirements.xml"};
    }

     @Test public void testRequirements() {
        SLA sla = (SLA) ac.getBean("SLA");
        assertNotNull(sla);
        assertNotNull(sla.getRequirements());
        assertEquals(4, sla.getRequirements().size());
        HostRequirement host = (HostRequirement) sla.getRequirements().get(0);
        assertEquals("test", host.getIp());
        SystemRequirement system = (SystemRequirement) sla.getRequirements().get(1);
        assertEquals("test2", system.getName());
        assertNotNull(system.getAttributes());
        CpuRequirement cpuRequirement = (CpuRequirement) sla.getRequirements().get(2);
        assertEquals(.9, cpuRequirement.getHigh(),0);
        MemoryRequirement memoryRequirement = (MemoryRequirement) sla.getRequirements().get(3);
        assertEquals(.8, memoryRequirement.getHigh(),0);

        // verify instance SLA
        assertNotNull(sla.getInstanceSLAs());
        assertEquals(1, sla.getInstanceSLAs().size());
        InstanceSLA instanceSLA = sla.getInstanceSLAs().get(0);
        assertEquals(1, instanceSLA.getInstanceId().intValue());
        assertEquals(2, instanceSLA.getBackupId().intValue());
        assertNotNull(instanceSLA.getRequirements());
        assertEquals(1, instanceSLA.getRequirements().size());
        host = (HostRequirement) instanceSLA.getRequirements().get(0);
        assertEquals("test1", host.getIp());
    }
}

